package com.example.newsreading.fragments.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.newsreading.databinding.FragmentNewsBinding
import com.example.newsreading.fragments.tabs.AppleFragment
import com.example.newsreading.fragments.tabs.TechCrunchFragment
import com.example.newsreading.fragments.tabs.TeslaFragment
import com.example.newsreading.fragments.tabs.USRightNowFragment
import com.example.newsreading.fragments.tabs.WallStreetJournalFragment
import com.google.android.material.tabs.TabLayoutMediator

class NewsFragment : Fragment() {
    private lateinit var binding: FragmentNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Обработка аргументов, если необходимо
        arguments?.let {
            // Ваш код здесь
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager
        val adapter = NewsPagerAdapter(requireActivity())

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Tesla"
                1 -> "Wall\nStreet\nJournal"
                2 -> "Apple"
                3 -> "US\nright\nnow"
                4 -> "Tech\nCrunch"
                else -> null
            }
        }.attach()

        viewPager.currentItem = 0
    }

    class NewsPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        private val fragments = listOf(
            TeslaFragment(),
            WallStreetJournalFragment(),
            AppleFragment(),
            USRightNowFragment(),
            TechCrunchFragment()
        )

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}