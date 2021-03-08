package com.example.mobilecomputing

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class AddLocationReminder : AppCompatActivity() {
    private lateinit var textView : TextView
    private lateinit var titleEditText : EditText
    private lateinit var messageEditText : EditText

    private var longitude : String = ""
    private var latitude : String = ""

    private lateinit var geofencingClient : GeofencingClient

    private fun createGeoFence(latLng: LatLng, uid: String, message: String) {
        val geofence = Geofence.Builder()
            .setRequestId(uid)
            .setCircularRegion(latLng.latitude, latLng.longitude, 500.0f)
            .setExpirationDuration(10 * 24 * 60 * 60 * 1000)
            .setLoiteringDelay(10 * 1000)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL).build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceReceiver::class.java)
            .putExtra("message", message)
            .putExtra("uid", uid)

        val pendingIntent = PendingIntent.getBroadcast(applicationContext,
            0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 12345)
            } else {
                geofencingClient.addGeofences(request, pendingIntent).run {
                    addOnSuccessListener { Log.d("Tag", "Geofences Added.") }
                    addOnFailureListener{ Log.d("Tag", "Failed to add geofence") }
                }
            }
        } else {
            geofencingClient.addGeofences(request, pendingIntent).run {
                addOnSuccessListener { Log.d("Tag", "Geofences Added.") }
                addOnFailureListener{ Log.d("Tag", "Failed to add geofence") }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            latitude = data?.getStringExtra("latitude")!!
            longitude = data?.getStringExtra("longitude")!!
            val formatted = String.format("Latitude: %.3f\n Longitude: %.3f", latitude.toDouble(), longitude.toDouble())
            textView.text = formatted
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_time_reminder)

        val addReminderButton = findViewById<Button>(R.id.reminder_location_add_confirm)
        val goBackButton = findViewById<Button>(R.id.reminder_time_add_go_back)
        textView = findViewById(R.id.text_reminder_location)
        titleEditText = findViewById(R.id.text_location_reminder_title)
        messageEditText = findViewById(R.id.text_location_reminder_message)

        geofencingClient = LocationServices.getGeofencingClient(this)

        addReminderButton.setOnClickListener {
            val reminderData = tryConstructReminderData()
            if (reminderData != null) {
                AsyncTask.execute {
                    val db = getReminderDb(applicationContext)
                    val uid = db.reminderDao().insert(reminderData).toInt()
                    db.close()
                    createGeoFence(LatLng(latitude.toDouble(), longitude.toDouble()), uid.toString(), reminderData.message)
                    addReminderButton.post{
                        finish()
                    }
                }
            }
        }

        textView.setOnClickListener{
            var intent = Intent(this, LocationPickerActivity::class.java)
            startActivityForResult(intent, 1)
        }

        goBackButton.setOnClickListener {
            finish()
        }
    }

    private fun validateString(str : String, error : String) : Boolean {
        if (str.isEmpty()) {
            Toast.makeText(this, "Title is empty!", Toast.LENGTH_LONG).show()
        }
        return str.isNotEmpty()
    }

    private fun tryConstructReminderData() : ReminderData? {
        var title = titleEditText.text.toString()
        var message = messageEditText.text.toString()
        if (validateString(title, "Title is empty") &&
                validateString(message, "Message is empty") &&
                latitude.isNotEmpty() &&
                longitude.isNotEmpty()) {
            return ReminderData(null,
                title,
                message,
                latitude,
                longitude,
                "",
                "",
                intent?.extras?.get("username").toString(),
                reminderSeen = false,
                reminderOccurred = false)
        }
        return null
    }
}