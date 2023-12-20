package com.example.surfswift.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.surfswift.adapter.BookmarkAdapter
import com.example.surfswift.databinding.ActivityBookmarkBinding
class BookmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding=ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvBookmarks.setItemViewCacheSize(10)
        binding.rvBookmarks.hasFixedSize()
        binding.rvBookmarks.layoutManager=LinearLayoutManager(this)
        binding.rvBookmarks.adapter= BookmarkAdapter(this, isActivity = true)
    }
}