package com.example.feedmycat

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var etCatName: EditText
    private lateinit var etCatAge: EditText
    private lateinit var etCatBreed: EditText
    private lateinit var btnSave: Button
    private lateinit var tvCatDetails: TextView

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                sendNotification()
            } else {
                // Handle the case where the user denied the permission
            }
        }

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
            checkNotificationPermissionAndSend()
        }

        createNotificationChannel()
        displayCatDetails()
    }

    private fun enableEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
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

private fun sendNotification() {
        val notificationId = 3
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

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                with(NotificationManagerCompat.from(this)) {
                    notify(notificationId, builder.build())
                }
            } else {
                // Handle the case where the permission is not granted
                // You could log an error or show a message to the user here
            }
        } catch (e: SecurityException) {
            // Handle the SecurityException if it occurs
            e.printStackTrace()
        }
    }


    private fun displayCatDetails() {
        val sharedPref = getSharedPreferences("CatDetails", Context.MODE_PRIVATE)
        val name = sharedPref.getString("name", "")
        val age = sharedPref.getInt("age", 0)
        val breed = sharedPref.getString("breed", "")

        tvCatDetails.text = "Name: $name\nAge: $age\nBreed: $breed"
    }

    private fun checkNotificationPermissionAndSend() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                sendNotification()
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                // Show an explanation to the user
                // You can use a dialog or a Snackbar here
            } else {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            sendNotification()
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
