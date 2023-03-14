package com.example.marcelo.fragments.qr

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.marcelo.R
import com.example.marcelo.databinding.FragmentResultScanBinding
import com.example.marcelo.entities.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

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
                if (document != null) {
                    if (document.getString("name") != null){
                        if (document.getBoolean("active") == true){
                            val user = document.toObject(User::class.java)
                            Log.d("TAG", "DocumentSnapshot data: ${document.data}")

                            CoroutineScope(Dispatchers.IO).launch {
                                if (user != null) {
                                    updateUser(event, user.name, user.surname, user.email, user.lvl, user.encargado, "true", requireContext())
                                }
                                withContext(Dispatchers.Main){
                                    binding.progressBar.visibility = View.GONE
                                    binding.nameTextview.text = "Name: " + user?.name
                                    binding.surnameTextview.text = "Surname: " + user?.surname
                                }
                            }

                            when (user?.lvl) {
                                1 -> {
                                    binding.backgroundView.setBackgroundColor(resources.getColor(R.color.bronze))
                                    binding.levelTextview.text = "General"
                                }
                                2 -> {
                                    binding.backgroundView.setBackgroundColor(resources.getColor(R.color.silver))
                                    binding.levelTextview.text = "VIP"
                                }
                                3 -> {
                                    binding.backgroundView.setBackgroundColor(resources.getColor(R.color.gold))
                                    binding.levelTextview.text = "Invitado"
                                }
                            }
                            db.collection("events").document(event).collection("users").document(docRef).update("active", false, "status", true,"timestamp", System.currentTimeMillis())
                            readAgain()
                        }else{ noExistUser() }
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
    suspend fun updateUser(eventName: String, name: String, surname: String, email: String, lvl: Int, encargado: String, status: String, context: Context){
        val url = URL("https://registra-app.uc.r.appspot.com/updateUser")
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        val json = JSONObject()
        json.put("eventName", eventName)
        json.put("name", name)
        json.put("surname", surname)
        json.put("email", email)
        json.put("lvl", lvl)
        json.put("encargado", encargado)
        json.put("status", status)
        val wr = DataOutputStream(connection.outputStream)
        withContext(Dispatchers.IO) {
            wr.writeBytes(json.toString())
            wr.flush()
            wr.close()
        }
        val responseCode = connection.responseCode
        Log.d("TAG", "Response Code : $responseCode")
    }
}
