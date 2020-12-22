package net.justdave.nwsweatheralertswidget

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import net.justdave.nwsweatheralertswidget.databinding.AlertsDisplayFragmentBinding

class AlertsDisplayFragment : Fragment() {

    private lateinit var viewModel: AlertsDisplayViewModel
    private lateinit var binding: AlertsDisplayFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)

        binding = AlertsDisplayFragmentBinding.inflate(inflater, container, false)
        binding.parsedEvents.emptyView = binding.emptytext

        Log.i("AlertsDisplayFragment", "loaded")

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AlertsDisplayViewModel::class.java)
        viewModel.initializeContext(requireActivity().applicationContext)
/*        viewModel.initializeRequestQueue(requireActivity().applicationContext)
        lifecycleScope.launch {
            try {
                viewModel.getEmptyText { response ->
                    binding.emptytext.setText(response, TextView.BufferType.NORMAL)
                }
            } finally {
                // foo
            }
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.alerts_display_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.option_debug -> {
                findNavController().navigate(AlertsDisplayFragmentDirections.actionAlertsDisplayFragmentToDebugFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

