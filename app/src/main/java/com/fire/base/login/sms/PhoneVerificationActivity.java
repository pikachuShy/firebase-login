package com.fire.base.login.sms;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.fire.base.util.ResourcesUtil;
import com.fire.base.util.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.*;
import com.fire.base.login.R;

import java.util.concurrent.TimeUnit;

public class PhoneVerificationActivity extends AppCompatActivity implements SmsPhoneFragment.Listener, SmsSentFragment.Listener{

    public final static String PARAM_PHONE_NUMBER = "phone_number";
    public final static String SMS_PHONE_NUMBER = "sms_phone_number";
    private final static String TAG_SMS_PHONE_FRAGMENT = "sms_phone_fragment";
    private final static String TAG_SMS_SENT_FRAGMENT = "sms_sent_fragment";
    private Snackbar mSnackbar;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mPhoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SmsPhoneFragment fragment = SmsPhoneFragment.newInstance();
        fragment.setListener(this);
        ft.add(R.id.fragment_container, fragment, TAG_SMS_PHONE_FRAGMENT).commit();
        ResourcesUtil.init(this);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = task.getResult().getUser();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(PARAM_PHONE_NUMBER, user.getPhoneNumber().replace("+", ""));
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                        showErrorSnackbar("We couldn't verify your code. Please try again");
                        Fragment fragmnet = getSupportFragmentManager().findFragmentByTag(TAG_SMS_SENT_FRAGMENT);
                        if(fragmnet instanceof SmsSentFragment){
                            ((SmsSentFragment) fragmnet).resetCode();
                        }
                    }
                });
    }

    private  PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            showErrorSnackbar(e.getMessage());
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }

            // Show a message and update the UI
            // ...

        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
            mSnackbar.dismiss();

            Bundle bundle = new Bundle();
            bundle.putString(SMS_PHONE_NUMBER, mPhoneNumber);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            SmsSentFragment fragment = SmsSentFragment.newInstance(bundle);
            fragment.setListener(PhoneVerificationActivity.this);
            ft.replace(R.id.fragment_container, fragment, TAG_SMS_SENT_FRAGMENT).commit();
        }
    };

    @Override
    public void smsNextButtonClicked(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
        mPhoneNumber = phoneNumber;
        showLoadingSnackbar("Sending");
    }

    @Override
    public void smsCodeButtonClicked(String code) {
        showLoadingSnackbar("Verifying");
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    @Override
    public void smsResendButtonClicked(String phoneNumber) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SmsPhoneFragment fragment =  SmsPhoneFragment.newInstance();
        fragment.setListener(this);
        ft.replace(R.id.fragment_container, fragment, TAG_SMS_PHONE_FRAGMENT).commit();
    }

    public void showLoadingSnackbar(String message){
        if(mSnackbar != null){
            mSnackbar.dismiss();
        }
        View coordinateLayout = getWindow().getDecorView().findViewById(android.R.id.content);
        mSnackbar = Snackbar.make(coordinateLayout, message, Snackbar.LENGTH_INDEFINITE);
        ViewGroup contentLay = (ViewGroup) mSnackbar.getView().findViewById(R.id.snackbar_text).getParent();
        ProgressBar progress = new ProgressBar(this);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        int margin = Utils.dpToPx(8);
        params.setMargins(margin,margin,margin,margin);
        progress.setLayoutParams(params);
        contentLay.addView(progress,0);
        int loadingPadding = Utils.dpToPx(4);
        contentLay.setPadding(loadingPadding,loadingPadding,loadingPadding,loadingPadding);
        mSnackbar.show();
    }

    public void dismissSnackbar(){
        if(mSnackbar != null){
            mSnackbar.dismiss();
        }
    }

    private void showErrorSnackbar(String message){
        if(mSnackbar != null){
            mSnackbar.dismiss();
        }
        View coordinateLayout = getWindow().getDecorView().findViewById(android.R.id.content);
        mSnackbar = Snackbar.make(coordinateLayout, message, Snackbar.LENGTH_LONG);
        TextView textView = mSnackbar.getView().findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        mSnackbar.getView().setBackgroundColor(Color.RED);
        mSnackbar.show();
    }
}
