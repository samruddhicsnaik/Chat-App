package com.example.letschat;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    onTimeSetTP callback;

    public void setOnTimeSetTPListner(onTimeSetTP callback) {
        this.callback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        Calendar rightNow = Calendar.getInstance();
        long date = rightNow.getTimeInMillis();
        int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);
        int currentMin = rightNow.get(Calendar.MINUTE);
        int currentSec = rightNow.get(Calendar.SECOND);
        long totMins=(hourOfDay*60 + minute) - (currentHourIn24Format*60 +currentMin);
        //callback.onTimeSetTP(totMins,""+hourOfDay +":"+minute);
        callback.onTimeSetTP(totMins, Long.toString(date), ""+hourOfDay +":"+minute);
    }

    public interface onTimeSetTP {
        /*public void onTimeSetTP(long min);*/
        public void onTimeSetTP(long minutes, String min, String time);
    }
}