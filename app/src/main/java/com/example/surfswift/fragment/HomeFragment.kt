package com.example.surfswift.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.surfswift.adapter.BookmarkAdapter
import com.example.surfswift.activity.MainActivity
import com.example.surfswift.R
import com.example.surfswift.activity.BookmarkActivity
import com.example.surfswift.activity.changeTab
import com.example.surfswift.activity.checkForInternet
import com.example.surfswift.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding  // object of home fragment binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_home, container, false)  // we need to create a view first for binding fragments
        binding = FragmentHomeBinding.bind(view)   //  then we pass that view to the bind function of the fragment binding

        return view   // at last we return the view
    }

    // whenever fragments are changed onResume will be called
    // so each time we will be sure that search view is initialised
    override fun onResume() {
        super.onResume()

        val mainActivityRef= requireActivity() as MainActivity
        mainActivityRef.binding.refreshBtn.visibility=View.GONE

        MainActivity.tabsList[MainActivity.myPager.currentItem].name="Home"
        MainActivity.tabsbtn.text=MainActivity.tabsList.size.toString()


        mainActivityRef.binding.topSearchBar.setText("") // this will make the top search bar empty whenever we will come to home fragment
        binding.searchView.setQuery("",false) // same way this will make the search vie field input area empty submit false is must otherwise it will keep submitting again and again

        mainActivityRef.binding.webIcon.setImageResource(R.drawable.ic_search)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(url: String?): Boolean {
                if(checkForInternet(requireContext()))
                    changeTab(url!!, BrowseFragment(url)) // the main activity is required here and in that changeTab function is called which has parameters url string and the fragment
                // the browse fragment is created as a constructor which accepts the url and that url is passed to loadUrl which will be opened

                else
                    Snackbar.make(binding.root,"Internet Not Connected 😕",300).show()
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean = false

        })
        mainActivityRef.binding.goBtn.setOnClickListener{
            val inputText = mainActivityRef.binding.topSearchBar.text.toString().trim()
            if(inputText.isNotEmpty()){
                if(checkForInternet(requireContext()))
                    changeTab(mainActivityRef.binding.topSearchBar.text.toString(),
                        BrowseFragment(mainActivityRef.binding.topSearchBar.text.toString())
                    ) // the main activity is required here and in that changeTab function is called which has parameters url string and the fragment
                // the browse fragment is created as a constructor which accepts the url and that url is passed to loadUrl which will be opened

                else
                    Snackbar.make(binding.root,"Internet Not Connected 😕",300).show()
            }
            else{
                Snackbar.make(binding.root,"Input Something",Toast.LENGTH_SHORT).show()
            }
        }



        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(5)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(),5)
        binding.recyclerView.adapter = BookmarkAdapter(requireContext())

        if(MainActivity.bookmarkList.size<1){
            binding.viewAllBtn.visibility=View.GONE
        }
        binding.viewAllBtn.setOnClickListener {
            startActivity(Intent(requireContext(),BookmarkActivity::class.java))
        }

    }

}


