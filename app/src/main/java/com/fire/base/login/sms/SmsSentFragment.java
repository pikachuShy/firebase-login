package com.fire.base.login.sms;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.fire.base.login.R;
import com.fire.base.util.ResourcesUtil;
import com.fire.base.util.Utils;

import java.util.Locale;
import java.util.Objects;

public class SmsSentFragment extends Fragment {

    private Listener mListener;
    private CountDownTimer cTimer = null;
    private LinearLayout mCodeContainer;


    public static SmsSentFragment newInstance(Bundle args) {
        if(args == null) args = new Bundle();

        SmsSentFragment fragment = new SmsSentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms_sent, container, false);
        TextView phoneNumber = view.findViewById(R.id.phone_number);
        phoneNumber.setText(getArguments() != null ? getArguments().getString(PhoneVerificationActivity.SMS_PHONE_NUMBER) : null);
        Button codeButton = view.findViewById(R.id.codeButton);
        Button resendButton = view.findViewById(R.id.resendButton);
        mCodeContainer = view.findViewById(R.id.code_container);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f);

        int margin = Utils.dpToPx(2);
        param.setMargins(margin,margin,margin,margin);
        for(int i = 0; i<6;i++){
            EditText codeEditText = (EditText) getLayoutInflater().inflate(R.layout.layout_sms_code_edittext, null);
            codeEditText.setLayoutParams(param);
            codeEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    int code = 0;

                    if(codeEditText.getText().length() > 0){
                        View nextFocusView = codeEditText.focusSearch(View.FOCUS_RIGHT);
                        if(nextFocusView != null)
                            nextFocusView.requestFocus();
                    }
                    else{
                        View previousFocusView = codeEditText.focusSearch(View.FOCUS_LEFT);
                        if(previousFocusView != null)
                            previousFocusView.requestFocus();
                    }
                    for(int i = 0; i<mCodeContainer.getChildCount();i++){
                        EditText codeText = (EditText) mCodeContainer.getChildAt(i);
                        if(!codeText.getText().toString().equals("")){
                            code++;
                        }
                    }
                    codeButton.setEnabled(code == mCodeContainer.getChildCount());
                }
            });
            mCodeContainer.addView(codeEditText);
        }

        codeButton.setEnabled(false);
        codeButton.setOnClickListener(v -> {
            if(mListener != null){
                StringBuilder code = new StringBuilder();
                for(int i = 0; i<mCodeContainer.getChildCount();i++){
                    EditText codeText = (EditText) mCodeContainer.getChildAt(i);
                    code.append(codeText.getText().toString());
                }
                if(getActivity() != null){
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null )
                        imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
                }
                mListener.smsCodeButtonClicked(code.toString());
            }
        });

        resendButton.setOnClickListener(v -> {
            mListener.smsResendButtonClicked(getArguments() != null ? getArguments().getString(PhoneVerificationActivity.SMS_PHONE_NUMBER) : null);
        });

        cTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                String smsResend = ResourcesUtil.getString(R.string.sms_resend);
                String countdownMessage = String.format(Locale.US, "%s in %ds", smsResend, (int)(millisUntilFinished/1000));
                resendButton.setText(countdownMessage);
            }
            public void onFinish() {
                resendButton.setEnabled(true);
                String smsResend = ResourcesUtil.getString(R.string.sms_resend);
                resendButton.setText(smsResend);
            }
        };
        cTimer.start();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(cTimer!=null)
            cTimer.cancel();
    }

    public void resetCode(){
        for(int i = 0; i<mCodeContainer.getChildCount();i++){
            EditText codeText = (EditText) mCodeContainer.getChildAt(i);
            codeText.getText().clear();
            if(i == 0){
                codeText.requestFocus();
            }
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void smsCodeButtonClicked(String code);
        void smsResendButtonClicked(String phoneNumber);
    }
}
