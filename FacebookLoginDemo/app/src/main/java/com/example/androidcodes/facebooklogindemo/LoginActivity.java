package com.example.androidcodes.facebooklogindemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    /*
    *   This Project is taken From : https://github.com/theodhorpandeli/facebook-sdk-4.18.0
    *
    *   Check this link to get email-id = http://stackoverflow.com/questions/29295987/android-facebook-4-0-sdk-how-to-get-email-date-of-birth-and-gender-of-user
    *
    * */

    private Activity activity;

    private CallbackManager callbackManager;

    private AccessTokenTracker accessTokenTracker;

    private ProfileTracker profileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        activity = LoginActivity.this;

        accessTokenTracker = new AccessTokenTracker() {

            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {

                nextActivity(newProfile);

            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                loginButton.performClick();

            }
        });

        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                final AccessToken accessToken = AccessToken.getCurrentAccessToken();
                final GraphRequest request = GraphRequest.newMeRequest(accessToken,//loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                System.out.println("LoginActivity -- Facebook -- " +
                                        "DetailsResponse --> " + response.toString());

                                // Application code
                                try {

                                    String userId = object.getString("id");

                                    String email = object.getString("email");

                                    //String birthday = object.getString("birthday");// 01/31/1980 format

                                    /*request.setParameters(null);
                                    request.setGraphPath("/".concat(userId).concat("/friendlists"));
                                    request.setHttpMethod(HttpMethod.GET);
                                    request.executeAsync();*/

                                    //TODO : To get friendlist.
                                    new GraphRequest(accessToken, "/".concat(userId).
                                            concat("/friendlists"), null, HttpMethod.GET,
                                            new GraphRequest.Callback() {

                                                public void onCompleted(GraphResponse response) {

                                                    /* go to this link : https://developers.facebook.com/docs/graph-api/reference/user and
                                                       select "Friend List" from left side menu and then
                                                       click on "Android SDK" button inside "Examples".*/

                                                    try {

                                                        System.out.println("LoginActivity -- Facebook -- " +
                                                                "FriendlistResponse --> " + response);

                                                        int index = 0;

                                                        JSONArray fbFriends = response.getJSONObject().
                                                                optJSONArray("data");
                                                        JSONObject friendList = new JSONObject();
                                                        JSONObject contact;
                                                        JSONArray contactList = new JSONArray();
                                                        for (int i = 0; i < fbFriends.length(); i++) {

                                                            contact = new JSONObject();
                                                            contact.put("name", fbFriends.getJSONObject(i).
                                                                    getString("name"));
                                                            contact.put("contact_id", -1);
                                                            contact.put("fb_id", fbFriends.getJSONObject(i).
                                                                    getString("id"));
                                                            contact.put("phonenumber", "");

                                                            contactList.put(index++, contact);

                                                        }

                                                        friendList.put("friends", contactList);

                                                        System.out.println("");

                                                    } catch (JSONException e) {

                                                        e.printStackTrace();

                                                    }
                                                }
                                            }).executeAsync();

                                    //TODO : To get data of "About Us" field of facebook of logged in user.
                                    Bundle parameters = new Bundle();
                                    parameters.putString("fields",
                                            "id,name,first_name,last_name,email,gender,birthday,location");
                                    new GraphRequest(accessToken, "/".concat(userId), parameters,
                                            HttpMethod.GET, new GraphRequest.Callback() {

                                        public void onCompleted(GraphResponse response) {

                                            try {

                                                //"/656555737865627/friendlists"

                                                System.out.println("LoginActivity -- Facebook -- " +
                                                        "Details1Response --> " + response);

                                            } catch (Exception e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    }).executeAsync();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields",
                        "id,name,first_name,last_name,email,gender,birthday,location");
                request.setParameters(parameters);
                request.executeAsync();

                Profile profile = Profile.getCurrentProfile();
                nextActivity(profile);

                Toast.makeText(activity, "Logging in...", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {

                Toast.makeText(activity, "Login canceled by user ... ", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(FacebookException e) {

                Toast.makeText(activity, "Login Error : " + e.getMessage(), Toast.LENGTH_SHORT).
                        show();

            }
        };

        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday",
                "user_friends"));
        loginButton.registerCallback(callbackManager, callback);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Facebook login
        Profile profile = Profile.getCurrentProfile();

        nextActivity(profile);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();

        //Facebook login
        accessTokenTracker.stopTracking();

        profileTracker.stopTracking();

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        //Facebook login
        callbackManager.onActivityResult(requestCode, responseCode, intent);

    }

    private void nextActivity(Profile profile) {

        if (profile != null) {

            Intent main = new Intent(activity, MainActivity.class);
            main.putExtra("name", profile.getFirstName());
            main.putExtra("surname", profile.getLastName());
            main.putExtra("imageUrl", profile.getProfilePictureUri(720, 720).toString());

            startActivity(main);

        }
    }
}
