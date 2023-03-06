package com.example.marcelo.fragments.qr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.marcelo.databinding.FragmentSelectQrReadingBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SelectQrReadingFragment : Fragment() {
    private var _binding: FragmentSelectQrReadingBinding? = null
    private val binding get() = _binding!!

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectQrReadingBinding.inflate(inflater, container, false)
        val view = binding.root
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

        binding.scanQrButton.setOnClickListener {
            val event = binding.eventSpinner.selectedItem.toString()
            val action =
                SelectQrReadingFragmentDirections.actionSelectQrReadingFragmentToReadQrFragment(
                    event
                )
            findNavController().navigate(action)
        }
    }
}