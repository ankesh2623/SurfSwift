package com.example.surfswift.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.surfswift.activity.MainActivity
import com.example.surfswift.R
import com.example.surfswift.databinding.FragmentBrowseBinding
import java.io.ByteArrayOutputStream
import java.lang.Exception

class BrowseFragment(private var URL: String) : Fragment() {


    lateinit var binding: FragmentBrowseBinding  // object of browse fragment binding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
            R.layout.fragment_browse,
            container,
            false
        )  // we need to create a view first for binding fragments
        binding =
            FragmentBrowseBinding.bind(view);   //  then we pass that view to the bind function of the fragment binding

        return view   // at last we return the view
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        val mainActivityRef = requireActivity() as MainActivity

        binding.webView.apply {

            //by default webview has javascript disabled so we should enable it as below
            settings.javaScriptEnabled = true

            settings.setSupportZoom(true)
            settings.builtInZoomControls =
                true // just by swipe in and swipe out we should be able to do zoom in and zoom out
            settings.displayZoomControls = false // plus and minus sign wont be showed for zooming

            // if we do not use these webclient and webChromeClient, we will be redirected to google browser when we swipe left and search anything in that webview of the below url of google search page
            webViewClient = object : WebViewClient() {


                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                    if (MainActivity.isDesktopSite) {
                        view?.evaluateJavascript(
                            "document.querySelector('meta[name=\"viewport\"]').setAttribute('content', " +
                                    "'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));",
                            null
                        )
                    }
                }

                override fun doUpdateVisitedHistory(
                    view: WebView?,
                    url: String?,
                    isReload: Boolean
                ) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    mainActivityRef.binding.topSearchBar.text =
                        SpannableStringBuilder(url) // this spannable string builder will make the url editable as the url is string type so we need to make it editable
                    // after writing the above method everytime we search something url will be shown at the top search bar just as it does in any other browser

                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    mainActivityRef.binding.progressBar.progress = 0
                    mainActivityRef.binding.progressBar.visibility = View.VISIBLE
                    if (url!!.contains(
                            "tube",
                            ignoreCase = false
                        )
                    ) mainActivityRef.binding.root.transitionToEnd()
                    if (url.contains(
                            "tv",
                            ignoreCase = false
                        )
                    ) mainActivityRef.binding.root.transitionToEnd()
                    if (url.contains(
                            "video",
                            ignoreCase = false
                        )
                    ) mainActivityRef.binding.root.transitionToEnd()
                    if (url.contains(
                            "netflix",
                            ignoreCase = false
                        )
                    ) mainActivityRef.binding.root.transitionToEnd()
                    if (url.contains(
                            "disney",
                            ignoreCase = false
                        )
                    ) mainActivityRef.binding.root.transitionToEnd()
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mainActivityRef.binding.progressBar.visibility = View.GONE
                    binding.webView.zoomOut()
                }
            }


            webChromeClient =
                object : WebChromeClient() { // this is used to watch something in full screen
                    override fun onReceivedIcon(
                        view: WebView?,
                        icon: Bitmap?
                    ) { // for setting icon to our search bar
                        super.onReceivedIcon(view, icon)
                        try {
                            mainActivityRef.binding.webIcon.setImageBitmap(icon)

                            MainActivity.bookmarkIndex=mainActivityRef.isBookmarked(view?.url!!)
                            if(MainActivity.bookmarkIndex!=-1){
                                val array = ByteArrayOutputStream()
                                icon!!.compress((Bitmap.CompressFormat.PNG),100,array)
                                MainActivity.bookmarkList[MainActivity.bookmarkIndex].image=array.toByteArray()
                            }
                        } catch (e: Exception) {
                        }
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

            when {
                URLUtil.isValidUrl(URL) -> loadUrl(URL)  // if the url is valid it will load
                URL.contains(
                    ".com",
                    ignoreCase = true
                ) -> loadUrl(URL)  // if something like youtube.com is entered
                else -> loadUrl("https://www.google.com/search?q=$URL")  // if just something like hello
                // or any string is input which is not a valid url of site the our URL variable will be appended to
                // the last of the https://www.google.com/search?q   which is the format of searching on google  so google
                //will be used as the default search engine
            }

            // using this animations in web view will work otherwise it will work in main activity only
            binding.webView.setOnTouchListener { _, motionEvent ->
                mainActivityRef.binding.root.onTouchEvent(motionEvent)
                return@setOnTouchListener false;
            }
        }
    }


    // this we are adding so tha the data built up in our browser will be gone just even if it is on pause in background
    // if we had done it under onDestroy  it will be there even if we keep the browser in the background so
    // so below will clear data
    override fun onPause() {
        super.onPause()
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


}