package com.roimaa.reminderer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

    private EditText mUserName;
    private EditText mPassword;
    private CheckBox mRemember;
    private TextView mNewAccount;
    private TextView mWrongPassword;
    private AlertDialog mNewAccountDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PrefUtils.getBoolean(getApplicationContext(), PrefUtils.REMEMBER_LOGIN)) {
            launchMain();
        }
        setContentView(R.layout.activity_login);

        mUserName = findViewById(R.id.username);
        mPassword = findViewById(R.id.password);
        mRemember = findViewById(R.id.rememberme);
        mWrongPassword = findViewById(R.id.not_authenticated);
        mNewAccount = findViewById(R.id.new_account);
        mNewAccount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (null == mNewAccountDialog) {
                    mNewAccountDialog = newAccount();
                    mNewAccountDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mNewAccountDialog = null;
                        }
                    });
                }
                return true;
            }
        });
    }

    public void login(View view) {
        mWrongPassword.setVisibility(View.INVISIBLE);
        String username = mUserName.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (username.isEmpty()) {
            mUserName.setHintTextColor(getResources().getColor(R.color.red, null));
            return;
        }

        if (password.isEmpty()) {
            mPassword.setHintTextColor(getResources().getColor(R.color.red, null));
            return;
        }

        if (KeyStoreHelper.getInstance(getApplicationContext()).authenticateUser(username, password)) {
            if (mRemember.isChecked()) {
                PrefUtils.putBoolean(getApplicationContext(), PrefUtils.REMEMBER_LOGIN, true);
            }
            PrefUtils.putString(getApplicationContext(), PrefUtils.LOGGED_USER, username);
            launchMain();
        } else {
            mWrongPassword.setVisibility(View.VISIBLE);
        }
    }

    public AlertDialog newAccount() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View viewInflated = LayoutInflater.from(getApplicationContext()).inflate(R.layout.new_account_dialog, null, false);
        final EditText newUsername = viewInflated.findViewById(R.id.new_account_name);
        final EditText newPassword = viewInflated.findViewById(R.id.new_account_new_password);
        alert.setView(viewInflated);
        alert.setCustomTitle(LayoutInflater.from(getApplicationContext()).inflate(R.layout.new_account_title, null, false));

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String user = newUsername.getText().toString().trim();
                String password = newPassword.getText().toString().trim();

                if (!user.isEmpty() && !password.isEmpty()) {
                    if (true == KeyStoreHelper.getInstance(getApplicationContext()).createNewAccount(user, password)) {
                        DBHelper.getInstance(getApplicationContext()).createUser(user);
                    }
                }
                dialog.cancel();
            }
        });

        alert.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
        });

        alert.create();
        return alert.show();
    }

    private void launchMain() {
        Intent goToNextActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(goToNextActivity);
    }
}
