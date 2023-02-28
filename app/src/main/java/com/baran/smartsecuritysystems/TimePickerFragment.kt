package com.baran.smartsecuritysystems

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.util.*
import kotlin.concurrent.thread

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        CameraActivity.timeDif=getTimeDifMilis(hourOfDay,minute)
        Toast.makeText(activity,"Timer Set -> ${CameraActivity.timeDif/(60*1000)} min. later",Toast.LENGTH_LONG).show()
        CameraActivity.pressed=1
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        CameraActivity.pressed=0
    }
    private fun getTimeDifMilis(hourOfDay: Int, minute: Int): Long {
        val c = Calendar.getInstance()
        val currHour = c.get(Calendar.HOUR_OF_DAY)
        val currMin = c.get(Calendar.MINUTE)
        val current= currHour*60+currMin
        val total=hourOfDay*60+minute
        return if (total-current<=0){
            (((total-current)+(24*60))*60*1000).toLong()
        }else
            ((total-current)*60*1000).toLong()

    }
}