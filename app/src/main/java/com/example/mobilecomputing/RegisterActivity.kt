package com.example.mobilecomputing

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialDialogs

class RegisterActivity : AppCompatActivity() {

    private fun isValidUsernamePassword (username: String, pw: String, pwVerify: String) : Boolean {
        return when {
            username.length < 4 -> {
                Toast.makeText(this, "Username must be atleast 4 characters!", Toast.LENGTH_LONG).show()
                resetText()
                false
            }
            pw.length < 4 -> {
                Toast.makeText(this, "Password must be atleast 4 characters!", Toast.LENGTH_LONG).show()
                resetText()
                false;
            }
            pw != pwVerify -> {
                Toast.makeText(this, "Passwords didn't match!", Toast.LENGTH_LONG).show()
                resetText()
                false;
            }
            else -> true
        }
    }

    private fun isUsernameTaken(username: String) : Boolean {
        val sharedPref = getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE)
        val returnVal = sharedPref.getString(username.toString(), null) != null
        if (returnVal) {
            Toast.makeText(this, "Username is taken!", Toast.LENGTH_LONG).show()
            resetText()
        }
        return returnVal
    }

    private fun createUser(username: String, password: String) {
        val sharedPref = getSharedPreferences(getString(R.string.shared_preference), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(username, password)
            apply()
        }
    }

    private fun resetText() {
        findViewById<TextView>(R.id.text_register_username).text = ""
        findViewById<TextView>(R.id.text_register_password).text = ""
        findViewById<TextView>(R.id.text_register_password_verify).text = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val registerButton = findViewById<Button>(R.id.button_try_register)
        val goBackButton = findViewById<Button>(R.id.button_go_back)
        goBackButton.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }

        registerButton.setOnClickListener {
            val username = findViewById<TextView>(R.id.text_register_username)
            val password = findViewById<TextView>(R.id.text_register_password)
            val passwordVerify = findViewById<TextView>(R.id.text_register_password_verify)

            if (!isUsernameTaken(username.text.toString())
                && isValidUsernamePassword(username.text.toString(), password.text.toString(), passwordVerify.text.toString())){

                createUser(username.text.toString(), password.text.toString())
                var intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("username", username.text.toString())
                startActivity(intent)
            }
        }
    }
}