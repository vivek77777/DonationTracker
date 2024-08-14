package com.example.donationtracker

object DonationRepository {
    private val donations = mutableListOf<Donation>()

    fun addDonation(donation: Donation) {
        donations.add(donation)
    }

    fun getAllDonations(): List<Donation> {
        return donations
    }

    fun getDonationsByOrganization(organization: String?): List<Donation> {
        return donations.filter { it.organization == organization }
    }

    fun getTotalDonations(): Double {
        return donations.sumOf { it.amount }
    }
}
