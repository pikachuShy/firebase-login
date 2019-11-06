package com.fire.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.fire.base.login.sms.PhoneVerificationActivity;

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
            if (mListener != null) mListener.onError();
        }
    }

    public static void openSmsVerification(Activity activity, Listener listener){
        mListener = listener;

        Intent intent = new Intent(activity, PhoneVerificationActivity.class);
        activity.startActivityForResult(intent, SMS_VERIFICATION_CODE);
    }

    public interface Listener{
        void onSuccess(Bundle bundle);
        void onError();

    }
}
