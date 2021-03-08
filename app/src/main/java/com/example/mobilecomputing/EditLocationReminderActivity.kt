package com.example.mobilecomputing

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
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
import java.util.*

class EditLocationReminderActivity : AppCompatActivity() {
    private lateinit var textView : TextView
    private lateinit var titleEditText : EditText
    private lateinit var messageEditText : EditText

    private lateinit var geofencingClient : GeofencingClient

    private var longitude : String = ""
    private var latitude : String = ""

    private fun createGeofence(latLng: LatLng, uid: String, message: String) {
        val geofence = Geofence.Builder()
            .setRequestId(uid)
            .setCircularRegion(latLng.latitude, latLng.longitude, 500f)
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
        setContentView(R.layout.activity_edit_location_reminder)
        geofencingClient = LocationServices.getGeofencingClient(this)

        val editButton = findViewById<Button>(R.id.reminder_edit_location_button)
        val deleteButton = findViewById<Button>(R.id.reminder_edit_location_delete_button)
        val goBackButton = findViewById<Button>(R.id.reminder_edit_location_go_back)
        textView = findViewById<TextView>(R.id.text_reminder_location_edit)

        textView.setOnClickListener{
            var intent = Intent(this, LocationPickerActivity::class.java)
            startActivityForResult(intent, 1)
        }

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
                findViewById<TextView>(R.id.text_reminder_location_edit_title).text = result.title
                findViewById<TextView>(R.id.text_reminder_location_edit_message).text = result.message
                val formatted = String.format("Latitude: %.3f\n Longitude: %.3f", result.locationX.toDouble(), result.locationY.toDouble())
                findViewById<TextView>(R.id.text_reminder_location_edit).text = formatted
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

    private fun validateString(str : String, error : String) : Boolean {
        if (str.isEmpty()) {
            Toast.makeText(this, "Title is empty!", Toast.LENGTH_LONG).show()
        }
        return str.isNotEmpty()
    }

    private fun updatedReminderData(oldReminderData: ReminderData) : Boolean {
        val title = findViewById<TextView>(R.id.text_reminder_location_edit_title).text.toString()
        val msg = findViewById<TextView>(R.id.text_reminder_location_edit_message).text.toString()

        if (validateString(title, "Title is empty")
            && validateString(title, "Message is empty")
            && longitude.isNotEmpty()
            && latitude.isNotEmpty()) {

            oldReminderData.title = title
            oldReminderData.message = msg
            oldReminderData.locationX = latitude
            oldReminderData.locationY = longitude
            oldReminderData.reminderTime = ""
            oldReminderData.reminderOccurred = false
            return true
        }
        return false
    }

    inner class UpdateReminder : AsyncTask<String?, String?, Boolean>() {
        override fun doInBackground(vararg params: String?) : Boolean {
            val db = getReminderDb(applicationContext)
            val dao = db.reminderDao()
            val uid = intent?.extras?.get("uid").toString().toInt()
            var reminder = dao.getReminder(uid)
            if (updatedReminderData(reminder)) {
                dao.update(reminder)
                createGeofence(LatLng(latitude.toDouble(), longitude.toDouble()), uid.toString(), reminder.message)
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