package com.roimaa.reminderer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.roimaa.reminderer.DB.Reminder;

import java.util.Date;
import java.util.List;


public class MainFragment extends Fragment implements RedminderDeleteCb {
    private static final String TAG = MainFragment.class.getSimpleName();
    private TextView mText;
    private RecyclerView mReminderRecylerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mText = view.findViewById(R.id.textview_hello);
        mReminderRecylerView = view.findViewById(R.id.recyleViewReminders);
        mReminderRecylerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Launch create reminder
                Reminder tempReminder = new Reminder();
                tempReminder.setUserId(1);
                tempReminder.setMessage("Temp Reminder");
                tempReminder.setReminderTime(new Date());
                DBHelper.getInstance(getContext()).addReminder(tempReminder);
                update();
            }
        });

        update();
    }

    public void update() {
        Log.d(TAG, "update()");
        getReminders(PrefUtils.getString(getContext(), PrefUtils.LOGGED_USER));
    }

    private void getReminders(String user) {
        class GetReminders extends AsyncTask<Void, Void, List<Reminder>> {

            @Override
            protected List<Reminder> doInBackground(Void... voids) {
                return DBHelper.getInstance(getContext()).getUserReminders(user);
            }

            @Override
            protected void onPostExecute(List<Reminder> reminders) {
                super.onPostExecute(reminders);
                ReminderAdapter adapter = new ReminderAdapter(getContext(), reminders,
                        MainFragment.this::deleteReminder);
                mReminderRecylerView.setAdapter(adapter);
            }
        }

        GetReminders gr = new GetReminders();
        gr.execute();
    }

    @Override
    public void deleteReminder(int id) {
        DBHelper.getInstance(getContext()).deleteReminder(id);
        update();
    }
}