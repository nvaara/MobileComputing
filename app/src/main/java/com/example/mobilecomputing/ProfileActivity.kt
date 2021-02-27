package com.example.mobilecomputing

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.room.Room

class ProfileActivity : AppCompatActivity() {

    lateinit var listView : ListView
    lateinit var adapter : ArrayAdapter<String>
    lateinit var username : String
    private var list : MutableList<String> = mutableListOf()
    private var uidList : MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_view)

        val signOutButton = findViewById<Button>(R.id.sign_out_button)
        val addReminderButton = findViewById<Button>(R.id.add_reminder_button)
        val editActiveRemindersButton = findViewById<Button>(R.id.edit_active_reminders_button)
        val updateReminders = findViewById<Button>(R.id.update_reminders_button)

        updateReminders.setOnClickListener{
            loadReminders()
        }

        listView = findViewById(R.id.profile_list_view)
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list)
        listView.adapter = adapter

        username = intent?.extras?.get("username").toString()
        if (username != null) {
            val str = "Welcome back, $username"
            findViewById<TextView>(R.id.greeting_text).text = str
        } else {
            finish()
        }
        signOutButton.setOnClickListener {
            finish()
        }

        addReminderButton.setOnClickListener{
            var intent = Intent(this, ReminderAddActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        editActiveRemindersButton.setOnClickListener{
            var intent = Intent(this, ReminderView::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        //listView.setOnItemClickListener { _, _, position, _ ->
        //    val uid = uidList[position]
        //    var intent = Intent(this, ReminderEditActivity::class.java)
        //    intent.putExtra("uid", uid)
        //    startActivity(intent)
        //}
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
            val newList = mutableListOf<ReminderData>()
            for (reminder in reminders) {
                if (reminder.reminderOccurred) {
                    newList.add(reminder)
                }
            }
            db.close()
            return newList
        }

        override fun onPostExecute(result: MutableList<ReminderData>?) {
            super.onPostExecute(result)
            list.clear()
            uidList.clear()
            if (result != null) {
                for (item in result){
                    list.add(item.title)
                    uidList.add(item.uid!!)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }
}