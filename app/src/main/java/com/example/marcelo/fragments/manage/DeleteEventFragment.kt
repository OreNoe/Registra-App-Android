package com.example.marcelo.fragments.manage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.marcelo.R
import com.example.marcelo.databinding.FragmentDeleteEventBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


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
                val event = binding.eventSpinner.selectedItem.toString()
                //delete all mails from the path /mail
                db.collection("mail").get().addOnSuccessListener { result ->
                    for (document in result) {
                        db.collection("mail").document(document.id).delete()
                    }
                }
                db.collection("events").document(event).delete()
                Toast.makeText(requireContext(), "Evento eliminado!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }else {Toast.makeText(requireContext(), "No hay eventos para eliminar!", Toast.LENGTH_SHORT).show()}
        }
    }
}