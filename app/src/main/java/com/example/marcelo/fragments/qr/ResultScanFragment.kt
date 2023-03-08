package com.example.marcelo.fragments.qr

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.marcelo.R
import com.example.marcelo.databinding.FragmentResultScanBinding
import com.example.marcelo.entities.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ResultScanFragment : Fragment() {

    private var _binding: FragmentResultScanBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore

    private var mWebView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultScanBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.progressBar.visibility = View.VISIBLE
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val docRef = arguments?.getString("docRef").toString()
        val event = arguments?.getString("event").toString()

        db.collection("events").document(event).collection("users").document(docRef).get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE
                if (document != null) {
                    if (document.getString("name") != null){
                        val user = document.toObject(User::class.java)
                        Log.d("TAG", "DocumentSnapshot data: ${document.data}")

                        when (user?.lvl) {
                            1 -> {
                                binding.backgroundView.setBackgroundColor(resources.getColor(R.color.bronze))
                                binding.levelTextview.text = "Level: Bronze"
                            }
                            2 -> {
                                binding.backgroundView.setBackgroundColor(resources.getColor(R.color.silver))
                                binding.levelTextview.text = "Level: Silver"
                            }
                            3 -> {
                                binding.backgroundView.setBackgroundColor(resources.getColor(R.color.gold))
                                binding.levelTextview.text = "Level: Gold"
                            }
                        }

                        binding.nameTextview.text = "Name: " + user?.name
                        binding.surnameTextview.text = "Surname: " + user?.surname

                        //update user in database
                        db.collection("events").document(event).collection("users").document(docRef)
                            .update("status", true)
                            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }

                        //delete mail collection
                        db.collection("mail").document(docRef).delete()
                            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully deleted!") }
                            .addOnFailureListener { e -> Log.w("TAG", "Error deleting document", e) }
                        readAgain()
                    }else{ noExistUser() }
                } else { noExistUser() }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
            }
    }

    private fun readAgain() {
        view?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun noExistUser() {
        Log.d("TAG", "No such document")
        binding.backgroundView.setBackgroundColor(resources.getColor(R.color.red))
        binding.nameTextview.text = "No such "
        binding.surnameTextview.text = "user"
        binding.levelTextview.text = "in database"
        readAgain()
    }

    private fun doWebViewPrint() {
        // Create a WebView object specifically for printing
        val webView = activity?.let { WebView(it) }
        webView?.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false

            override fun onPageFinished(view: WebView, url: String) {
                Log.i(TAG, "page finished loading $url")
                createWebPrintJob(view)
                mWebView = null
            }
        }

        // Generate an HTML document on the fly:
        val htmlDocument =
            "<html><body><h1>Test Content</h1><p>Testing, testing, testing...</p></body></html>"
        webView?.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView
    }

    private fun createWebPrintJob(webView: WebView) {

        // Get a PrintManager instance
        (activity?.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->

            val jobName = "${getString(R.string.app_name)} Document"

            // Get a print adapter instance
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            // Create a print job with name and adapter instance
            printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder().build()
            ).also { printJob ->

                // Save the job object for later status checking
                var printJobs = printJob
            }
        }
    }


}
