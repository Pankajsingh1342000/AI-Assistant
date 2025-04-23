package com.example.aiassistant.presentation.assistant

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
import com.example.aiassistant.databinding.FragmentAssistantBinding
import com.example.aiassistant.utils.collectWhenStarted

class AssistantFragment : Fragment() {

    private var _binding: FragmentAssistantBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AssistantViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if(granted) viewModel.startVoiceInput()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = FragmentAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.micButton.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.uiState.collectWhenStarted(viewLifecycleOwner) { state ->
            when (state) {
                is AssistantUiState.Idle -> {
                    binding.statusText.text = "Tap mic to speak"
                    binding.progressBar.isVisible = false
                }

                is AssistantUiState.Listening -> {
                    binding.statusText.text = "Listening..."
                    binding.progressBar.isVisible = false
                }

                is AssistantUiState.Processing -> {
                    binding.statusText.text = "Heard: ${state.input}"
                    binding.progressBar.isVisible = true
                }

                is AssistantUiState.Loading -> {
                    binding.statusText.text = "Thinking..."
                    binding.progressBar.isVisible = true
                }

                is AssistantUiState.Success -> {
                    binding.statusText.text = state.response
                    binding.progressBar.isVisible = false
                }

                is AssistantUiState.Error -> {
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