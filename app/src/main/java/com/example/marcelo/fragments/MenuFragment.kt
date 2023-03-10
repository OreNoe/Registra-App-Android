package com.example.marcelo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.marcelo.R
import com.example.marcelo.databinding.FragmentMenuBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MenuFragment : Fragment() {
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    val auth = Firebase.auth
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        val view = binding.root

        if (auth.currentUser?.email == "rosanomarcelo84@gmail.com") {
            binding.btnDeleteEvent.visibility = View.VISIBLE
            binding.btnNewSeller.visibility = View.VISIBLE
            binding.btnNewEvent.visibility = View.VISIBLE
        }else {
            binding.btnDeleteEvent.visibility = View.GONE
            binding.btnNewSeller.visibility = View.GONE
            binding.btnNewEvent.visibility = View.GONE
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnScanQr.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_selectQrReadingFragment)
        }
        binding.btnCreateUser.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_newUserFragment)
        }
        binding.btnDeleteEvent.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_deleteEventFragment)
        }
        binding.btnExit.setOnClickListener {
            auth.signOut()
            findNavController().popBackStack()
        }

        binding.btnNewSeller.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_newSellerFragment)
        }

        binding.btnNewEvent.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_newEventFragment)
        }

    }
}