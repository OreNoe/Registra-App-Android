package com.example.marcelo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.marcelo.R
import com.example.marcelo.databinding.FragmentLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    val auth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            findNavController().navigate(R.id.action_loginFragment_to_menuFragment)
        }

        binding.btnLogin.setOnClickListener {
            val emailTxt = binding.etEmail.text.toString()
            val passwordTxt = binding.etPassword.text.toString()
            when{
                emailTxt.isEmpty() -> {
                    binding.etEmail.error = "Please enter email"
                    binding.etEmail.requestFocus()
                }
                passwordTxt.isEmpty() -> {
                    binding.etPassword.error = "Please enter password"
                    binding.etPassword.requestFocus()
                }
                else -> {
                    auth.signInWithEmailAndPassword(emailTxt, passwordTxt)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                findNavController().navigate(R.id.action_loginFragment_to_menuFragment)
                            } else {
                                Toast.makeText(
                                    requireContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
    }
}
