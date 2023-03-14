package com.example.marcelo.fragments.manage

import android.R
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.marcelo.databinding.FragmentNewUserBinding
import com.example.marcelo.entities.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*


class NewUserFragment : Fragment() {
    private var _binding: FragmentNewUserBinding? = null
    private val binding get() = _binding!!

    val auth = Firebase.auth

    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewUserBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.progressBar.visibility = View.GONE
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db.collection("events")
            .get()
            .addOnSuccessListener { result ->
                val events = mutableListOf<String>()
                for (document in result) {
                    events.add(document.id)
                }
                val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, events)
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                binding.eventSpinner.adapter = adapter
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }

        if (auth.currentUser != null) {
            binding.continueButton.setOnClickListener {
                val nameTxt = binding.nameEdittext.text.toString().trim()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                val surnameTxt = binding.surnameEdittext.text.toString().trim()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                val emailTxt = binding.emailEdittext.text.toString().trim()
                val emailEncargado = auth.currentUser?.email.toString().trim()
                val lvlNum = binding.levelSpinner.selectedItemPosition + 1
                val user = User(nameTxt, surnameTxt, emailTxt, lvlNum, emailEncargado)
                when {
                    nameTxt.isEmpty() -> {
                        binding.nameEdittext.error = "Please enter name"
                        binding.nameEdittext.requestFocus()
                    }
                    surnameTxt.isEmpty() -> {
                        binding.surnameEdittext.error = "Please enter surname"
                        binding.surnameEdittext.requestFocus()
                    }
                    emailTxt.isEmpty() -> {
                        binding.emailEdittext.error = "Please enter email"
                        binding.emailEdittext.requestFocus()
                    }
                    binding.eventSpinner.adapter.isEmpty -> {
                        Toast.makeText(requireContext(), "No hay eventos disponibles, crea uno primero", Toast.LENGTH_LONG).show()
                        binding.eventSpinner.requestFocus()
                    }
                    else -> {
                        binding.continueButton.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                        db.collection("events")
                            .document(binding.eventSpinner.selectedItem.toString())
                            .collection("users").add(user)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(
                                    requireContext(),
                                    "User added successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                db.collection("events")
                                    .document(binding.eventSpinner.selectedItem.toString()).get()
                                    .addOnSuccessListener { document ->
                                        val event = document.data
                                        val eventName = event?.get("name").toString()
                                        val eventDate = event?.get("date").toString()
                                        var color = ""
                                        when (lvlNum) {
                                            1 -> color = "cd7f32"
                                            2 -> color = "c0c0c0"
                                            3 -> color = "ffd700"
                                        }
                                        val qrCodeEncoded =
                                            URLEncoder.encode(documentReference.id, "UTF-8")
                                        val qrCode =
                                            "<p><img src=\"https://quickchart.io/qr?text=${qrCodeEncoded}&amp;size=300&amp;centerImageUrl=https://i.imgur.com/d4walK4.jpeg\" style=\"height:300px; max-width:100%; width:300px\" /></p>"

                                        val mailData = hashMapOf(
                                            "to" to emailTxt,
                                            "message" to hashMapOf(
                                                "subject" to "Registro en evento",
                                                "html" to "<p>Estimado ${nameTxt} ${surnameTxt},</p><p>Te has registrado en el evento ${eventName} con fecha ${eventDate}.</p>"+
                                                        "<p>Este es tu código QR:</p>${qrCode}<p>Saludos,</p><p>Equipo de Eventos</p>"
                                            )
                                        )
                                        db.collection("mail").document(documentReference.id)
                                            .set(mailData)
                                            .addOnSuccessListener { ref ->

                                            }
                                            .addOnFailureListener { e ->
                                                println("Error adding document: $e")
                                            }
                                        CoroutineScope(Dispatchers.IO).launch() {
                                            addUserToEvent(eventName, nameTxt, surnameTxt, emailTxt, lvlNum, emailEncargado)
                                            withContext(Dispatchers.Main) {
                                                binding.continueButton.isEnabled = true
                                                binding.progressBar.visibility = View.GONE
                                                findNavController().popBackStack()
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        println("Error adding document: $e")
                                        Toast.makeText(
                                            requireContext(),
                                            "Error adding user",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        findNavController().popBackStack()
                                    }
                            }
                    }
                }
            }
        } else {
            findNavController().popBackStack()
        }
    }

    suspend fun deleteUser(eventName: String, name: String, surname: String, email: String, lvl: Int, encargado: String) {
        val url = URL("https://registra-app.uc.r.appspot.com/deleteUser")
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
        json.put("lvl", lvl.toString())
        json.put("encargado", encargado)
        println(json.toString())
        val wr = DataOutputStream(connection.outputStream)
        withContext(Dispatchers.IO) {
            wr.writeBytes(json.toString())
            wr.flush()
            wr.close()
        }
        val responseCode = connection.responseCode
        Log.d("TAG", "Response Code : $responseCode")
    }

    suspend fun addUserToEvent(eventName: String, name: String, surname: String, email: String, lvl: Int, encargado: String) {
        val url = URL("https://registra-app.uc.r.appspot.com/addUserToEvent")
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
