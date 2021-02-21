package com.roimaa.reminderer;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.roimaa.reminderer.DB.Reminder;
import com.roimaa.reminderer.DB.RemindererDataBase;
import com.roimaa.reminderer.DB.User;

import java.util.List;

public class DBHelper {
    private static final String TAG = DBHelper.class.getSimpleName();
    private static DBHelper mInstance;
    private final Context mContext;

    private RemindererDataBase db;

    public static DBHelper getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new DBHelper(context);
        }
        return mInstance;
    }

    private DBHelper(Context context) {
        mContext = context;
        db = Room.databaseBuilder(mContext, RemindererDataBase.class, "reminderer").build();
    }

    public void createUser(String user) {
        if (null == user || user.isEmpty())
            return;

        User newUser = new User();
        newUser.setUserName(user);
        
        class CreateUser extends AsyncTask<Void, Void, Void> {
            private User newUser;

            public CreateUser(User newUser) {
                this.newUser = newUser;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                db.userDao().insert(newUser);
                return null;
            }
        }

        CreateUser st = new CreateUser(newUser);
        st.execute();
    }

    public User getUser(String userName) {
        return db.userDao().findByName(userName);
    }

    public List<Reminder> getUserReminders(String user) {
        User wantedUser = getUser(user);
        if (null == wantedUser) return null;
        return db.reminderDao().getUserReminders(wantedUser.getUid());
    }

    public void addReminder(Reminder toAdd) {
        class AddReminder extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                db.reminderDao().insert(toAdd);
                return null;
            }
        }

        if (null != toAdd) {
            AddReminder ar = new AddReminder();
            ar.execute();
        }
    }

    public void deleteReminder(int id) {
        class DeleteReminder extends AsyncTask<Void, Void, Void> {
             @Override
            protected Void doInBackground(Void... voids) {
                db.reminderDao().delete(db.reminderDao().getById(id));
                return null;
            }
        }

        DeleteReminder ar = new DeleteReminder();
        ar.execute();
    }
}
