package net.justdave.nwsweatheralertswidget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertTypesFragment : Fragment() {

    private lateinit var nwsapi: NWSAPI
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.alert_types_fragment, container, false)
        recyclerView = view.findViewById(R.id.alert_types_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nwsapi = NWSAPI.getInstance(requireContext())

        CoroutineScope(Dispatchers.Main).launch {
            val alertTypes = nwsapi.getAlertTypes()
            recyclerView.adapter = AlertTypesAdapter(alertTypes)
        }
    }
}
