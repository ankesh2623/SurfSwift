package com.example.surfswift.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.util.Base64
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ShareCompat
import com.example.surfswift.activity.MainActivity
import com.example.surfswift.R
import com.example.surfswift.activity.changeTab
import com.example.surfswift.databinding.FragmentBrowseBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.lang.Exception

class BrowseFragment(private var URL: String ) : Fragment() {

    lateinit var binding: FragmentBrowseBinding  // object of browse fragment binding
    var favicon: Bitmap? = null
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_browse, container, false)  // we need to create a view first for binding fragments
        binding = FragmentBrowseBinding.bind(view)   //  then we pass that view to the bind function of the fragment binding
        registerForContextMenu(binding.webView)

        binding.webView.apply {
            when{
                URLUtil.isValidUrl(URL)->loadUrl(URL)
                URL.contains(".com", ignoreCase = true) ->loadUrl(URL)
                else -> loadUrl("https://www.google.com/search?q=$URL")
            }
        }

        return view   // at last we return the view
    }
    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        MainActivity.tabsList[MainActivity.myPager.currentItem].name = binding.webView.url.toString()
        MainActivity.tabsbtn.text=MainActivity.tabsList.size.toString()

        binding.webView.setDownloadListener { url, _, _, _, _ ->  startActivity(Intent(Intent.ACTION_VIEW).setData(
            Uri.parse(url)))}

        val mainActivityRef = requireActivity() as MainActivity

        mainActivityRef.binding.refreshBtn.visibility=View.VISIBLE
        mainActivityRef.binding.refreshBtn.setOnClickListener{
            binding.webView.reload()
        }

        binding.webView.apply {

            //by default webview has javascript disabled so we should enable it as below
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true // just by swipe in and swipe out we should be able to do zoom in and zoom out
            settings.displayZoomControls = false // plus and minus sign wont be showed for zooming

            // if we do not use these webclient and webChromeClient, we will be redirected to google browser when we swipe left and search anything in that webview of the below url of google search page
            webViewClient = object : WebViewClient() {
                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                    if (MainActivity.isDesktopSite) {
                        view?.evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content', " +
                                    "'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));",
                            null
                        )
                    }
                }

                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    mainActivityRef.binding.topSearchBar.text = SpannableStringBuilder(url) // this spannable string builder will make the url editable as the url is string type so we need to make it editable
                    // after writing the above method everytime we search something url will be shown at the top search bar just as it does in any other browser
                    MainActivity.tabsList[MainActivity.myPager.currentItem].name = url.toString()
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    mainActivityRef.binding.progressBar.progress = 0
                    mainActivityRef.binding.progressBar.visibility = View.VISIBLE
                    if (url!!.contains("tube", ignoreCase = false)) mainActivityRef.binding.root.transitionToEnd()
                    if (url.contains("tv", ignoreCase = false)) mainActivityRef.binding.root.transitionToEnd()
                    if (url.contains("video", ignoreCase = false)) mainActivityRef.binding.root.transitionToEnd()
                    if (url.contains("netflix", ignoreCase = false)) mainActivityRef.binding.root.transitionToEnd()
                    if (url.contains("disney", ignoreCase = false)) mainActivityRef.binding.root.transitionToEnd()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mainActivityRef.binding.progressBar.visibility = View.GONE
                    binding.webView.zoomOut()
                }

            }


            webChromeClient = object : WebChromeClient() {
                    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) { // for setting icon to our search bar
                        super.onReceivedIcon(view, icon)
                        try {
                            mainActivityRef.binding.webIcon.setImageBitmap(icon)
                            this@BrowseFragment.favicon = icon
                            MainActivity.bookmarkIndex=mainActivityRef.isBookmarked(view?.url!!)
                            if(MainActivity.bookmarkIndex!=-1){
                                val array = ByteArrayOutputStream()
                                icon!!.compress((Bitmap.CompressFormat.PNG),100,array)
                                MainActivity.bookmarkList[MainActivity.bookmarkIndex].image=array.toByteArray()
                            }
                        } catch (e: Exception) { }
                    }

                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        super.onShowCustomView(view, callback)
                        binding.webView.visibility = View.GONE
                        binding.custom.visibility = View.VISIBLE
                        binding.custom.addView(view)
                        mainActivityRef.binding.root.transitionToEnd()
                    }

                    override fun onHideCustomView() {
                        super.onHideCustomView()
                        binding.webView.visibility = View.VISIBLE
                        binding.custom.visibility = View.GONE
                    }

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        mainActivityRef.binding.progressBar.progress = newProgress
                    }
                }

            // using this animations in web view will work otherwise it will work in main activity only
            binding.webView.setOnTouchListener { _, motionEvent ->
                mainActivityRef.binding.root.onTouchEvent(motionEvent)
                return@setOnTouchListener false
            }
            binding.webView.reload()
        }
    }


    // this we are adding so tha the data built up in our browser will be gone just even if it is on pause in background
    // if we had done it under onDestroy  it will be there even if we keep the browser in the background so
    // so below will clear data
    override fun onPause() {
        super.onPause()

        (requireActivity() as MainActivity).saveBookmarks()
        // for clearing webView data
        binding.webView.apply {
            clearCache(true)  // true passed so that if there is some internal storage data that will also be cleared
            clearHistory()
            clearFormData()
            clearMatches()
            clearSslPreferences()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().removeAllCookies(null)
            }
            WebStorage.getInstance().deleteAllData()
        }

    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val result = binding.webView.hitTestResult
        when(result.type){
            WebView.HitTestResult.IMAGE_TYPE ->{
                menu.add("View Image")
                menu.add("Save Image")
                menu.add("Share")
                menu.add("Close")
            }
            WebView.HitTestResult.SRC_ANCHOR_TYPE, WebView.HitTestResult.ANCHOR_TYPE->{
                menu.add("Open in new tab")
                menu.add("Open Tab in Background")
                menu.add("Share")
                menu.add("Close")
            }
            WebView.HitTestResult.EDIT_TEXT_TYPE, WebView.HitTestResult.UNKNOWN_TYPE ->{}
            else->{
                menu.add("Open in new tab")
                menu.add("Open Tab in Background")
                menu.add("Share")
                menu.add("Close")
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val message = Handler().obtainMessage()
        binding.webView.requestFocusNodeHref(message)
        val url = message.data.getString("url")
        val imageUrl =message.data.getString("src")
        when(item.title){
            "Open in new tab" ->{
                changeTab(url.toString(),BrowseFragment(url.toString()))
            }
            "Open Tab in Background"->{
                changeTab(url.toString(),BrowseFragment(url.toString()),isBackground = true)
            }
            "View Image" ->{
                if(imageUrl!=null) {
                    if (imageUrl.contains("base64")){
                        val purebytes = imageUrl.substring(imageUrl.indexOf(",")+1)
                        val decodedbytes= Base64.decode(purebytes,Base64.DEFAULT)
                        val finalimage = BitmapFactory.decodeByteArray(decodedbytes,0,decodedbytes.size)

                        val imageView = ShapeableImageView(requireContext())
                        imageView.setImageBitmap(finalimage)

                        val imageDialog = MaterialAlertDialogBuilder(requireContext()).setView(imageView).create()
                        imageDialog.show()

                        imageView.layoutParams.width = Resources.getSystem().displayMetrics.widthPixels
                        imageView.layoutParams.height = (Resources.getSystem().displayMetrics.heightPixels*.75).toInt()
                        imageView.requestLayout()

                        imageDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    }
                    else changeTab(imageUrl,BrowseFragment(imageUrl))
                }
            }

            "Save Image"->{

                    if(imageUrl!=null) {
                        if (imageUrl.contains("base64")){
                            val purebytes = imageUrl.substring(imageUrl.indexOf(",")+1)
                            val decodedbytes= Base64.decode(purebytes,Base64.DEFAULT)
                            val finalimage = BitmapFactory.decodeByteArray(decodedbytes,0,decodedbytes.size)

                            MediaStore.Images.Media.insertImage(requireActivity().contentResolver,finalimage,"Image",null)

                            Snackbar.make(binding.root,"Image Saved Successfully",2000).show()

                        }
                        else startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(imageUrl)))
                    }
            }

            "Share" ->{
                val tempurl = url?: imageUrl
                if(tempurl!=null) {
                    if (tempurl.contains("base64")){
                        val purebytes = tempurl.substring(tempurl.indexOf(",")+1)
                        val decodedbytes= Base64.decode(purebytes,Base64.DEFAULT)
                        val finalimage = BitmapFactory.decodeByteArray(decodedbytes,0,decodedbytes.size)

                        val path = MediaStore.Images.Media.insertImage(requireActivity().contentResolver,finalimage,"Image",null)

                        ShareCompat.IntentBuilder(requireContext()).setChooserTitle("Sharing URL!").setType("image/*").setStream(Uri.parse(path)).startChooser()
                    }else{
                        ShareCompat.IntentBuilder(requireContext()).setChooserTitle("Sharing URL!").setType("text/plain").setText(url).startChooser()
                    }
                }
                else Snackbar.make(binding.root,"Not a Valid Link",2000).show()
            }
            "Close"->{}

        }

        return super.onContextItemSelected(item)
    }
}