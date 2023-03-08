package com.example.marcelo.fragments.manage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.marcelo.databinding.FragmentNewEventBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
            val datePicker = binding.datePicker
            val dateTxt = "${datePicker.dayOfMonth}/${datePicker.month + 1}/${datePicker.year}"
            println("Fecha $dateTxt")
            when{
                nameTxt.isEmpty() -> {
                    binding.nameEdittext.error = "Please enter name"
                    binding.nameEdittext.requestFocus()
                }
                dateTxt.isEmpty() -> {
                    Toast.makeText(context, "Please enter date", Toast.LENGTH_SHORT).show()
                    binding.datePicker.requestFocus()
                }else -> {
                    //create event with nameTxt as document id
                    val event = hashMapOf(
                        "name" to nameTxt,
                        "date" to dateTxt
                    )
                    db.collection("events").document(nameTxt).set(event)
                        .addOnSuccessListener {
                            findNavController().popBackStack()
                            Toast.makeText(context, "Event created", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            println("Error adding document $e")
                            Toast.makeText(context, "Error creating event", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}

