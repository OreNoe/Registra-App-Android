package com.example.marcelo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.marcelo.databinding.FragmentNewUserBinding
import com.example.marcelo.entities.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URLEncoder


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (auth.currentUser != null) {
            binding.continueButton.setOnClickListener {
                val emailEncargado = auth.currentUser?.email.toString()
                val nameTxt = binding.nameEdittext.text.toString()
                val surnameTxt = binding.surnameEdittext.text.toString()
                val emailTxt = binding.emailEdittext.text.toString()
                val lvlNum = binding.spinner.selectedItemPosition + 1
                val user = User(nameTxt, surnameTxt, emailTxt, lvlNum, emailEncargado)
                when{
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
                    else -> {

                        db.collection("users").add(user)
                            .addOnSuccessListener { documentReference ->
                                println("DocumentSnapshot added with ID: ${documentReference.id}")
                                Toast.makeText(context, "Usuario agregado", Toast.LENGTH_SHORT)
                                    .show()

                                val qrCodeEncoded = URLEncoder.encode(documentReference.id, "UTF-8")

                                var color = ""
                                when (lvlNum) {
                                    1 -> color = "cd7f32"
                                    2 -> color = "c0c0c0"
                                    3 -> color = "ffd700"
                                }

                                val mailData = hashMapOf(
                                    "to" to emailTxt,
                                    "message" to hashMapOf(
                                        "subject" to "Entrada evento!",
                                        "html" to (
                                                "<p>Hola ${nameTxt} ${surnameTxt}!</p>" +
                                                        "<p>Gracias por registrarte al evento!</p>" +
                                                        "<p>Para ingresar al evento escanea el siguiente codigo QR:</p>" +
                                                        "<p><img src=\"https://api.qrserver.com/v1/create-qr-code/?size=300x300&amp;data=${qrCodeEncoded}&amp;&bgcolor=${color}\" /></p>" +
                                                        "<p>Saludos!</p>"
                                        )
                                    )
                                )
                                //add mail data to firestore with the same id as the user
                                db.collection("mail").document(documentReference.id).set(mailData)
                                    .addOnSuccessListener {
                                        println("Mail added")
                                        Toast.makeText(context, "Mail enviado", Toast.LENGTH_SHORT).show()
                                        findNavController().popBackStack()
                                    }
                                    .addOnFailureListener { e ->
                                        println("Error adding mail $e")
                                        Toast.makeText(context, "Error al enviar mail", Toast.LENGTH_SHORT).show()
                                        findNavController().popBackStack()
                                    }
                            }
                            .addOnFailureListener { e ->
                                println("Error adding document $e")
                                Toast.makeText(context, "Error al agregar usuario", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            }
                    }
                }
            }
        }else{
            findNavController().popBackStack()
        }
    }
}
