package com.roimaa.reminderer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.roimaa.reminderer.DB.Reminder;
import com.roimaa.reminderer.DB.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.view.MotionEvent.ACTION_UP;

public class AddReminder extends AppCompatActivity implements DatePicker.DatePickedListener, TimePicker.TimePickedListener {
    private final static String TAG = AddReminder.class.getSimpleName();
    private final static int LOGIN_RESULT = 123;
    private final static int LOCATION_RESULT = 124;

    private TextView mEditMessage;
    private TextView mEditDate;
    private TextView mEditTime;
    private ImageButton mPickLocation;
    private Button mButton;
    private CheckBox mRemindMe;
    private double mLatitude = -1;
    private double mLongitude = -1;
    private int mReminderId;

    @SuppressLint({"ClickableViewAccessibility", "SimpleDateFormat"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startIntent = getIntent();
        mReminderId = startIntent.getIntExtra("reminderId", -1);

        setContentView(R.layout.activity_add_reminder);

        mEditMessage = findViewById(R.id.editMessage);
        mEditDate = findViewById(R.id.editTextDate);
        mEditTime = findViewById(R.id.editTextTime);
        mPickLocation = findViewById(R.id.pickLocation);
        mRemindMe = findViewById(R.id.remindMe);
        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(createReminder);

        setHintTexts();

        mEditDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (ACTION_UP == event.getActionMasked()) {
                    Log.w(TAG, "DateEdit");
                    Bundle args = new Bundle();
                    args.putString("highlight", mEditDate.getHint().toString());
                    DatePicker newFragment = new DatePicker();
                    newFragment.setArguments(args);
                    newFragment.show(getFragmentManager(), "datePicker");
                    return true;
                }
                return false;
            }
        });

        mEditTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (ACTION_UP == event.getActionMasked()) {
                    Log.w(TAG, "TimeEdit");
                    Bundle args = new Bundle();
                    args.putString("highlight", mEditTime.getHint().toString());
                    TimePicker newFragment = new TimePicker();
                    newFragment.setArguments(args);
                    newFragment.show(getFragmentManager(), "timePicker");
                    return true;
                }
                return false;
            }
        });

        mPickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickLocation = new Intent(AddReminder.this, LocationActivity.class);
                pickLocation.putExtra("pickLocation", true);
                startActivityForResult(pickLocation, LOCATION_RESULT);
            }
        });
    }

    @Override
    protected void onResume () {
        super.onResume();

        if (PrefUtils.getString(this, PrefUtils.LOGGED_USER).isEmpty()) {
            // User has logged out -> password needed
            Intent loginscreen = new Intent(this, LoginActivity.class);
            loginscreen.putExtra("reminderId", mReminderId);
            startActivityForResult(loginscreen, LOGIN_RESULT);
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w(TAG, "result received: " + requestCode);

        switch (requestCode) {
            case LOGIN_RESULT:
                if (RESULT_OK == resultCode) {
                    mReminderId = data.getIntExtra("reminderId", -1);
                    Log.w(TAG, "mReminderId: " + mReminderId);
                } else {
                    this.finish();
                }
                break;

            case LOCATION_RESULT:
                if (RESULT_OK == resultCode) {
                    mLatitude = data.getDoubleExtra("latitude", -1);
                    mLongitude = data.getDoubleExtra("longitude", -1);
                    Log.d(TAG, "Location set: " + mLatitude + " " + mLongitude);
                }
                break;

            default:
                break;
        }
    }

    private void setHintTexts() {
        if (-1 == mReminderId) {
            Calendar calendar = Calendar.getInstance();
            mEditDate.setHint(new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime()));
            mEditTime.setHint(new SimpleDateFormat("H:mm").format(calendar.getTime()));
        } else {
            Reminder toEdit = DBHelper.getInstance(getApplicationContext()).getReminder(mReminderId);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(toEdit.getReminderTime());
            mEditDate.setHint(new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime()));
            mEditTime.setHint(new SimpleDateFormat("H:mm").format(calendar.getTime()));
            mEditMessage.setText(toEdit.getMessage());
            mRemindMe.setChecked(toEdit.isRemind());
            mButton.setText(R.string.save);
        }
    }

    private boolean checkValid() {
        boolean messageValid = !mEditMessage.getText().toString().isEmpty();
        boolean timeValid = !(mEditDate.getCurrentHintTextColor() == getColor(R.color.red));
        timeValid &= !(mEditTime.getCurrentHintTextColor() == getColor(R.color.red));
        boolean locationValid = (mLatitude != -1);
        boolean valid = messageValid && (timeValid || locationValid);

        if (!valid) {
            Toast.makeText(getApplicationContext(), R.string.reminder_not_valid, Toast.LENGTH_SHORT).show();
        }
        return valid;
    }

    private View.OnClickListener createReminder = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!checkValid()) return;

            final Calendar c = Calendar.getInstance();
            final Calendar d = Calendar.getInstance();

            User who = DBHelper.getInstance(getApplicationContext()).getUser(PrefUtils.getString(
                    getApplicationContext(), PrefUtils.LOGGED_USER));

            if (!mEditTime.getText().toString().isEmpty()) {
                String time = mEditTime.getText().toString();
                String[] separated = time.split(":");
                int hours = Integer.parseInt(separated[0]);
                int mins = Integer.parseInt(separated[1]);

                try {
                    c.setTime(new SimpleDateFormat("dd.MM.yyyy").parse(mEditDate.getHint().toString()));
                } catch (ParseException e) {

                }
                d.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), hours, mins);
            }

            if (-1 == mReminderId) {
                Reminder toAdd = new Reminder();
                toAdd.setUserId(who.getUid());
                toAdd.setMessage(mEditMessage.getText().toString());
                toAdd.setCreationTime(new Date());
                if (!mEditTime.getText().toString().isEmpty()) {
                    toAdd.setReminderTime(d.getTime());
                }
                toAdd.setLat(mLatitude);
                toAdd.setLon(mLongitude);
                toAdd.setRemind(mRemindMe.isChecked());
                DBHelper.getInstance(getApplicationContext()).addReminder(toAdd);
            } else {
                Reminder toEdit = DBHelper.getInstance(getApplicationContext()).getReminder(mReminderId);
                toEdit.setMessage(mEditMessage.getText().toString());
                if (!mEditTime.getText().toString().isEmpty()) {
                    toEdit.setReminderTime(d.getTime());
                }
                toEdit.setLat(mLatitude);
                toEdit.setLon(mLongitude);
                toEdit.setRemind(mRemindMe.isChecked());
                DBHelper.getInstance(getApplicationContext()).updateReminder(toEdit);
            }
            AddReminder.this.finish();
        }
    };

    @Override
    public void onDatePicked(int selectedYear, int selectedMonth, int selectedDay) {
        Calendar c = Calendar.getInstance();
        c.set(selectedYear, selectedMonth, selectedDay);
        mEditDate.setText(new SimpleDateFormat("dd.MM.yyyy").format(c.getTime()));

        if (c.compareTo(Calendar.getInstance()) < 0) {
            mEditDate.setTextColor(getColor(R.color.red));
        } else {
            mEditDate.setTextColor(getColor(R.color.black));
            mEditTime.setTextColor(getColor(R.color.black));
        }
    }

    @Override
    public void onTimePicked(int hourOfDay, int minute) {
        String newTime = String.valueOf(hourOfDay) + ":" + String.valueOf(minute);
        mEditTime.setText(newTime);

        final Calendar c = Calendar.getInstance();
        final Calendar d = Calendar.getInstance();
        try {
            c.setTime(new SimpleDateFormat("dd.MM.yyyy").parse(mEditDate.getHint().toString()));
        } catch (ParseException e) {

        }
        d.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), hourOfDay, minute);

        if (d.compareTo(Calendar.getInstance()) < 0) {
            mEditTime.setTextColor(getColor(R.color.red));
        } else {
            mEditTime.setTextColor(getColor(R.color.black));
        }
    }
}