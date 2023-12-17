package com.example.surfswift

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
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

        mainActivityRef.binding.topSearchBar.setText("") // this will make the top search bar empty whenever we will come to home fragment
        binding.searchView.setQuery("",false) // same way this will make the search vie field input area empty submit false is must otherwise it will keep submitting again and again

        mainActivityRef.binding.webIcon.setImageResource(R.drawable.ic_search)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(url: String?): Boolean {
                if(mainActivityRef.checkForInternet(requireContext()))
                    mainActivityRef.changeTab(url!!,BrowseFragment(url)) // the main activity is required here and in that changeTab function is called which has parameters url string and the fragment
                // the browse fragment is created as a constructor which accepts the url and that url is passed to loadUrl which will be opened

                else
                    Snackbar.make(binding.root,"Internet Not Connected 😕",300).show()
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean = false

        })
        mainActivityRef.binding.goBtn.setOnClickListener{
            if(mainActivityRef.checkForInternet(requireContext()))
                mainActivityRef.changeTab(mainActivityRef.binding.topSearchBar.text.toString(),
                    BrowseFragment(mainActivityRef.binding.topSearchBar.text.toString())) // the main activity is required here and in that changeTab function is called which has parameters url string and the fragment
            // the browse fragment is created as a constructor which accepts the url and that url is passed to loadUrl which will be opened

            else
                Snackbar.make(binding.root,"Internet Not Connected 😕",300).show()
        }


    }

}


