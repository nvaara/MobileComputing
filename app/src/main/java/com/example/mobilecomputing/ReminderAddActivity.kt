package com.example.mobilecomputing

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.text.DateFormat

class ReminderAddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_add)

        val addReminderButton = findViewById<Button>(R.id.reminder_add_confirm)
        val goBackButton = findViewById<Button>(R.id.reminder_add_go_back)

        goBackButton.setOnClickListener{
            finish()
        }

        addReminderButton.setOnClickListener{
            val reminderData = tryConstructReminderData()

            if (reminderData != null) {
                AsyncTask.execute {
                    val db = getReminderDb(applicationContext)
                    val dao = db.reminderDao().insert(reminderData)
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
        val time = findViewById<TextView>(R.id.text_reminder_time).text.toString()


        if (validateString(title, "Title is empty")
            && validateString(title, "Message is empty")
            && validateString(locX, "Location X is empty")
            && validateString(locY, "Location Y is empty")
            && validateString(time, "Time is empty")) {

            val username = intent?.extras?.get("username").toString()
            val creationTime = ""
            return ReminderData(null,
                title,
                msg,
                locX,
                locY,
                time,
                creationTime,
                username,
                false)
        }
        return null
    }
}