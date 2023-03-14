package com.example.marcelo.fragments.manage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.marcelo.databinding.FragmentNewEventBinding
import com.example.marcelo.entities.Event
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class NewEventFragment : Fragment() {
    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewEventBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.progressBar.visibility = View.GONE
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createEventButton.setOnClickListener {
            val nameTxt = binding.nameEdittext.text.toString().replace("\\s".toRegex(), "")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            val datePicker = binding.datePicker
            val dateTxt = "${datePicker.dayOfMonth}/${datePicker.month + 1}/${datePicker.year}"
            println("Fecha $dateTxt")
            when {
                nameTxt.isEmpty() -> {
                    binding.nameEdittext.error = "Please enter name"
                    binding.nameEdittext.requestFocus()
                }
                dateTxt.isEmpty() -> {
                    Toast.makeText(context, "Please enter date", Toast.LENGTH_SHORT).show()
                    binding.datePicker.requestFocus()
                }
                else -> {
                    //create event with nameTxt as document id
                    val event = Event(nameTxt, dateTxt)
                    db.collection("events").document(nameTxt).set(event)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Event created", Toast.LENGTH_SHORT).show()
                            binding.progressBar.visibility = View.VISIBLE
                            binding.createEventButton.isEnabled = false
                            binding.datePicker.visibility = View.GONE
                            GlobalScope.launch(Dispatchers.IO) {
                                addEvent(nameTxt)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Event created", Toast.LENGTH_SHORT)
                                        .show()
                                    binding.progressBar.visibility = View.GONE
                                    binding.createEventButton.isEnabled = true
                                    binding.datePicker.visibility = View.VISIBLE
                                    findNavController().popBackStack()
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            println("Error adding document $e")
                            Toast.makeText(context, "Error creating event", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
        }
    }

    suspend fun addEvent(eventName: String) {
        val url = URL("https://registra-app.uc.r.appspot.com/addEvent")
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        val json = JSONObject()
        json.put("eventName", eventName)
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

