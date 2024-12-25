package com.example.newsreading

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.newsreading.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.fragmentContainerView)

        binding.bottomNavigationView.setOnItemSelectedListener {
            item ->
            when (item.itemId){
                R.id.nav_newsFragment -> {
                    navController.navigate(R.id.nav_newsFragment)
                    true
                }
                R.id.nav_bookmarkFragment ->{
                    navController.navigate(R.id.nav_bookmarkFragment)
                    true
                }
                else -> false
            }
        }
    }
}