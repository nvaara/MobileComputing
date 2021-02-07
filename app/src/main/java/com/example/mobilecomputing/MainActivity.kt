package com.example.mobilecomputing

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInButton = findViewById<Button>(R.id.button_signin)
        val registerButton = findViewById<Button>(R.id.button_register)

        signInButton.setOnClickListener {
            val username = findViewById<TextView>(R.id.text_username)
            val password = findViewById<TextView>(R.id.text_password)
            if (isValidUser(username.text.toString(), password.text.toString())) {
                var intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("username", username.text.toString())
                startActivity(intent)
            } else {
                username.text = ""
                password.text = ""
                Toast.makeText(this, "Incorrect username or password!", Toast.LENGTH_LONG).show()
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val username = findViewById<TextView>(R.id.text_username)
        val password = findViewById<TextView>(R.id.text_password)
        username.text = ""
        password.text = ""
    }

    private fun isValidUser (username: String, pw: String) : Boolean {
        val sharedPref = getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE)
            val password = sharedPref.getString(username, null);
            if (password != null && pw == password) {
                return true
            }
        return false
    }
}