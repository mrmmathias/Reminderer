package com.roimaa.reminderer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private DatePickedListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String highlight = args.getString("highlight");
        final Calendar c = Calendar.getInstance();
        try {
            c.setTime(new SimpleDateFormat("dd.MM.yyyy").parse(highlight));
        } catch (ParseException e) {

        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onAttach(Activity activity)
    {
        // when the fragment is initially attached to the activity,
        // cast the activity to the callback interface type
        super.onAttach(activity);

        try
        {
            listener = (DatePickedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() +
                    " must implement " + DatePickedListener.class.getName());
        }
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        listener.onDatePicked(year, month, dayOfMonth);
    }

    public static interface DatePickedListener
    {
        public void onDatePicked(int selectedYear, int selectedMonth, int selectedDay);
    }
}
