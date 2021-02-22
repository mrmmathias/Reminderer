package com.roimaa.reminderer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roimaa.reminderer.DB.Reminder;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private Context mContext;
    private List<Reminder> mReminderList;
    private RedminderDeleteCb mDeleteCb;

    public ReminderAdapter(Context context, List<Reminder> reminderList, RedminderDeleteCb deleteCb) {
        this.mContext = context;
        this.mReminderList = reminderList;
        this.mDeleteCb = deleteCb;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder r = mReminderList.get(position);
        holder.message.setText(r.getMessage());
        holder.reminderTime.setText(r.getReminderTime().toString());

        holder.delete.setOnClickListener(v -> {
            mDeleteCb.deleteReminder(r.getId());
        });


    }

    @Override
    public int getItemCount() {
        return mReminderList.size();
    }

    class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView message, reminderTime;
        public ImageButton delete;

        public ReminderViewHolder(View reminder) {
            super(reminder);

            message = itemView.findViewById(R.id.message);
            reminderTime = itemView.findViewById(R.id.reminder_time);
            delete = itemView.findViewById(R.id.delete);
            reminder.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Reminder reminder = mReminderList.get(getAdapterPosition());
            Intent intent = new Intent(mContext, AddReminder.class);
            intent.putExtra("reminderId", reminder.getId());
            mContext.startActivity(intent);
        }
    }
}
