package com.example.aiassistant.presentation.voicechat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.aiassistant.R
import com.example.aiassistant.databinding.FragmentVoiceChatBinding
import com.example.aiassistant.utils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class VoiceChatFragment : Fragment() {

    private var _binding: FragmentVoiceChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VoiceChatViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startVoiceInput()
            showListeningState()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentVoiceChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.micButton.setOnClickListener {
            val bounceAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
            binding.micButton.startAnimation(bounceAnim)

            binding.statusText.text = ""
            binding.thinkingAnimationView.visibility = View.GONE
            binding.responseCard.visibility = View.GONE
            binding.layoutCard.visibility = View.GONE
            binding.micWaveView.visibility = View.VISIBLE

            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
        viewModel.setAmplitudeListener { amp ->
            binding.micWaveView.updateAmplitude(amp)
        }
        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collectWhenStarted(viewLifecycleOwner) { state ->
                when (state) {
                    is VoiceChatUiState.Listening -> showListeningState()
                    is VoiceChatUiState.Loading -> showLoadingState()
                    is VoiceChatUiState.Success -> showResponse(state.response)
                    is VoiceChatUiState.Error -> showError(state.message)
                    is VoiceChatUiState.HighlightByRange -> highlightWordInRange(state.response, state.start, state.end)
                    else -> Unit
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


    private fun showListeningState() {
        animateGlow(true)
        binding.layoutCard.visibility = View.GONE
        binding.responseCard.visibility = View.GONE
        binding.thinkingAnimationView.visibility = View.GONE
        binding.micWaveView.visibility = View.VISIBLE
    }

    private fun showLoadingState() {
        binding.micWaveView.stop()
        binding.micWaveView.visibility = View.GONE
        binding.responseCard.visibility = View.GONE
        binding.thinkingAnimationView.visibility = View.VISIBLE
        binding.layoutCard.visibility = View.VISIBLE
    }

    private fun showResponse(response: String) {
        animateGlow(false)
        binding.micWaveView.stop()
        binding.micWaveView.visibility = View.GONE
        binding.thinkingAnimationView.visibility = View.GONE
        binding.layoutCard.visibility = View.VISIBLE
        binding.responseCard.visibility = View.VISIBLE
        binding.statusText.text = response
    }

    private fun showError(message: String) {
        animateGlow(false)
        binding.micWaveView.stop()
        binding.micWaveView.visibility = View.GONE
        binding.thinkingAnimationView.visibility = View.GONE
        binding.layoutCard.visibility = View.VISIBLE
        binding.responseCard.visibility = View.VISIBLE
        binding.statusText.text = message
    }

    private fun animateGlow(show: Boolean) {
        binding.micGlow.animate()
            .alpha(if (show) 1f else 0f)
            .setDuration(300)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}