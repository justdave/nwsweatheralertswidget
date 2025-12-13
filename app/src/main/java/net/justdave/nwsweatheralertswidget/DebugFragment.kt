package net.justdave.nwsweatheralertswidget

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.databinding.DebugFragmentBinding
import net.justdave.nwsweatheralertswidget.objects.NWSAlert
import net.justdave.nwsweatheralertswidget.objects.NWSArea
import net.justdave.nwsweatheralertswidget.objects.NWSZone
import net.justdave.nwsweatheralertswidget.widget.loadDebugPrefs
import net.justdave.nwsweatheralertswidget.widget.saveDebugPrefs

class DebugFragment : Fragment() {

    private lateinit var viewModel: DebugViewModel
    private var _binding: DebugFragmentBinding? = null
    private val binding get() = _binding!!

    private var cachedAlerts: List<NWSAlert>? = null
    private var fullJson: String = ""
    private var currentPage: Int = 0
    private val pageSize: Int = 2000 // characters

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("AlertsDisplayFragment", "onCreateView called")
        _binding = DebugFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[DebugViewModel::class.java]
        viewModel.initializeContext(requireActivity().applicationContext)
        binding.debugRecyclerView.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            val (savedAreaId, savedZoneId) = loadDebugPrefs(requireContext())
            viewModel.getAreaPopupContent {
                val areaAdapter = ArrayAdapter(requireContext(), R.layout.spinner_layout, it)
                binding.areaPopup.adapter = areaAdapter
                val areaIndex = it.indexOfFirst { area -> area.id == savedAreaId }
                binding.areaPopup.onItemSelectedListener = null
                binding.areaPopup.setSelection(if (areaIndex != -1) areaIndex else 0)

                val selectedArea = binding.areaPopup.selectedItem as NWSArea
                viewModel.getZonePopupContent(selectedArea) { zones ->
                    val zoneAdapter = ArrayAdapter(requireContext(), R.layout.spinner_layout, zones)
                    binding.zonePopup.adapter = zoneAdapter
                    val zoneIndex = zones.indexOfFirst { zone -> zone.id == savedZoneId }
                    binding.zonePopup.setSelection(if (zoneIndex != -1) zoneIndex else 0)
                    binding.areaPopup.onItemSelectedListener = areaSelectedListener
                }
            }
        }

        binding.displayFormatTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                renderDebugContent(cachedAlerts)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Do nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Do nothing
            }
        })

        binding.submitButton.setOnClickListener {
            val area = binding.areaPopup.selectedItem as NWSArea
            val zone = binding.zonePopup.selectedItem as NWSZone

            lifecycleScope.launch {
                val (savedAreaId, savedZoneId) = loadDebugPrefs(requireContext())
                if (cachedAlerts != null && savedAreaId == area.id && savedZoneId == zone.id) {
                    renderDebugContent(cachedAlerts)
                    return@launch
                }
            }

            binding.debugText.setText(R.string.loading)
            when (binding.displayFormatTabs.selectedTabPosition) {
                0, 1 -> {
                    binding.debugRecyclerView.visibility = View.GONE
                    binding.debugEmptyView.visibility = View.GONE
                    binding.debugTextScrollView.visibility = View.VISIBLE
                    binding.paginationControls.visibility = if (binding.displayFormatTabs.selectedTabPosition == 1) View.VISIBLE else View.GONE
                }
                2 -> {
                    binding.debugTextScrollView.visibility = View.GONE
                    binding.paginationControls.visibility = View.GONE
                    binding.debugRecyclerView.visibility = View.VISIBLE
                    binding.debugEmptyView.visibility = View.VISIBLE
                    binding.debugRecyclerView.adapter = DebugAlertsAdapter(emptyList())
                }
            }

            viewModel.getDebugContent(area, zone, {
                cachedAlerts = it
                lifecycleScope.launch {
                    saveDebugPrefs(requireContext(), area.id, zone.id)
                }
                fullJson = it.joinToString(separator = ",\n", prefix = "[\n", postfix = "\n]") { alert ->
                    alert.getRawDataForDisplay().prependIndent("  ")
                }
                currentPage = 0
                renderDebugContent(it)
            }, {
                cachedAlerts = null
                binding.debugRecyclerView.visibility = View.GONE
                binding.debugEmptyView.visibility = View.GONE
                binding.debugTextScrollView.visibility = View.VISIBLE
                binding.paginationControls.visibility = View.GONE
                binding.debugText.text = it.toString()
            })
        }
        binding.previousButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                renderDebugContent(cachedAlerts)
            }
        }
        binding.nextButton.setOnClickListener {
            val maxPage = (fullJson.length - 1) / pageSize
            if (currentPage < maxPage) {
                currentPage++
                renderDebugContent(cachedAlerts)
            }
        }
        binding.demoAllAlertsButton.setOnClickListener {
            findNavController().navigate(R.id.action_debugFragment_to_alertTypesFragment)
        }
        return binding.root
    }

    private val areaSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>,
            view: View?,
            position: Int,
            id: Long
        ) {
            val area = parent.getItemAtPosition(position) as NWSArea
            viewModel.getZonePopupContent(area) { zones ->
                val newZoneAdapter = ArrayAdapter(requireContext(), R.layout.spinner_layout, zones)
                binding.zonePopup.adapter = newZoneAdapter
                binding.zonePopup.setSelection(0)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    private fun renderDebugContent(alerts: List<NWSAlert>?) {
        if (alerts == null) return
        when (binding.displayFormatTabs.selectedTabPosition) {
            0 -> {
                binding.debugRecyclerView.visibility = View.GONE
                binding.debugEmptyView.visibility = View.GONE
                binding.debugTextScrollView.visibility = View.VISIBLE
                binding.paginationControls.visibility = View.GONE
                val sb = StringBuilder()
                sb.append("There are currently ").append(alerts.size).append(" active alerts")
                alerts.forEach { alert ->
                    sb.append("\n").append(alert.toString())
                }
                binding.debugText.setText(sb.toString(), TextView.BufferType.NORMAL)
            }
            1 -> {
                binding.debugRecyclerView.visibility = View.GONE
                binding.debugEmptyView.visibility = View.GONE
                binding.debugTextScrollView.visibility = View.VISIBLE
                binding.paginationControls.visibility = View.VISIBLE
                val start = currentPage * pageSize
                val end = (start + pageSize).coerceAtMost(fullJson.length)
                binding.debugText.text = fullJson.substring(start, end)
                binding.previousButton.isEnabled = currentPage > 0
                binding.nextButton.isEnabled = end < fullJson.length
            }
            2 -> {
                binding.debugTextScrollView.visibility = View.GONE
                binding.paginationControls.visibility = View.GONE
                if (alerts.isEmpty()) {
                    binding.debugRecyclerView.visibility = View.GONE
                    binding.debugEmptyView.visibility = View.VISIBLE
                } else {
                    binding.debugRecyclerView.visibility = View.VISIBLE
                    binding.debugEmptyView.visibility = View.GONE
                    binding.debugRecyclerView.adapter = DebugAlertsAdapter(alerts)
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
