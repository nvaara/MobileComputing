package com.example.mobilecomputing

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast

class ReminderView : AppCompatActivity() {

    lateinit var listView : ListView
    lateinit var adapter : ArrayAdapter<String>
    lateinit var username : String
    private var list : MutableList<String> = mutableListOf()
    private var reminderList : MutableList<ReminderData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_view)

        listView = findViewById(R.id.reminder_list_view)
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list)
        listView.adapter = adapter
        username = intent?.extras?.get("username").toString()

        val goBackButton = findViewById<Button>(R.id.reminder_view_go_back)
        goBackButton.setOnClickListener{
            finish()
        }

        val updateButton = findViewById<Button>(R.id.reminder_view_update)
        updateButton.setOnClickListener{
            loadReminders()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val reminder = reminderList[position]
            if (reminder.reminderOccurred) {
                var intent = Intent(this, ReminderEditActivity::class.java)
                intent.putExtra("uid", reminder.uid)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Reminder is still active!", Toast.LENGTH_LONG).show()
            }
        }
        loadReminders()
    }

    private fun loadReminders() {
        val load = LoadReminders()
        load.execute()
    }

    override fun onResume() {
        super.onResume()
        loadReminders()
    }

    inner class LoadReminders : AsyncTask<String?, String?, MutableList<ReminderData>>() {
        override fun doInBackground(vararg params: String?): MutableList<ReminderData> {
            val db = getReminderDb(applicationContext)
            val dao = db.reminderDao()
            val reminders = dao.getRemindersForUser(username).toMutableList()
            db.close()
            return reminders
        }

        override fun onPostExecute(result: MutableList<ReminderData>?) {
            super.onPostExecute(result)
            list.clear()
            reminderList.clear()
            if (result != null) {
                for (item in result){
                    list.add(item.title)
                    reminderList.add(item)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }
}