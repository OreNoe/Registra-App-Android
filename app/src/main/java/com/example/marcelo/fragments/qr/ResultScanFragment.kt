package com.example.marcelo.fragments.qr

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.marcelo.R
import com.example.marcelo.databinding.FragmentResultScanBinding
import com.example.marcelo.entities.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ResultScanFragment : Fragment() {

    private var _binding: FragmentResultScanBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultScanBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.progressBar.visibility = View.VISIBLE
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val docRef = arguments?.getString("docRef").toString()
        val event = arguments?.getString("event").toString()

        db.collection("events").document(event).collection("users").document(docRef).get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE
                if (document != null) {
                    if (document.getString("name") != null){
                        val user = document.toObject(User::class.java)
                        Log.d("TAG", "DocumentSnapshot data: ${document.data}")

                        when (user?.lvl) {
                            1 -> {
                                binding.backgroundView.setBackgroundColor(resources.getColor(R.color.bronze))
                                binding.levelTextview.text = "Level: Bronze"
                            }
                            2 -> {
                                binding.backgroundView.setBackgroundColor(resources.getColor(R.color.silver))
                                binding.levelTextview.text = "Level: Silver"
                            }
                            3 -> {
                                binding.backgroundView.setBackgroundColor(resources.getColor(R.color.gold))
                                binding.levelTextview.text = "Level: Gold"
                            }
                        }

                        binding.nameTextview.text = "Name: " + user?.name
                        binding.surnameTextview.text = "Surname: " + user?.surname

                        db.collection("events").document(event).collection("users").document(docRef)
                            .delete()
                            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully deleted!") }
                            .addOnFailureListener { e -> Log.w("TAG", "Error deleting document", e) }

                        //delete mail from database
                        db.collection("mails").document(docRef)
                            .delete()
                            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully deleted!") }
                            .addOnFailureListener { e -> Log.w("TAG", "Error deleting document", e) }

                        readAgain()
                    }else{ noExistUser() }
                } else { noExistUser() }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
            }
    }

    private fun readAgain() {
        view?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun noExistUser() {
        Log.d("TAG", "No such document")
        binding.backgroundView.setBackgroundColor(resources.getColor(R.color.red))
        binding.nameTextview.text = "No such "
        binding.surnameTextview.text = "user"
        binding.levelTextview.text = "in database"
        readAgain()
    }
}
