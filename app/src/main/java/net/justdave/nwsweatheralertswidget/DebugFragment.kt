package net.justdave.nwsweatheralertswidget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.databinding.DebugFragmentBinding
import net.justdave.nwsweatheralertswidget.objects.NWSArea
import net.justdave.nwsweatheralertswidget.objects.NWSZone

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[DebugViewModel::class.java]
        viewModel.initializeContext(requireActivity().applicationContext)
        lifecycleScope.launch {
            try {
                viewModel.getAreaPopupContent { response ->
                    val adapter = ArrayAdapter(
                        requireActivity().applicationContext,
                        R.layout.spinner_layout,
                        response
                    )
                    binding.areaPopup.adapter = adapter
                    binding.areaPopup.setSelection(0)
                }
                viewModel.getZonePopupContent(binding.areaPopup.selectedItem as NWSArea) { response ->
                    binding.zonePopup.adapter = ArrayAdapter(
                        requireActivity().applicationContext,
                        R.layout.spinner_layout,
                        response
                    )
                    binding.zonePopup.setSelection(0)
                }
                binding.areaPopup.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (parent.getItemAtPosition(position) == "") {
                                binding.areaPopup.setSelection(0)
                            } else {
                                val area = parent.getItemAtPosition(position) as NWSArea
                                val loadingMenu = ArrayList<NWSZone>()
                                loadingMenu.add(NWSZone("all","Loading..."))
                                binding.zonePopup.adapter = ArrayAdapter(
                                    requireActivity().applicationContext,
                                    R.layout.spinner_layout,
                                    loadingMenu
                                )
                                viewModel.getZonePopupContent(area) { response ->
                                    binding.zonePopup.adapter = ArrayAdapter(
                                        requireActivity().applicationContext,
                                        R.layout.spinner_layout,
                                        response
                                    )
                                    binding.zonePopup.setSelection(0)
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                binding.zonePopup.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (parent.getItemAtPosition(position) == "") {
                                binding.areaPopup.setSelection(0)
                            } else {
                                val area = binding.areaPopup.selectedItem as NWSArea
                                val zone = parent.getItemAtPosition(position) as NWSZone
                                binding.debugText.setText(R.string.loading)

                                viewModel.getDebugContent(area, zone) { response ->
                                    binding.debugText.setText(response.toString(), TextView.BufferType.NORMAL)
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
            } finally {
                // foo
            }
        }
    }

}