package com.example.donationtracker

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class DonationSummaryActivity : AppCompatActivity() {

    private lateinit var summaryTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_summary)

        summaryTextView = findViewById(R.id.summaryTextView)

        val organization = intent.getStringExtra("organization")
        val amount = intent.getStringExtra("amount")
        val date = intent.getStringExtra("date")

        summaryTextView.text = "Organization: $organization\nAmount: $$amount\nDate: $date"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

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
