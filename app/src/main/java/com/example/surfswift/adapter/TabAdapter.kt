package com.example.surfswift.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.surfswift.activity.MainActivity
import com.example.surfswift.databinding.TabBinding
import com.google.android.material.snackbar.Snackbar

class TabAdapter(private var context: Context, private val dialog: AlertDialog) :
    RecyclerView.Adapter<TabAdapter.MyHolder>() {
    class MyHolder(binding: TabBinding) : RecyclerView.ViewHolder(binding.root) {
        val cancelBtn = binding.cancelButton
        val name = binding.tabName
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(TabBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = MainActivity.tabsList[position].name
        holder.root.setOnClickListener {
            MainActivity.myPager.currentItem = position
            dialog.dismiss()
        }

        holder.cancelBtn.setOnClickListener {
            MainActivity.tabsbtn.text = MainActivity.tabsList.size.toString()
            if (MainActivity.tabsList.size == 1)
                Snackbar.make(MainActivity.myPager, "Can't remove current tab.", 3000).show()
            else {
                val removedTab = MainActivity.tabsList[position]
                val isHome = removedTab.name == "Home"
                val numberOfHomeFragments = MainActivity.tabsList.count { it.name == "Home" }
                val canremoveHome = numberOfHomeFragments > 1

                if (!isHome || canremoveHome) {
                    MainActivity.tabsList.removeAt(position)
                    notifyDataSetChanged()
                    MainActivity.myPager.adapter?.notifyItemRemoved(position)
                } else {
                    Snackbar.make(MainActivity.myPager, "Can't remove home tab.", 300).show()
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return MainActivity.tabsList.size
    }

}