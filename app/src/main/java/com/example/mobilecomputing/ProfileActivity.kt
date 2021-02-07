package com.example.mobilecomputing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class ProfileActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var adapter : ArrayAdapter<String>
    lateinit var list : MutableList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_view)

        val signOutButton = findViewById<Button>(R.id.sign_out_button)
        val addReminderButton = findViewById<Button>(R.id.add_reminder_button)

        listView = findViewById(R.id.profile_list_view)
        list = mutableListOf<String>("Reminder1", "Reminder2", "Reminder3")
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list)
        listView.adapter = adapter

        val username = intent?.extras?.get("username")
        if (username != null) {
            val str = "Welcome back, $username"
            findViewById<TextView>(R.id.greeting_text).text = str
        }
        signOutButton.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }

        addReminderButton.setOnClickListener{
            list.add("New reminder")
            adapter.notifyDataSetChanged()
        }

    }
}