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
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.databinding.DebugFragmentBinding
import net.justdave.nwsweatheralertswidget.objects.NWSAlert
import net.justdave.nwsweatheralertswidget.objects.NWSArea
import net.justdave.nwsweatheralertswidget.objects.NWSZone

class DebugFragment : Fragment() {

    private lateinit var viewModel: DebugViewModel
    private var _binding: DebugFragmentBinding? = null
    private val binding get() = _binding!!

    private var cachedAlerts: List<NWSAlert>? = null
    private var cachedArea: String? = null
    private var cachedZone: String? = null
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
                binding.displayFormatGroup.setOnCheckedChangeListener { _, _ -> renderDebugContent(cachedAlerts) }
                binding.submitButton.setOnClickListener {
                    val area = binding.areaPopup.selectedItem as NWSArea
                    val zone = binding.zonePopup.selectedItem as NWSZone

                    if (cachedArea == area.id && cachedZone == zone.id) {
                        renderDebugContent(cachedAlerts)
                        return@setOnClickListener
                    }

                    binding.debugText.setText(R.string.loading)
                    when (binding.displayFormatGroup.checkedRadioButtonId) {
                        R.id.radio_text, R.id.radio_json -> {
                            binding.debugRecyclerView.visibility = View.GONE
                            binding.debugTextScrollView.visibility = View.VISIBLE
                            binding.paginationControls.visibility = if (binding.displayFormatGroup.checkedRadioButtonId == R.id.radio_json) View.VISIBLE else View.GONE
                        }
                        R.id.radio_widget -> {
                            binding.debugTextScrollView.visibility = View.GONE
                            binding.paginationControls.visibility = View.GONE
                            binding.debugRecyclerView.visibility = View.VISIBLE
                            binding.debugRecyclerView.adapter = DebugAlertsAdapter(emptyList())
                        }
                    }

                    viewModel.getDebugContent(area, zone, { response ->
                        cachedAlerts = response
                        cachedArea = area.id
                        cachedZone = zone.id
                        fullJson = response.joinToString(separator = ",\n", prefix = "[\n", postfix = "\n]") { alert ->
                            alert.getRawDataForDisplay().prependIndent("  ")
                        }
                        currentPage = 0
                        renderDebugContent(response)
                    }, { error ->
                        cachedAlerts = null
                        cachedArea = null
                        cachedZone = null
                        fullJson = ""
                        currentPage = 0
                        binding.debugRecyclerView.visibility = View.GONE
                        binding.debugTextScrollView.visibility = View.VISIBLE
                        binding.paginationControls.visibility = View.GONE
                        binding.debugText.setText(error.toString(), TextView.BufferType.NORMAL)
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
            } finally {
                // foo
            }
        }
        val view = binding.root
        return view
    }

    private fun renderDebugContent(alerts: List<NWSAlert>?) {
        if (alerts == null) return
        when (binding.displayFormatGroup.checkedRadioButtonId) {
            R.id.radio_text -> {
                binding.debugRecyclerView.visibility = View.GONE
                binding.debugTextScrollView.visibility = View.VISIBLE
                binding.paginationControls.visibility = View.GONE
                val sb = StringBuilder()
                sb.append("There are currently ").append(alerts.size).append(" active alerts")
                alerts.forEach { alert ->
                    sb.append("\n").append(alert.toString())
                }
                binding.debugText.setText(sb.toString(), TextView.BufferType.NORMAL)
            }
            R.id.radio_json -> {
                binding.debugRecyclerView.visibility = View.GONE
                binding.debugTextScrollView.visibility = View.VISIBLE
                binding.paginationControls.visibility = View.VISIBLE
                val start = currentPage * pageSize
                val end = (start + pageSize).coerceAtMost(fullJson.length)
                binding.debugText.text = fullJson.substring(start, end)
                binding.previousButton.isEnabled = currentPage > 0
                binding.nextButton.isEnabled = end < fullJson.length
            }
            R.id.radio_widget -> {
                binding.debugTextScrollView.visibility = View.GONE
                binding.paginationControls.visibility = View.GONE
                binding.debugRecyclerView.visibility = View.VISIBLE
                binding.debugRecyclerView.adapter = DebugAlertsAdapter(alerts)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
