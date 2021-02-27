package com.roimaa.reminderer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.roimaa.reminderer.DB.Reminder;
import com.roimaa.reminderer.DB.RemindererDataBase;
import com.roimaa.reminderer.DB.User;

import java.util.Date;
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
        db = Room.databaseBuilder(mContext, RemindererDataBase.class, "reminderer")
                .addMigrations(MIGRATION_1_2).allowMainThreadQueries().build();
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'Reminder' ADD 'remind' INTEGER DEFAULT 0 NOT NULL");
        }
    };


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

    public Reminder getReminder(int Id) {
        return db.reminderDao().getById(Id);
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
                long newId = db.reminderDao().insert(toAdd);

                if (toAdd.isRemind()) {
                    Intent intent = new Intent(mContext, WorkManagerService.class);
                    intent.putExtra(WorkManagerService.Id, (int)newId);
                    intent.putExtra(WorkManagerService.Action, WorkManagerService.ADD);
                    mContext.startService(intent);
                }

                return null;
            }
        }

        if (null != toAdd) {
            AddReminder ar = new AddReminder();
            ar.execute();
        }
    }

    public void updateReminder(Reminder toEdit) {
        class EditReminder extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                Reminder before = db.reminderDao().getById(toEdit.getId());
                boolean wasRemind = before.isRemind();
                Date wasDate = before.getReminderTime();
                db.reminderDao().update(toEdit);

                if (wasRemind != toEdit.isRemind()) {
                    // Remind status has changed
                    Intent intent = new Intent(mContext, WorkManagerService.class);
                    intent.putExtra(WorkManagerService.Id, toEdit.getId());
                    intent.putExtra(WorkManagerService.Action, toEdit.isRemind() ?
                            WorkManagerService.ADD : WorkManagerService.REMOVE);
                    mContext.startService(intent);
                } else if (!wasDate.equals(toEdit.getReminderTime()) && toEdit.isRemind()) {
                    // Reminder time has changed.
                    Intent intent = new Intent(mContext, WorkManagerService.class);
                    intent.putExtra(WorkManagerService.Id, toEdit.getId());
                    intent.putExtra(WorkManagerService.Action, WorkManagerService.UPDATE);
                    mContext.startService(intent);
                }
                return null;
            }
        }

        if (null != toEdit) {
            EditReminder ar = new EditReminder();
            ar.execute();
        }
    }

    public void deleteReminder(int id) {
        class DeleteReminder extends AsyncTask<Void, Void, Void> {
             @Override
            protected Void doInBackground(Void... voids) {
                Reminder removed = db.reminderDao().getById(id);
                boolean wasReminder = removed.isRemind();
                db.reminderDao().delete(db.reminderDao().getById(id));

                if (wasReminder) {
                    // Remove worker item as well
                    Intent intent = new Intent(mContext, WorkManagerService.class);
                    intent.putExtra(WorkManagerService.Id, id);
                    intent.putExtra(WorkManagerService.Action, WorkManagerService.REMOVE);
                    mContext.startService(intent);
                }
                return null;
            }
        }

        DeleteReminder ar = new DeleteReminder();
        ar.execute();
    }
}
