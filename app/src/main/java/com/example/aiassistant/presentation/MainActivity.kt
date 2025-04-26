package com.example.aiassistant.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.aiassistant.R
import com.example.aiassistant.databinding.ActivityMainBinding
import com.example.aiassistant.presentation.imagechat.ImageChatFragment
import com.example.aiassistant.presentation.voicechat.VoiceChatFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentTabId = R.id.voiceChatFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            switchToFragment(R.id.voiceChatFragment)
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != currentTabId) {
                switchToFragment(item.itemId)
            }
            true
        }
    }

    private fun switchToFragment(itemId: Int) {
        val fragment = when (itemId) {
            R.id.voiceChatFragment -> VoiceChatFragment()
            R.id.imageChatFragment -> ImageChatFragment()
            else -> null
        }

        fragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_container, it)
                .commitAllowingStateLoss()
            currentTabId = itemId
        }
    }

    override fun onBackPressed() {

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_container)

        if (navHostFragment?.childFragmentManager?.backStackEntryCount == 0) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }
}
