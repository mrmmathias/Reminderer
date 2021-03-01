package com.roimaa.reminderer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.roimaa.reminderer.DB.Reminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> implements Filterable {
    private Context mContext;
    private List<Reminder> mReminderList;
    private List<Reminder> mReminderListFiltered;
    private RedminderDeleteCb mDeleteCb;

    public ReminderAdapter(Context context, List<Reminder> reminderList, RedminderDeleteCb deleteCb) {
        this.mContext = context;
        this.mReminderList = reminderList;
        this.mReminderListFiltered = this.mReminderList;
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
        Reminder r = mReminderListFiltered.get(position);
        holder.message.setText(r.getMessage());

        Date reminderTime = r.getReminderTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(reminderTime);
        String formattedTime = new SimpleDateFormat("dd.MM.yyyy H:mm").format(calendar.getTime());
        holder.reminderTime.setText(formattedTime);

        holder.delete.setOnClickListener(v -> {
            mReminderListFiltered.remove(mReminderListFiltered.get(position));
            notifyDataSetChanged();
            mDeleteCb.deleteReminder(r.getId());
        });
    }

    @Override
    public int getItemCount() {
        return mReminderListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d("FilterResults", "performFiltering: " + constraint);
                FilterResults filterResults = new FilterResults();
                List<Reminder> resultsModel = new ArrayList<>();

                if (null == constraint) {
                    filterResults.count = mReminderList.size();
                    filterResults.values = mReminderList;
                } else {
                    Date now = new Date();
                    for (Reminder reminder : mReminderList) {
                        Log.d("Date", "Date: " + reminder.getReminderTime());
                        if (reminder.getReminderTime().before(now))
                            resultsModel.add(reminder);
                    }
                    filterResults.count = resultsModel.size();
                    filterResults.values = resultsModel;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mReminderListFiltered = (List<Reminder>) results.values;
                notifyDataSetChanged();
            }
        };

        return filter;
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
