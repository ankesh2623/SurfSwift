package com.example.surfswift

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.surfswift.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding  //creating an object of view binding

    companion object{ //static objects which are called only in time in the life cycle of an activity
        var tabsList: ArrayList<Fragment> = ArrayList()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)  //initialising before setting content
        setContentView(binding.root)  //the content view will be set to the binding of the Main as the binding is object for activity main

        tabsList.add(HomeFragment()) //just at the time when the activity is created home fragment will be added to the tabs list

        binding.myPager.adapter=TabsAdapter(supportFragmentManager,lifecycle)  // initialising the view pager with the adapter

        binding.myPager.isUserInputEnabled=false;
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
        var frag: BrowseFragment?= null
        try{
            frag= tabsList[binding.myPager.currentItem] as BrowseFragment
        }catch (e:Exception){}
        when{
            // this go back is done so that if we click back button from a fragment then it can go back again to that fragment
            frag?.binding?.webView?.canGoBack()==true->frag.binding.webView.goBack()
            binding.myPager.currentItem!=0 ->{
                tabsList.removeAt(binding.myPager.currentItem)
                binding.myPager.adapter!!.notifyDataSetChanged()
                binding.myPager.currentItem= tabsList.size-1
            }
            else -> super.onBackPressed()
        }
    }
    private inner class TabsAdapter(fa: FragmentManager, lc: Lifecycle) : FragmentStateAdapter(fa,lc) { // adapter for the tabs of the browser
        override fun getItemCount(): Int = tabsList.size  // the number of tabs that are created

        override fun createFragment(position: Int): Fragment = tabsList[position]  //the tab of the particulat position int the array of tabs will be created
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeTab(url : String, fragment: Fragment){ // function to change tabs
        tabsList.add(fragment)            // fragment is added to the tabs list
        binding.myPager.adapter?.notifyDataSetChanged() //the adapter will be notified about the tab added
        binding.myPager.currentItem= tabsList.size-1  // current item is representing the current tab
        // whatever is the tabslist size that is the number of tabs so that index will be tabslist.size - 1 in the array list
    }

    fun checkForInternet(context: Context): Boolean{
        val connectivityManager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork= connectivityManager.getNetworkCapabilities(network)?:return false

            return when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->true
                else ->false
            }
        }
        else{

            val networkInfo=connectivityManager.activeNetworkInfo?:return false
            return networkInfo.isConnected
        }
    }
}