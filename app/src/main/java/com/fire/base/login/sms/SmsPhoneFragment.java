package com.fire.base.login.sms;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.fire.base.database.AppDatabase;
import com.fire.base.database.Flag;
import com.fire.base.login.R;
import com.fire.base.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SmsPhoneFragment extends Fragment implements FlagsAdapter.Listener {
    private static final String TAG = "BipBip";
    private EditText mPhoneNumber;
    private AlertDialog mFlagDialog;
    private TextView mFlag;
    private Listener mListener;
    private PhoneNumberFormattingTextWatcher mPhoneNumberListener;
    private AppDatabase flagDb;

    static SmsPhoneFragment newInstance() {

        Bundle args = new Bundle();

        SmsPhoneFragment fragment = new SmsPhoneFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms_phone, container, false);
        flagDb = Room.databaseBuilder(getContext(), AppDatabase.class, "flag-db").build();
        mPhoneNumber = view.findViewById(R.id.phoneNumber);
        mFlag = view.findViewById(R.id.phoneNumberFlag);
        TextView nextButton = view.findViewById(R.id.phoneNumberNextButton);
        mPhoneNumberListener = new PhoneNumberFormattingTextWatcher();
        mPhoneNumber.addTextChangedListener(mPhoneNumberListener);
        AsyncTask.execute(() -> {
            insertFlagDb();
            Flag row = getCode();
            if(row != null){
                mPhoneNumber.post(() -> {
                    setDialCode(row.dialCode);
                    mFlag.setText(Utils.getEmoji(row.unicode));
                    mPhoneNumber.setEnabled(true);
                });
            }
            mPhoneNumber.postDelayed(() -> {
                if(getActivity() != null && getActivity() instanceof PhoneVerificationActivity){
                    ((PhoneVerificationActivity) getActivity()).dismissSnackbar();
                }

            }, 1000);
            mPhoneNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.length() == 0){
                        if(row != null){
                            openFlagDialog(row.dialCode);
                            setDialCode(row.dialCode);
                            mFlag.setText(Utils.getEmoji(row.unicode));
                        }
                    }
                    else if(row != null && getActivity() != null){
                        getActivity().runOnUiThread(() ->
                                nextButton.setEnabled(isPhoneNumberValid(mPhoneNumber.getText().toString(), row.code)));

                        AsyncTask.execute(() -> {
                            Flag dialCodeFlag = getFlagByDialCode(mPhoneNumber.getText().toString());
                            if(dialCodeFlag != null){
                                getActivity().runOnUiThread(() ->
                                        mFlag.setText(Utils.getEmoji(dialCodeFlag.unicode)));
                            }
                        });
                    }
                }
            });

            mFlag.setOnClickListener(v -> {
                if(row != null){
                    openFlagDialog(row.dialCode);
                }
            });
        });

        nextButton.setOnClickListener(v -> {
            if(mListener != null){
                if(getActivity() != null){
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null )
                        imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
                }
                mListener.smsNextButtonClicked(mPhoneNumber.getText().toString());
            }
        });
        return view;
    }



    private void openFlagDialog(String dialCode){
        AsyncTask.execute(() -> {
            if(getContext() != null){
                List<Flag> row = flagDb.flagDao().getAll();
                RecyclerView rv = new RecyclerView(getContext());
                LinearLayoutManager lm = new LinearLayoutManager(getContext());
                rv.setLayoutManager(lm);
                Flag flag = flagDb.flagDao().findByCode(dialCode);
                rv.post(() -> {
                    if(flag != null){
                        int position = (flag.id == 0)? flag.id: flag.id - 1;
                        lm.scrollToPositionWithOffset(position, 0);
                    }
                });
                FlagsAdapter flagsAdapter = new FlagsAdapter(getContext(), row, dialCode);
                flagsAdapter.setListener(this);
                rv.setAdapter(flagsAdapter);
                AlertDialog.Builder flagDialogBuilder = new AlertDialog.Builder(getActivity());
                flagDialogBuilder.setView(rv);
                if(getActivity() != null)
                    getActivity().runOnUiThread(() ->{
                        mFlagDialog = flagDialogBuilder.create();
                        mFlagDialog.show();
                    });
            }
        });
    }

    @Override
    public void onFlagClicked(Flag flag) {
        mPhoneNumber.removeTextChangedListener(mPhoneNumberListener);
        mFlagDialog.dismiss();
        setDialCode(flag.dialCode);
        mPhoneNumberListener = new PhoneNumberFormattingTextWatcher();
        mPhoneNumber.addTextChangedListener(mPhoneNumberListener);
        mFlag.setText(Utils.getEmoji(flag.unicode));
    }

    private void setDialCode(String dialCode){
        mPhoneNumber.setText(dialCode);
        mPhoneNumber.setSelection(mPhoneNumber.getText().length());
    }

    private Flag getFlagByDialCode(String dialCode){
        Flag row;
        if(dialCode.equals("+1")){
            row = flagDb.flagDao().findByCode("US");
        }
        else{
            row = flagDb.flagDao().findByDialCode(dialCode.trim());
        }
        return row;
    }

    private Flag getCode(){
        if(getContext() != null){
            String countryCode = Utils.getCurrentCountry(getContext());
            if(countryCode != null){
                return flagDb.flagDao().findByCode(countryCode);
            }
            else{
                Flag row = flagDb.flagDao().findByCode("US");
                return row;
            }
        }

        return null;
    }

    private void insertFlagDb(){
        List<Flag> row = flagDb.flagDao().getAll();
        if(row.size() == 0){
            mPhoneNumber.post(() -> mPhoneNumber.setEnabled(false));
            FlagResponse[] response = new GsonBuilder().create().fromJson(loadJSONFromAsset(), FlagResponse[].class);
            if(getActivity() != null && getActivity() instanceof PhoneVerificationActivity){
                ((PhoneVerificationActivity) getActivity()).showLoadingSnackbar("Initializing");
            }
            for(FlagResponse flag: response){
                if(flag.code != null && flag.unicode != null && flag.name != null && flag.dialCode != null){
                    Flag db_flag = new Flag();
                    db_flag.code = flag.code;
                    db_flag.unicode = flag.unicode;
                    db_flag.name = flag.name;
                    db_flag.title = flag.title;
                    db_flag.dialCode = flag.dialCode;
                    flagDb.flagDao().insertAll(db_flag);
                }
            }
        }
    }

    private String loadJSONFromAsset() {
        String json = null;
        if(getContext() != null) {
            try {
                InputStream is = getContext().getAssets().open("flag.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    json = new String(buffer, StandardCharsets.UTF_8);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return json;
    }

    private boolean isPhoneNumberValid(String phoneNumber, String countryCode) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try
        {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, countryCode);
            return phoneUtil.isValidNumber(numberProto);
        }
        catch (NumberParseException e)
        {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        return false;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }



    public interface Listener {
        void smsNextButtonClicked(String phoneNumber);
    }

}
