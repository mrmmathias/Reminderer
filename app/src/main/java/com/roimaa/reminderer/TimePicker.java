package com.roimaa.reminderer;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private TimePicker.TimePickedListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        Bundle args = getArguments();
        String highlight = args.getString("highlight");
        try {
            c.setTime(new SimpleDateFormat("H.mm").parse(highlight));
        } catch (ParseException e) {

        }
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }


    @Override
    public void onAttach(Activity activity)
    {
        // when the fragment is initially attached to the activity,
        // cast the activity to the callback interface type
        super.onAttach(activity);

        try
        {
            listener = (TimePicker.TimePickedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() +
                    " must implement " + TimePicker.TimePickedListener.class.getName());
        }
    }

    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        listener.onTimePicked(hourOfDay, minute);
    }

    public static interface TimePickedListener
    {
        public void onTimePicked(int hourOfDay, int minute);
    }
}
