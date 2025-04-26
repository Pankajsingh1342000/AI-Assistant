package com.example.aiassistant.presentation.imagechat

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.aiassistant.R
import com.example.aiassistant.data.manager.CameraManager
import com.example.aiassistant.databinding.FragmentImageChatBinding
import com.example.aiassistant.utils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class ImageChatFragment : Fragment() {

    private var _binding: FragmentImageChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ImageChatViewModel by viewModels()

    @Inject
    lateinit var cameraManager: CameraManager

    private var cameraPermissionGranted = false
    private var isCameraActive = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraPermissionGranted = permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentImageChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideCamera()

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )

        binding.captureButton.setOnClickListener {
            if (!cameraPermissionGranted) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
                return@setOnClickListener
            }

            if (!isCameraActive) {
                showCamera()
            } else {
                captureImageAndAsk()
            }
        }

        observeState()
    }

    private fun showCamera() {
        binding.cameraPreview.visibility = View.VISIBLE
        cameraManager.startCamera(
            lifecycleOwner = viewLifecycleOwner,
            previewView = binding.cameraPreview,
            onError = { error ->
                binding.statusText.text = "Camera error: $error"
            }
        )
        isCameraActive = true
    }

    private fun hideCamera() {
        cameraManager.stopCamera()
        binding.cameraPreview.visibility = View.GONE
        isCameraActive = false
    }

    private fun captureImageAndAsk() {
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

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collectWhenStarted(viewLifecycleOwner) { state ->
                when (state) {
                    is ImageChatUiState.Idle -> {
                        binding.statusText.text = "Tap to ask about an image"
                    }
                    is ImageChatUiState.Listening -> {
                        binding.statusText.text = "Listening..."
                    }
                    is ImageChatUiState.Processing -> {
                        binding.statusText.text = "Heard: ${state.input}"
                    }
                    is ImageChatUiState.Capturing -> {
                        binding.statusText.text = "Capturing image..."
                    }
                    is ImageChatUiState.Loading -> {
                        binding.statusText.text = "Thinking..."
                        hideCamera()
                    }
                    is ImageChatUiState.Success -> {
                        binding.statusText.text = state.response
//                        hideCamera()
                    }
                    is ImageChatUiState.Error -> {
                        binding.statusText.text = "Error: ${state.message}"
                        hideCamera()
                    }
                    is ImageChatUiState.HighlightByRange -> highlightWordInRange(state.response, state.start, state.end)
                }
            }
        }
    }

    private fun highlightWordInRange(text: String, start: Int, end: Int) {
        val spannable = android.text.SpannableString(text)
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.micBackground)
            ),
            start, end,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
            start, end,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.statusText.text = spannable

        scrollToHighlighted(start)
    }

    private fun scrollToHighlighted(start: Int) {
        binding.statusText.post {
            val layout = binding.statusText.layout
            if (layout != null) {
                val line = layout.getLineForOffset(start)
                val lineTop = layout.getLineTop(line)
                val lineBottom = layout.getLineBottom(line)

                val centerY = (lineTop + lineBottom) / 2
                val scrollViewHeight = binding.scrollView.height
                val scrollY = centerY - scrollViewHeight / 2

                binding.scrollView.smoothScrollTo(0, scrollY.coerceAtLeast(0))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}