package com.roimaa.reminderer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

    private TextView mEditMessage;
    private TextView mEditDate;
    private TextView mEditTime;
    private Button mButton;
    private CheckBox mRemindMe;
    private int mReminderId;

    @SuppressLint({"ClickableViewAccessibility", "SimpleDateFormat"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        mEditMessage = findViewById(R.id.editMessage);
        mEditDate = findViewById(R.id.editTextDate);
        mEditTime = findViewById(R.id.editTextTime);
        mRemindMe = findViewById(R.id.remindMe);
        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(createReminder);

        Intent startIntent = getIntent();
        mReminderId = startIntent.getIntExtra("reminderId", -1);

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
        boolean valid = !mEditMessage.getText().toString().isEmpty();
        valid &= !(mEditDate.getCurrentHintTextColor() == getColor(R.color.red));
        valid &= !(mEditTime.getCurrentHintTextColor() == getColor(R.color.red));

        if (!valid) {
            Toast.makeText(getApplicationContext(), R.string.reminder_not_valid, Toast.LENGTH_SHORT).show();
        }
        return valid;
    }

    private View.OnClickListener createReminder = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!checkValid()) return;

            User who = DBHelper.getInstance(getApplicationContext()).getUser(PrefUtils.getString(
                    getApplicationContext(), PrefUtils.LOGGED_USER));

            String time = mEditTime.getHint().toString();
            String[] separated = time.split(":");
            int hours = Integer.parseInt(separated[0]);
            int mins = Integer.parseInt(separated[1]);

            final Calendar c = Calendar.getInstance();
            final Calendar d = Calendar.getInstance();
            try {
                c.setTime(new SimpleDateFormat("dd.MM.yyyy").parse(mEditDate.getHint().toString()));
            } catch (ParseException e) {

            }
            d.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), hours, mins);


            if (-1 == mReminderId) {
                Reminder toAdd = new Reminder();
                toAdd.setUserId(who.getUid());
                toAdd.setMessage(mEditMessage.getText().toString());
                toAdd.setCreationTime(new Date());
                toAdd.setReminderTime(d.getTime());
                toAdd.setRemind(mRemindMe.isChecked());
                DBHelper.getInstance(getApplicationContext()).addReminder(toAdd);
            } else {
                Reminder toEdit = DBHelper.getInstance(getApplicationContext()).getReminder(mReminderId);
                toEdit.setMessage(mEditMessage.getText().toString());
                toEdit.setReminderTime(d.getTime());
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
        mEditDate.setHint(new SimpleDateFormat("dd.MM.yyyy").format(c.getTime()));

        if (c.compareTo(Calendar.getInstance()) < 0) {
            mEditDate.setHintTextColor(getColor(R.color.red));
        } else {
            mEditDate.setHintTextColor(getColor(R.color.hint));
            mEditTime.setHintTextColor(getColor(R.color.hint));
        }
    }

    @Override
    public void onTimePicked(int hourOfDay, int minute) {
        String newTime = String.valueOf(hourOfDay) + ":" + String.valueOf(minute);
        mEditTime.setHint(newTime);

        final Calendar c = Calendar.getInstance();
        final Calendar d = Calendar.getInstance();
        try {
            c.setTime(new SimpleDateFormat("dd.MM.yyyy").parse(mEditDate.getHint().toString()));
        } catch (ParseException e) {

        }
        d.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), hourOfDay, minute);

        if (d.compareTo(Calendar.getInstance()) < 0) {
            mEditTime.setHintTextColor(getColor(R.color.red));
        } else {
            mEditTime.setHintTextColor(getColor(R.color.hint));
        }
    }
}