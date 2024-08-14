package com.example.donationtracker

import android.Manifest
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var organizationInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var addButton: Button
    private lateinit var clearButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ensure the notification channel is created before any notification is sent
        createNotificationChannel()

        // Request Notification permission for Android 13 and above
        requestNotificationPermission()

        // Initialize views
        initializeViews()

        // Set up DatePickerDialog for date input
        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        // Set up the Add Donation button click listener
        addButton.setOnClickListener {
            handleAddButtonClick()
        }

        // Set up the Clear button click listener
        clearButton.setOnClickListener {
            clearFields()
        }
    }

    // Function to create a notification channel for Android 8.0 and higher
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Donation Channel Test"
            val descriptionText = "Test Notifications for Donations"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("donation_channel_test", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Function to request notification permission for Android 13 and higher
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }

    // Function to initialize views
    private fun initializeViews() {
        organizationInput = findViewById(R.id.organizationInput)
        amountInput = findViewById(R.id.amountInput)
        dateInput = findViewById(R.id.dateInput)
        addButton = findViewById(R.id.addButton)
        clearButton = findViewById(R.id.clearButton)
    }

    // Function to show DatePickerDialog and set the selected date to the dateInput
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "${selectedDay.toString().padStart(2, '0')}/${(selectedMonth + 1).toString().padStart(2, '0')}/$selectedYear"
            dateInput.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    // Function to handle Add button click event
    private fun handleAddButtonClick() {
        val organization = organizationInput.text.toString()
        val amount = amountInput.text.toString()
        val date = dateInput.text.toString()

        // Check if all fields are filled in
        if (organization.isEmpty() || amount.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        } else {
            // Save the donation to SharedPreferences
            saveDonation(organization, amount, date)

            // Send notification after adding the donation
            val amountValue = amount.toDoubleOrNull()
            if (amountValue != null) {
                sendNotification(organization, amountValue)
            }

            // Start DonationSummaryActivity with the donation details
            val intent = Intent(this, DonationSummaryActivity::class.java).apply {
                putExtra("organization", organization)
                putExtra("amount", amount)
                putExtra("date", date)
            }
            startActivity(intent)

            // Clear the input fields after saving
            clearFields()
        }
    }

    // Function to save donation data in SharedPreferences
    private fun saveDonation(organization: String, amount: String, date: String) {
        val sharedPreferences = getSharedPreferences("DonationTrackerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve existing donations and append the new one
        val donations = sharedPreferences.getStringSet("donations", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        donations.add("$organization - $$amount - $date")

        editor.putStringSet("donations", donations)
        editor.apply()
    }

    // Function to send a notification after a donation is added
    private fun sendNotification(organization: String, amount: Double) {
        // Create an intent that will be triggered when the notification is clicked
        val intent = Intent(this, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, "donation_channel_test")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Donation Added")
            .setContentText("You donated $$amount to $organization")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You donated $$amount to $organization"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(fullScreenPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    // Function to clear the input fields
    private fun clearFields() {
        organizationInput.text.clear()
        amountInput.text.clear()
        dateInput.text.clear()
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, you can post notifications now
            } else {
                // Permission denied, inform the user that notifications won't work
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Inflate the menu for the homepage
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Handle action bar item clicks here
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_view_history -> {
                val intent = Intent(this, DonationHistoryActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
