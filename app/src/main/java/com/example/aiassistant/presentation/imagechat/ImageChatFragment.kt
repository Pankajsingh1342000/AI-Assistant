package com.example.aiassistant.presentation.imagechat

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
import com.example.aiassistant.data.manager.CameraManager
import com.example.aiassistant.databinding.FragmentImageChatBinding
import com.example.aiassistant.utils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class ImageChatFragment : Fragment() {

    private var _binding: FragmentImageChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ImageChatViewModel by viewModels()

    @Inject
    lateinit var cameraManager: CameraManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true &&
            permissions[Manifest.permission.RECORD_AUDIO] == true
        ) {
            cameraManager.startCamera(
                lifecycleOwner = viewLifecycleOwner,
                previewView = binding.cameraPreview,
                onError = { error ->
                    binding.statusText.text = error
                }
            )

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentImageChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
        binding.captureButton.setOnClickListener {
            val file = File(requireContext().cacheDir, "image.jpg")
            cameraManager.captureImage(
                file,
                onCaptured = {
                    viewModel.startVoiceAndImageFlow(file)
                },
                onError = { error ->
                    binding.statusText.text = "Capture failed: $error"
                }
            )
        }
        observeState()
    }

    private fun observeState() {
        viewModel.uiState.collectWhenStarted(viewLifecycleOwner) { state->
            when (state) {
                is ImageChatUiState.Idle -> {
                    binding.statusText.text = "Tap to ask about an image"
                    binding.progressBar.isVisible = false
                }
                is ImageChatUiState.Listening -> {
                    binding.statusText.text = "Listening..."
                    binding.progressBar.isVisible = false
                }
                is ImageChatUiState.Processing -> {
                    binding.statusText.text = "Heard: ${state.input}"
                    binding.progressBar.isVisible = true
                }
                is ImageChatUiState.Capturing -> {
                    binding.statusText.text = "Capturing image..."
                    binding.progressBar.isVisible = true
                }
                is ImageChatUiState.Loading -> {
                    binding.statusText.text = "Thinking..."
                    binding.progressBar.isVisible = true
                }
                is ImageChatUiState.Success -> {
                    binding.statusText.text = state.response
                    binding.progressBar.isVisible = false
                }
                is ImageChatUiState.Error -> {
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