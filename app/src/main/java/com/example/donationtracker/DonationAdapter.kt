package com.example.donationtracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class DonationAdapter(private val context: Context, private var donations: MutableList<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return donations.size
    }

    override fun getItem(position: Int): Any {
        return donations[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_donation, parent, false)

        val donation = donations[position].split(" - ")

        val organizationTextView = view.findViewById<TextView>(R.id.organizationTextView)
        val amountTextView = view.findViewById<TextView>(R.id.amountTextView)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)

        organizationTextView.text = "Organization: ${donation[0]}"
        amountTextView.text = "Amount: ${donation[1]}"
        dateTextView.text = "Date: ${donation[2]}"

        return view
    }

    fun updateData(newDonations: MutableList<String>) {
        donations = newDonations
        notifyDataSetChanged()
    }
}
