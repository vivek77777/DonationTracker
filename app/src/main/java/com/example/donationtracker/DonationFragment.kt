package com.example.donationtracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class DonationFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_donation, container, false)

        progressBar = view.findViewById(R.id.progressBar)
        progressTextView = view.findViewById(R.id.progressTextView)

        val totalDonations = DonationRepository.getTotalDonations()
        val goal = 1000.0 // Example goal

        progressBar.max = goal.toInt()
        progressBar.progress = totalDonations.toInt()

        progressTextView.text = "Donations: $$totalDonations of $$goal"

        return view
    }
}
