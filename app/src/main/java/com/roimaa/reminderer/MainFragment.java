package com.roimaa.reminderer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.roimaa.reminderer.DB.Reminder;

import java.util.Date;
import java.util.List;


public class MainFragment extends Fragment implements RedminderDeleteCb {
    private static final String TAG = MainFragment.class.getSimpleName();
    private RecyclerView mReminderRecylerView;
    private CoordinatorLayout myCoordinatorLayout;
    private ReminderAdapter mAdapter;
    private SwipeRefreshLayout mSwipeContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myCoordinatorLayout = view.findViewById(R.id.myCoordinatorLayout);
        mSwipeContainer = view.findViewById(R.id.swiperefresh);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.getFilter().filter(null);
                mSwipeContainer.setRefreshing(false);
            }
        });

        mReminderRecylerView = view.findViewById(R.id.recyleViewReminders);
        mReminderRecylerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddReminder.class);
                getContext().startActivity(intent);
            }
        });

        update();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    public void update() {
        Log.w(TAG, "update()");
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
                mAdapter = new ReminderAdapter(getContext(), reminders,
                        MainFragment.this::deleteReminder);
                mReminderRecylerView.setAdapter(mAdapter);
                mAdapter.getFilter().filter((new Date()).toString());
            }
        }

        GetReminders gr = new GetReminders();
        gr.execute();
    }

    @Override
    public void deleteReminder(int id) {
        Snackbar undoBar =
        Snackbar.make(myCoordinatorLayout, R.string.reminder_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        update();
                    }
                });

        ((BaseTransientBottomBar)undoBar).addCallback(new BaseTransientBottomBar.BaseCallback() {
            @Override
            public void onDismissed(Object transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (DISMISS_EVENT_ACTION != event) {
                    // Undo not pressed -> remove for good
                    DBHelper.getInstance(getContext()).deleteReminder(id);
                }
            }
        });

        undoBar.show();
    }
}