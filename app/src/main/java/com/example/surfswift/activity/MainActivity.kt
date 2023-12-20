package com.example.surfswift.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.surfswift.fragment.BrowseFragment
import com.example.surfswift.fragment.HomeFragment
import com.example.surfswift.model.Bookmark
import com.example.surfswift.R
import com.example.surfswift.activity.MainActivity.Companion.myPager
import com.example.surfswift.activity.MainActivity.Companion.tabsbtn
import com.example.surfswift.adapter.TabAdapter
import com.example.surfswift.databinding.ActivityMainBinding
import com.example.surfswift.databinding.BookmarkDialogBinding
import com.example.surfswift.databinding.MoreFeaturesBinding
import com.example.surfswift.databinding.TabsViewBinding
import com.example.surfswift.model.Tab
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream

import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.Exception
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding  //creating an object of view binding
    private var printjob: PrintJob? = null
    companion object { //static objects which are called only in time in the life cycle of an activity
        var tabsList: ArrayList<Tab> = ArrayList()
        private var isfullScreen: Boolean = false
        var isDesktopSite: Boolean = false
        var bookmarkList: ArrayList<Bookmark> = ArrayList()
        var bookmarkIndex: Int = -1
        lateinit var myPager: ViewPager2
        lateinit var tabsbtn: MaterialTextView
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        binding = ActivityMainBinding.inflate(layoutInflater)  //initialising before setting content
        setContentView(binding.root)  //the content view will be set to the binding of the Main as the binding is object for activity main
        getAllBookmarks()

        tabsList.add(Tab("Home",HomeFragment())) //just at the time when the activity is created home fragment will be added to the tabs list

        binding.myPager.adapter = TabsAdapter(supportFragmentManager, lifecycle)  // initialising the view pager with the adapter
        binding.myPager.isUserInputEnabled = false
        myPager=binding.myPager

        tabsbtn=binding.tabsBtn
        initialiseViews()
        changeFullscreen(enable = false)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
        var frag: BrowseFragment? = null
        try {
            frag = tabsList[binding.myPager.currentItem].fragmnet as BrowseFragment
        } catch (_: Exception) {
        }
        when {
            // this go back is done so that if we click back button from a fragment then it can go back again to that fragment
            frag?.binding?.webView?.canGoBack() == true -> frag.binding.webView.goBack()
            binding.myPager.currentItem != 0 -> {
                tabsList.removeAt(binding.myPager.currentItem)
                binding.myPager.adapter?.notifyDataSetChanged()
                binding.myPager.currentItem = tabsList.size - 1
            }

            else -> super.onBackPressed()
        }
    }

    private inner class TabsAdapter(fa: FragmentManager, lc: Lifecycle) :
        FragmentStateAdapter(fa, lc) { // adapter for the tabs of the browser
        override fun getItemCount(): Int = tabsList.size  // the number of tabs that are created

        override fun createFragment(position: Int): Fragment =
            tabsList[position].fragmnet  //the tab of the particular position int the array of tabs will be created
    }


    @Suppress("DEPRECATION")
    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initialiseViews() {
        binding.tabsBtn.setOnClickListener {
            val viewTabs = layoutInflater.inflate(R.layout.tabs_view, binding.root, false) // this will create a view for the dialog of the back and forward etc features but it is not attached to the root so it will not be attached to the main activity
            val tabsBinding = TabsViewBinding.bind(viewTabs)

            val dialogTabs = MaterialAlertDialogBuilder(this,R.style.roundCornerDialog).setView(viewTabs)
                .setTitle("Select Tab")
                .setPositiveButton("Home"){self,_->
                    changeTab("Home",HomeFragment())
                    self.dismiss()}
                .setNeutralButton("Google"){self,_->
                    changeTab("Google",BrowseFragment(URL="https://www.google.com"))

                    self.dismiss()
                }
                .create()

            tabsBinding.tabsRV.setHasFixedSize(true)
            tabsBinding.tabsRV.layoutManager=LinearLayoutManager(this)
            tabsBinding.tabsRV.adapter=TabAdapter(this,dialogTabs)
            dialogTabs.show()

            val positiveButton=dialogTabs.getButton(AlertDialog.BUTTON_POSITIVE)
            val neutralButton=dialogTabs.getButton(AlertDialog.BUTTON_NEUTRAL)

            positiveButton.isAllCaps=false
            neutralButton.isAllCaps=false
            positiveButton.setTextColor(Color.BLACK)
            neutralButton.setTextColor(Color.BLACK)
            positiveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(ResourcesCompat.getDrawable(resources,R.drawable.ic_home,theme)
                ,null,null,null)
            neutralButton.setCompoundDrawablesRelativeWithIntrinsicBounds(ResourcesCompat.getDrawable(resources,R.drawable.ic_add,theme)
                ,null,null,null)
        }

        binding.settingsButton.setOnClickListener {

            var frag: BrowseFragment? = null
            try {
                frag = tabsList[binding.myPager.currentItem].fragmnet as BrowseFragment
            } catch (_: Exception) {
            }

            // below is the implementation for showing dialog
            val view = layoutInflater.inflate(R.layout.more_features, binding.root, false) // this will create a view for the dialog of the back and forward etc features but it is not attached to the root so it will not be attached to the main activity
            val dialogBinding = MoreFeaturesBinding.bind(view)
            val dialog = MaterialAlertDialogBuilder(this).setView(view).create()

            dialog.window?.apply {
                attributes.gravity = Gravity.BOTTOM  // generally dialogues are shown in  middle but his attribute will make bring it to bottom
                attributes.y = 50   // to make shift it a bit up by 50dp
                setBackgroundDrawable(ColorDrawable(0xffffffff.toInt())) // this will set the background as white and using this it will be covering approximately whole width
            }
            dialog.show()

            if (isfullScreen) {
                dialogBinding.fullscreenBtn.setIconTintResource(R.color.cool_blue)
                dialogBinding.fullscreenBtn.setTextColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.cool_blue
                    )
                )
            }

            frag?.let{
                bookmarkIndex=isBookmarked(it.binding.webView.url!!)
                if (bookmarkIndex != -1) { bookmarkIndex=isBookmarked(it.binding.webView.url!!)
                    dialogBinding.bookmarkBtn.setIconTintResource(R.color.cool_blue)
                    dialogBinding.bookmarkBtn.setTextColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.cool_blue
                        )
                    )
                }
            }

            if (isDesktopSite) {
                dialogBinding.desktopBtn.setIconTintResource(R.color.cool_blue)
                dialogBinding.desktopBtn.setTextColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.cool_blue
                    )
                )
            }


            dialogBinding.backBtn.setOnClickListener {
                onBackPressed()
            }

            dialogBinding.forwardBtn.setOnClickListener {
                frag?.apply {
                    if (binding.webView.canGoForward()) {
                        binding.webView.goForward()
                    }
                }
            }

            // save btn will save the web page as pdf
            dialogBinding.saveBtn.setOnClickListener {
                dialog.dismiss()
                if(frag?.binding?.webView?.progress!=100){
                    Toast.makeText(this,"Let the site load properly.",Toast.LENGTH_SHORT).show()
                }
                else if (frag != null && frag.binding.webView.url != null) {
                    saveAsPdf(web = frag.binding.webView)
                } else {
                    Toast.makeText(this, "First open a Web page", Toast.LENGTH_SHORT).show()
                }
            }

            dialogBinding.fullscreenBtn.setOnClickListener {
                it as MaterialButton
                isfullScreen = if (isfullScreen) {
                    changeFullscreen(enable = false)
                    if (isfullScreen) {
                        it.setIconTintResource(R.color.black)
                        it.setTextColor(ContextCompat.getColor(this, R.color.black))
                    }
                    false
                } else {
                    changeFullscreen(enable = true)
                    if (!isfullScreen) {
                        it.setIconTintResource(R.color.cool_blue)
                        it.setTextColor(ContextCompat.getColor(this, R.color.cool_blue))
                    }
                    true
                }
            }

            dialogBinding.desktopBtn.setOnClickListener {
                it as MaterialButton
                if (frag == null) {
                    Toast.makeText(this, "First open a Web Page", Toast.LENGTH_SHORT).show()
                } else {
                    frag.binding.webView.apply {
                        isDesktopSite = if (isDesktopSite) {
                            settings.userAgentString = null
                            // Set back to default user agent for mobile view
                            false
                        } else {
                            settings.userAgentString =
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                            settings.useWideViewPort = true
                            // Corrected JavaScript string concatenation
                            evaluateJavascript(
                                "document.querySelector('meta[name=\"viewport\"]').setAttribute('content', " +
                                        "'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));",
                                null
                            )
                            true
                        }

                        // Reload the WebView to apply changes
                        reload()
                        dialog.dismiss()
                    }
                }
            }

            dialogBinding.bookmarkBtn.setOnClickListener{
                frag?.let {

                    if(bookmarkIndex==-1){
                        val viewBookmark = layoutInflater.inflate(R.layout.bookmark_dialog, binding.root, false) // this will create a view for the dialog of the back and forward etc features but it is not attached to the root so it will not be attached to the main activity
                        val bookmarkDialogBinding = BookmarkDialogBinding.bind(viewBookmark)

                        val bookmarkDialog = MaterialAlertDialogBuilder(this)
                            .setTitle("Add Bookmark")
                            .setMessage("Url: ${it.binding.webView.url}")
                            .setPositiveButton("Add"){self,_->
                                try {
                                    val array = ByteArrayOutputStream()
                                    it.favicon?.compress((Bitmap.CompressFormat.PNG),100,array)
                                    bookmarkList.add(Bookmark(name=bookmarkDialogBinding.bookmarkTitle.text.toString(),url=it.binding.webView.url!!,array.toByteArray()))
                                }catch (e:Exception){
                                    bookmarkList.add(Bookmark(name=bookmarkDialogBinding.bookmarkTitle.text.toString(),url=it.binding.webView.url!!))
                                }
                                self.dismiss()}
                            .setNegativeButton("Cancel"){self,_->self.dismiss()}
                            .setView(viewBookmark).create()

                        bookmarkDialog.show()

                        bookmarkDialogBinding.bookmarkTitle.setText(it.binding.webView.title)
                    }else{
//                        val viewBookmark = layoutInflater.inflate(
//                            R.layout.bookmark_dialog,
//                            binding.root,
//                            false
//                        ) // this will create a view for the dialog of the back and forward etc features but it is not attached to the root so it will not be attached to the main activity
//                        val bookmarkDialogBinding = BookmarkDialogBinding.bind(viewBookmark)

                        val bookmarkDialog = MaterialAlertDialogBuilder(this)
                            .setTitle("Remove Bookmark")
                            .setMessage("Url:${it.binding.webView.url}")
                            .setPositiveButton("Remove"){self,_->
                                bookmarkList.removeAt(bookmarkIndex)
                                self.dismiss()}
                            .setNegativeButton("Cancel"){self,_->self.dismiss()}
                            .create()

                        bookmarkDialog.show()


                    }
                }

                dialog.dismiss()
            }


        }
    }

    override fun onResume() {
        super.onResume()
        printjob?.let {

            when {
                it.isCompleted -> Toast.makeText(this, "Successful->${it.info.label}", Toast.LENGTH_LONG).show()
                it.isFailed -> Toast.makeText(this, "Failed->${it.info.label}", Toast.LENGTH_LONG).show()
                it.isCancelled -> Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                it.isBlocked -> Toast.makeText(this, "Blocked", Toast.LENGTH_LONG).show()
            }

        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun saveAsPdf(web: WebView) {
        val printmanager = getSystemService(Context.PRINT_SERVICE) as PrintManager

        val jobname = "${URL(web.url).host}_${SimpleDateFormat("HH:mm, d_MMM_yy", Locale.ENGLISH).format(Calendar.getInstance().time)}"

        val printadapter = web.createPrintDocumentAdapter(jobname)
        val printAttributes = PrintAttributes.Builder()
        printjob = printmanager.print(jobname, printadapter, printAttributes.build())


    }
    private fun changeFullscreen(enable: Boolean) {  // if enable is true them change to full screen otherwise change to small
        if (enable) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, binding.root).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, binding.root).show((WindowInsetsCompat.Type.systemBars()))
        }

    }
    fun isBookmarked(url: String): Int{
        bookmarkList.forEachIndexed{
            index,bookmark-> if(bookmark.url == url) return index
        }
        return -1
    }
    fun saveBookmarks(){
        // for storing bookmarks data using shared preferences
        val editor = getSharedPreferences("BOOKMARKS", MODE_PRIVATE).edit()

        val data = GsonBuilder().create().toJson(bookmarkList)
        editor.putString("bookmarkList",data)

        editor.apply()
    }
    private fun getAllBookmarks(){
        bookmarkList = ArrayList()
        val editor = getSharedPreferences("BOOKMARKS", MODE_PRIVATE)
        val data = editor.getString("bookmarkList",null)
        if(data !=null ){
            val list:ArrayList<Bookmark> = GsonBuilder().create().fromJson(data,object: TypeToken<ArrayList<Bookmark>>(){}.type)
            bookmarkList.addAll(list)

        }
    }
}
@SuppressLint("NotifyDataSetChanged")
fun changeTab(url: String, fragment: Fragment,isBackground : Boolean=false) { // function to change tabs
    MainActivity.tabsList.add(Tab(name=url, fragmnet = fragment))            // fragment is added to the tabs list
    myPager.adapter?.notifyDataSetChanged() //the adapter will be notified about the tab added
    tabsbtn.text=MainActivity.tabsList.size.toString()

    if(!isBackground) myPager.currentItem = MainActivity.tabsList.size-1

}
@Suppress("DEPRECATION")
fun checkForInternet(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {

        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        return networkInfo.isConnected
    }
}