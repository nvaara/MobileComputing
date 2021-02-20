package com.example.mobilecomputing

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.room.Update
import java.time.LocalTime

class ReminderEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_edit)
        val editButton = findViewById<Button>(R.id.reminder_edit_button)
        val deleteButton = findViewById<Button>(R.id.reminder_edit_delete_button)
        val goBackButton = findViewById<Button>(R.id.reminder_edit_go_back)


        editButton.setOnClickListener{
            val updater = UpdateReminder()
            updater.execute()
        }

        deleteButton.setOnClickListener{
            val deleter = DeleteReminder()
            deleter.execute()
        }

        goBackButton.setOnClickListener{
            finish()
        }

        loadReminderData();
    }

    private fun loadReminderData() {
        val load = LoadReminder()
        load.execute()
    }

    private fun validateString(str : String, error : String) : Boolean {
        if (str.isEmpty()) {
            Toast.makeText(this, "Title is empty!", Toast.LENGTH_LONG).show()
        }
        return str.isNotEmpty()
    }

    private fun updatedReminderData(oldReminderData: ReminderData) : Boolean {
        val title = findViewById<TextView>(R.id.text_reminder_edit_title).text.toString()
        val msg = findViewById<TextView>(R.id.text_reminder_edit_message).text.toString()
        val locX = findViewById<TextView>(R.id.text_reminder_edit_loc_x).text.toString()
        val locY = findViewById<TextView>(R.id.text_reminder_edit_loc_y).text.toString()
        val time = findViewById<TextView>(R.id.text_reminder_edit_time).text.toString()

        if (validateString(title, "Title is empty")
            && validateString(title, "Message is empty")
            && validateString(locX, "Location X is empty")
            && validateString(locY, "Location Y is empty")
            && validateString(time, "Time is empty")) {

            oldReminderData.title = title
            oldReminderData.message = msg
            oldReminderData.locationX = locX
            oldReminderData.locationY = locY
            oldReminderData.reminderTime = time
            return true
        }
        return false
    }

    inner class LoadReminder : AsyncTask<String?, String?, ReminderData>() {
        override fun doInBackground(vararg params: String?): ReminderData {
            val db = getReminderDb(applicationContext)
            val dao = db.reminderDao()
            val reminder = dao.getReminder(intent?.extras?.get("uid").toString().toInt())
            db.close()
            return reminder
        }

        override fun onPostExecute(result: ReminderData?) {
            super.onPostExecute(result)
            if (result != null) {
                findViewById<TextView>(R.id.text_reminder_edit_title).text = result.title
                findViewById<TextView>(R.id.text_reminder_edit_message).text = result.message
                findViewById<TextView>(R.id.text_reminder_edit_loc_x).text = result.locationX
                findViewById<TextView>(R.id.text_reminder_edit_loc_y).text = result.locationY
                findViewById<TextView>(R.id.text_reminder_edit_time).text = result.reminderTime
            }
        }
    }

    inner class DeleteReminder : AsyncTask<String?, String?, Boolean>() {
        override fun doInBackground(vararg params: String?) : Boolean {
            val db = getReminderDb(applicationContext)
            val dao = db.reminderDao()
            val reminder = dao.getReminder(intent?.extras?.get("uid").toString().toInt())
            dao.remove((reminder.uid!!))
            db.close()
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            finish()
        }
    }

    inner class UpdateReminder : AsyncTask<String?, String?, Boolean>() {
        override fun doInBackground(vararg params: String?) : Boolean {
            val db = getReminderDb(applicationContext)
            val dao = db.reminderDao()
            var reminder = dao.getReminder(intent?.extras?.get("uid").toString().toInt())
            if (updatedReminderData(reminder)) {
                dao.update(reminder)
            }
            db.close()
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            finish()
        }
    }
}