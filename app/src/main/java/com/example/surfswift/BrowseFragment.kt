package com.example.surfswift

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.surfswift.databinding.FragmentBrowseBinding
import com.example.surfswift.databinding.FragmentHomeBinding
import java.lang.Exception

class BrowseFragment(private var URL: String) : Fragment() {

    lateinit var binding: FragmentBrowseBinding  // object of browse fragment binding
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_browse, container, false)  // we need to create a view first for binding fragments
        binding = FragmentBrowseBinding.bind(view);   //  then we pass that view to the bind function of the fragment binding

        return view   // at last we return the view
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()

        val mainActivityRef= requireActivity() as MainActivity

        binding.webView.apply {

            //by default webview has javascript disabled so we should enable it as below
            settings.javaScriptEnabled=true

            settings.setSupportZoom(true)
            settings.builtInZoomControls=true // just by swipe in and swipe out we should be able to do zoom in and zoom out
            settings.displayZoomControls=false // plus and minus sign wont be showed for zooming

            // if we do not use these webclient and webChromeClient, we will be redirected to google browser when we swipe left and search anything in that webview of the below url of google search page
            webViewClient= object: WebViewClient(){
                override fun doUpdateVisitedHistory(view: WebView?,url: String?, isReload: Boolean) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    mainActivityRef.binding.topSearchBar.text=SpannableStringBuilder(url) // this spannable string builder will make the url editable as the url is string type so we need to make it editable
                    // after writing the above method everytime we search something url will be shown at the top search bar just as it does in any other browser

                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    mainActivityRef.binding.progressBar.progress=0
                    mainActivityRef.binding.progressBar.visibility=View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    mainActivityRef.binding.progressBar.visibility=View.GONE
                }
            }


            webChromeClient= object : WebChromeClient(){ // this is used to watch something in full screen
                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) { // for setting icon to our search bar
                    super.onReceivedIcon(view, icon)
                    try{
                        mainActivityRef.binding.webIcon.setImageBitmap(icon)
                    }catch (e:Exception){}
                }
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)
                    binding.webView.visibility=View.GONE
                    binding.custom.visibility=View.VISIBLE
                    binding.custom.addView(view)
                }

                override fun onHideCustomView() {
                    super.onHideCustomView()
                    binding.webView.visibility=View.VISIBLE
                    binding.custom.visibility=View.GONE
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    mainActivityRef.binding.progressBar.progress=newProgress
                }
            }

            when{
                URLUtil.isValidUrl(URL)->loadUrl(URL)  // if the url is valid it will load
                URL.contains(".com", ignoreCase = true)->loadUrl(URL)  // if something like youtube.com is entered
                else-> loadUrl("https://www.google.com/search?q=$URL")  // if just something like hello
                // or any string is input which is not a valid url of site the our URL variable will be appended to
                // the last of the https://www.google.com/search?q   which is the format of searching on google  so google
                //will be used as the default search engine
            }

        }
    }

}