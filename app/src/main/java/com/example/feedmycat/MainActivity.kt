package com.example.feedmycat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var etCatName: EditText
    private lateinit var etCatAge: EditText
    private lateinit var etCatBreed: EditText
    private lateinit var btnSave: Button
    private lateinit var tvCatDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        etCatName = findViewById(R.id.etCatName)
        etCatAge = findViewById(R.id.etCatAge)
        etCatBreed = findViewById(R.id.etCatBreed)
        btnSave = findViewById(R.id.btnSave)
        tvCatDetails = findViewById(R.id.tvCatDetails)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnSave)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSave.setOnClickListener {
            saveCatDetails()
            displayCatDetails()
            sendNotification()
        }

        createNotificationChannel()
        displayCatDetails()
    }

    private fun saveCatDetails() {
        val sharedPref = getSharedPreferences("CatDetails", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("name", etCatName.text.toString())
            putInt("age", etCatAge.text.toString().toIntOrNull() ?: 0)
            putString("breed", etCatBreed.text.toString())
            apply()
        }
    }

    private fun displayCatDetails() {
        val sharedPref = getSharedPreferences("CatDetails", Context.MODE_PRIVATE)
        val name = sharedPref.getString("name", "")
        val age = sharedPref.getInt("age", 0)
        val breed = sharedPref.getString("breed", "")

        tvCatDetails.text = "Name: $name\nAge: $age\nBreed: $breed"
    }

    private fun sendNotification() {
        val notificationId = 1
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(this, "feedMyCatChannel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Feed My Cat")
            .setContentText("It's time to feed your cat!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Feed My Cat Channel"
            val descriptionText = "Channel for feed my cat reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("feedMyCatChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
