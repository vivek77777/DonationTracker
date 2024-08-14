package com.example.donationtracker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.content.Intent
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DonationHistoryActivity : AppCompatActivity() {

    private lateinit var donationListView: ListView
    private lateinit var adapter: DonationAdapter
    private lateinit var donations: MutableList<String>
    private lateinit var searchInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_history)

        donationListView = findViewById(R.id.donationListView)
        searchInput = findViewById(R.id.searchInput)

        loadDonations()

        // Setup search functionality
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString()
                filterDonations(searchText)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this implementation
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this implementation
            }
        })

        // Setup long-click listener to delete a donation
        donationListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            showDeleteConfirmationDialog(position)
            true
        }
    }

    private fun loadDonations() {
        val sharedPreferences = getSharedPreferences("DonationTrackerPrefs", Context.MODE_PRIVATE)
        donations = sharedPreferences.getStringSet("donations", setOf())?.toMutableList() ?: mutableListOf()

        adapter = DonationAdapter(this, donations)
        donationListView.adapter = adapter
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Donation")
        builder.setMessage("Are you sure you want to delete this donation?")
        builder.setPositiveButton("Yes") { _, _ ->
            deleteDonation(position)
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }

    private fun deleteDonation(position: Int) {
        if (position < donations.size) {
            donations.removeAt(position)
            adapter.notifyDataSetChanged()

            // Update SharedPreferences
            val sharedPreferences = getSharedPreferences("DonationTrackerPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putStringSet("donations", donations.toSet())
            editor.apply()
        }
    }

    private fun filterDonations(searchText: String) {
        val filteredDonations = donations.filter { donation ->
            donation.contains(searchText, ignoreCase = true)
        }
        adapter.updateData(filteredDonations.toMutableList())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_amount -> {
                sortDonationsByAmount()
                true
            }
            R.id.action_sort_by_date -> {
                sortDonationsByDate()
                true
            }
            R.id.action_sort_by_name -> {
                sortDonationsByName()
                true
            }
            R.id.action_filter_by_date_range -> {
                showDateRangePicker()
                true
            }
            R.id.action_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sortDonationsByAmount() {
        donations.sortBy { donation ->
            val amount = donation.split(" - ")[1].replace("$", "").toDoubleOrNull()
            amount ?: 0.0
        }
        adapter.notifyDataSetChanged()
    }

    private fun sortDonationsByDate() {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        donations.sortBy { donation ->
            val date = donation.split(" - ").last()
            sdf.parse(date)
        }
        adapter.notifyDataSetChanged()
    }

    private fun sortDonationsByName() {
        donations.sortBy { donation ->
            donation.split(" - ").first()
        }
        adapter.notifyDataSetChanged()
    }

    private fun showDateRangePicker() {
        val calendar = java.util.Calendar.getInstance()
        val startDateDialog = android.app.DatePickerDialog(this, { _, year, month, day ->
            val startDate = "${day.toString().padStart(2, '0')}/${(month + 1).toString().padStart(2, '0')}/$year"
            showEndDatePicker(startDate)
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))

        startDateDialog.show()
    }

    private fun showEndDatePicker(startDate: String) {
        val calendar = java.util.Calendar.getInstance()
        val endDateDialog = android.app.DatePickerDialog(this, { _, year, month, day ->
            val endDate = "${day.toString().padStart(2, '0')}/${(month + 1).toString().padStart(2, '0')}/$year"
            filterDonationsByDateRange(startDate, endDate)
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))

        endDateDialog.show()
    }

    private fun filterDonationsByDateRange(startDate: String, endDate: String) {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val filteredDonations = donations.filter { donation ->
            val date = donation.split(" - ").last()
            val donationDate = sdf.parse(date)
            val start = sdf.parse(startDate)
            val end = sdf.parse(endDate)
            donationDate in start..end
        }
        adapter.updateData(filteredDonations.toMutableList())
    }
}
