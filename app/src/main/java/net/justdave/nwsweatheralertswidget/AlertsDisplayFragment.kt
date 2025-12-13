package net.justdave.nwsweatheralertswidget

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
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
        (requireActivity() as MenuHost).addMenuProvider(object: MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.alerts_display_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.option_debug -> {
                        findNavController().navigate(AlertsDisplayFragmentDirections.actionAlertsDisplayFragmentToDebugFragment())
                        return true
                    }
                    R.id.action_about -> {
                        showAboutDialog(requireActivity(), findNavController())
                        return true
                    }

                }
                return false
            }
        })

        binding = AlertsDisplayFragmentBinding.inflate(inflater, container, false)
        binding.parsedEvents.emptyView = binding.emptytext

        Log.i("AlertsDisplayFragment", "loaded")

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AlertsDisplayViewModel::class.java]
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

}
