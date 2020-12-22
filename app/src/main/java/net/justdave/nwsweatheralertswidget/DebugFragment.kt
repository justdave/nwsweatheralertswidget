package net.justdave.nwsweatheralertswidget

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.databinding.DebugFragmentBinding

class DebugFragment : Fragment() {

    private lateinit var viewModel: DebugViewModel
    private lateinit var binding: DebugFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DebugFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DebugViewModel::class.java)
        viewModel.initializeContext(requireActivity().applicationContext)
        lifecycleScope.launch {
            try {
                viewModel.getDebugText { response ->
                    Log.i("DebugFragment","setting the result text")
                    binding.debugText.setText(response, TextView.BufferType.NORMAL)
                }
            } finally {
                // foo
            }
        }
    }

}