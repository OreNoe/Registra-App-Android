package com.example.marcelo.fragments.manage

import android.R
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.marcelo.databinding.FragmentNewUserBinding
import com.example.marcelo.entities.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.checkerframework.checker.nullness.qual.NonNull
import java.io.ByteArrayOutputStream
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
                val emailEncargado = auth.currentUser?.email.toString()
                val nameTxt = binding.nameEdittext.text.toString()
                val surnameTxt = binding.surnameEdittext.text.toString()
                val emailTxt = binding.emailEdittext.text.toString()
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
                                        findNavController().popBackStack()
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
}
