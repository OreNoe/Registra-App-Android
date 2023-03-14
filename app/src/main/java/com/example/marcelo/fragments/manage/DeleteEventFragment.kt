package com.example.marcelo.fragments.manage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.marcelo.R
import com.example.marcelo.databinding.FragmentDeleteEventBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL


class DeleteEventFragment : Fragment() {
    private var _binding: FragmentDeleteEventBinding? = null
    private val binding get() = _binding!!

    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeleteEventBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.progressBar.visibility = View.VISIBLE
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db.collection("events")
            .get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                val events = mutableListOf<String>()
                for (document in result) {
                    events.add(document.id)
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, events)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.eventSpinner.adapter = adapter
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }

        binding.btnDeleteEvent.setOnClickListener {
            if (!binding.eventSpinner.adapter.isEmpty) {
                val event = com.example.marcelo.entities.Event(
                    binding.eventSpinner.selectedItem.toString(),"")
                //delete all mails from the path /mail
                //recursive delete all users from the path event/users and /mail
                db.collection("events").document(event.name).delete()
                db.collection("events").document(event.name).collection("users").get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            db.collection("events").document(event.name).collection("users").document(document.id).delete()
                            db.collection("mail").document(document.id).delete()
                        }
                    }
                    .addOnFailureListener { exception ->
                        println("Error getting documents: $exception")
                    }
                binding.progressBar.visibility = View.VISIBLE
                binding.btnDeleteEvent.isEnabled = false
                CoroutineScope(Dispatchers.IO).launch {
                    deleteEvent(event.name)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Evento eliminado!", Toast.LENGTH_SHORT).show()
                        binding.btnDeleteEvent.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        findNavController().popBackStack()
                    }
                }
            }else {Toast.makeText(requireContext(), "No hay eventos para eliminar!", Toast.LENGTH_SHORT).show()}
        }
    }

    suspend fun deleteEvent(eventName: String) {
        val url = URL("https://registra-app.uc.r.appspot.com/deleteEvent")
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