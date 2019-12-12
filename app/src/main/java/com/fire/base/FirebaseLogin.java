package com.fire.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.fire.base.login.sms.PhoneVerificationActivity;
import com.google.android.gms.common.GooglePlayServicesUtil;

import static android.app.Activity.RESULT_OK;

public class FirebaseLogin {
    private final static int SMS_VERIFICATION_CODE = 101;
    public final static String PHONE_NUMBER = "phone_number";
    private static Listener mListener;

    public static void setOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SMS_VERIFICATION_CODE) {
            if (resultCode == RESULT_OK) {
                String phoneNumberString = data.getStringExtra(PHONE_NUMBER);
                Bundle bundle = new Bundle();
                bundle.putString(PHONE_NUMBER, phoneNumberString);
                if (mListener != null) {
                    mListener.onSuccess(bundle);
                    return;
                }
            }
            if (mListener != null) mListener.onError("Failed to login using sms verification");
        }
    }

    public static void openSmsVerification(Activity activity, Listener listener){
        if(isPlayStoreInstalled(activity.getApplicationContext())){
            mListener = listener;
            Intent intent = new Intent(activity, PhoneVerificationActivity.class);
            activity.startActivityForResult(intent, SMS_VERIFICATION_CODE);
        }
        else {
            if (listener != null) listener.onError("Play store missing. Play store is needed for sms verification.");
        }
    }

    public static boolean isPlayStoreInstalled(Context context){
        try {
            context.getPackageManager()
                    .getPackageInfo(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public interface Listener{
        void onSuccess(Bundle bundle);
        void onError(String message);

    }
}
