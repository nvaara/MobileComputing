package com.example.mobilecomputing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices

class GeofenceReceiver : BroadcastReceiver() {

    private lateinit var uid : String
    private lateinit var message : String

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val event = GeofencingEvent.fromIntent(intent)

            if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                if (intent != null) {
                    uid = intent.getStringExtra("uid")!!
                    message = intent.getStringExtra("message")!!
                    ReminderNotification(context.applicationContext, uid.toInt(), 0, message)
                }
            }
            removeGeofences(context, GeofencingEvent.fromIntent(intent).triggeringGeofences)
        }
    }

    private fun removeGeofences(context: Context, geofenceList: MutableList<Geofence>) {
        val idList = mutableListOf<String>()
        for (geofence in geofenceList) {
            idList.add(geofence.requestId)
        }
        LocationServices.getGeofencingClient(context).removeGeofences(idList)
    }
}