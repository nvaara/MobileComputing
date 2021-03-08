package com.example.mobilecomputing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var provider = locationManager.getProvider(LocationManager.GPS_PROVIDER)
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200L, 50f, this)
        }

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

    override fun onLocationChanged(location: Location?) {
        Log.d("Tag", "Location")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }
}