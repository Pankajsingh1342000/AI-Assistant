package com.example.aiassistant.presentation.voicechat

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.aiassistant.R
import com.example.aiassistant.databinding.FragmentVoiceChatBinding
import com.example.aiassistant.utils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VoiceChatFragment : Fragment() {

    private var _binding: FragmentVoiceChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VoiceChatViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startVoiceInput()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentVoiceChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.micButton.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        collectUiState()
    }

    private fun collectUiState() {
        viewModel.uiState.collectWhenStarted(viewLifecycleOwner) { state ->
            when (state) {
                is VoiceChatUiState.Idle -> {
                    binding.statusText.text = "Tap mic to speak"
                    binding.progressBar.isVisible = false
                }

                is VoiceChatUiState.Listening -> {
                    binding.statusText.text = "Listening..."
                    binding.progressBar.isVisible = false
                }

                is VoiceChatUiState.Processing -> {
                    binding.statusText.text = "Heard: ${state.input}"
                    binding.progressBar.isVisible = true
                }

                is VoiceChatUiState.Loading -> {
                    binding.statusText.text = "Thinking..."
                    binding.progressBar.isVisible = true
                }

                is VoiceChatUiState.Success -> {
                    binding.statusText.text = state.response
                    binding.progressBar.isVisible = false
                }

                is VoiceChatUiState.Error -> {
                    binding.statusText.text = "Error: ${state.message}"
                    binding.progressBar.isVisible = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}