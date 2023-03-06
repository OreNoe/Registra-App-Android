package com.example.marcelo.fragments.manage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.marcelo.databinding.FragmentNewSellerBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class NewSellerFragment : Fragment() {
    private var _binding: FragmentNewSellerBinding? = null
    private val binding get() = _binding!!

    var auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewSellerBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onStart() {
        super.onStart()
        binding.btnLogin.setOnClickListener {
            val emailTxt = binding.etEmail.text.toString()
            val passwordTxt = binding.etPassword.text.toString()
            when{
                emailTxt.isEmpty() -> {
                    binding.etEmail.error = "Please enter email"
                    binding.etEmail.requestFocus()
                }
                passwordTxt.isEmpty() -> {
                    if (passwordTxt.length < 6) {
                        binding.etPassword.error = "Password must be at least 6 characters"
                        binding.etPassword.requestFocus()
                    } else {
                        binding.etPassword.error = "Please enter password"
                        binding.etPassword.requestFocus()
                    }
                }
                else -> {
                    auth.createUserWithEmailAndPassword(emailTxt, passwordTxt)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                findNavController().popBackStack()
                            } else {
                                Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }
}