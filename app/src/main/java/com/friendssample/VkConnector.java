package com.friendssample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class VkConnector {
    private static final String TAG = VkConnector.class.getSimpleName();

    public interface Listener {
        void onFriendsReceived(JSONArray friends);
    }

    private VkConnector.Listener mListener;

    public VkConnector(Activity activity, String appId) {
        VKSdk.initialize(new VKSdkListener() {
            @Override
            public void onCaptchaError(VKError captchaError) {
                Log.d(TAG, "onCaptchaError");
            }

            @Override
            public void onTokenExpired(VKAccessToken expiredToken) {
                Log.d(TAG, "onTokenExpired");
            }

            @Override
            public void onAccessDenied(VKError authorizationError) {
                Log.d(TAG, "onAccessDenied");
            }

            public void onReceiveNewToken(VKAccessToken newToken) {
                Log.d(TAG, "onReceiveNewToken");

                getFriends();
            }
        }, appId);
        VKUIHelper.onCreate(activity);
    }


    public void onResume(Activity activity) {
        VKUIHelper.onResume(activity);
    }

    public void onDestroy(Activity activity) {
        VKUIHelper.onDestroy(activity);
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(activity, requestCode, resultCode, data);
    }

    public static String[] gimmeTheHash(Context context) {
        return VKUtil.getCertificateFingerprint(context, context.getPackageName());
    }

    public void login(VkConnector.Listener listener) {
        mListener = listener;
        String[] scopes = new String[] {
                VKScope.FRIENDS,
                VKScope.NOHTTPS
        };
        VKSdk.authorize(scopes, true, false);
    }

    private void getFriends() {
        VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS,
                "id,photo,first_name,last_name,bdate"));

        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                try {
                    JSONArray friends = response.json
                            .getJSONObject("response")
                            .getJSONArray("items");

                    if (mListener != null)
                        mListener.onFriendsReceived(friends);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {
                Log.d(TAG, "onError");
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType,
                                   long bytesLoaded,
                                   long bytesTotal) {
                Log.d(TAG, "onProgress");
            }
            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                Log.d(TAG, "attemptFailed");
            }
        });
    }


}
