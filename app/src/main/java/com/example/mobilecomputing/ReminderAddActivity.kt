package com.example.mobilecomputing

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ReminderAddActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var reminderCalendar: Calendar
    private lateinit var textView : TextView

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        reminderCalendar.set(Calendar.YEAR, year)
        reminderCalendar.set(Calendar.MONTH, month)
        reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        TimePickerDialog(this, this,
                reminderCalendar.get(Calendar.HOUR_OF_DAY),
                reminderCalendar.get(Calendar.MINUTE),
                true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        reminderCalendar.set(Calendar.MINUTE, minute)
        reminderCalendar.set(Calendar.SECOND, 0)
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyy HH:mm")
        textView.text = simpleDateFormat.format(reminderCalendar.time)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_add)

        val addReminderButton = findViewById<Button>(R.id.reminder_add_confirm)
        val goBackButton = findViewById<Button>(R.id.reminder_add_go_back)
        textView = findViewById<TextView>(R.id.text_reminder_time)

        textView.setOnClickListener{
            reminderCalendar = GregorianCalendar.getInstance()
            DatePickerDialog(this, this,
                    reminderCalendar.get(Calendar.YEAR),
                    reminderCalendar.get(Calendar.MONTH),
                    reminderCalendar.get(Calendar.DAY_OF_MONTH)).show()

        }

        goBackButton.setOnClickListener{
            finish()
        }

        addReminderButton.setOnClickListener{
            val reminderData = tryConstructReminderData()

            if (reminderData != null) {
                AsyncTask.execute {
                    val db = getReminderDb(applicationContext)
                    val uid = db.reminderDao().insert(reminderData).toInt()
                    ReminderNotification(applicationContext, uid, reminderCalendar.timeInMillis, reminderData.message)
                    db.close()
                    addReminderButton.post{
                        finish()
                    }
                }
            }
        }
    }

    private fun validateString(str : String, error : String) : Boolean {
        if (str.isEmpty()) {
            Toast.makeText(this, "Title is empty!", Toast.LENGTH_LONG).show()
        }
        return str.isNotEmpty()
    }

    private fun tryConstructReminderData() : ReminderData? {
        val title = findViewById<TextView>(R.id.text_reminder_title).text.toString()
        val msg = findViewById<TextView>(R.id.text_reminder_message).text.toString()
        val locX = findViewById<TextView>(R.id.text_reminder_loc_x).text.toString()
        val locY = findViewById<TextView>(R.id.text_reminder_loc_y).text.toString()

        if (validateString(title, "Title is empty")
            && validateString(title, "Message is empty")
            && validateString(locX, "Location X is empty")
            && validateString(locY, "Location Y is empty")
            && this::reminderCalendar.isInitialized) {

            val username = intent?.extras?.get("username").toString()
            val creationTime = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", java.util.Date())

            return ReminderData(null,
                    title,
                    msg,
                    locX,
                    locY,
                    textView.text.toString(),
                    creationTime.toString(),
                    username,
                    reminderSeen = false,
                    reminderOccurred = false)
        }
        return null
    }
}