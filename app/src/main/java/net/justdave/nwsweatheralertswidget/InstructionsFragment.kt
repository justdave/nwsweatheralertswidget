package net.justdave.nwsweatheralertswidget

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class InstructionsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_instructions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.about_button).setOnClickListener {
            showAboutDialog(requireContext(), findNavController())
        }

        view.findViewById<Button>(R.id.debug_button).setOnClickListener {
            findNavController().navigate(R.id.debugFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        // Show the exact alarm permission card on Android 12+ if the permission is not granted.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val card = view?.findViewById<CardView>(R.id.exact_alarm_card)
            if (!alarmManager.canScheduleExactAlarms()) {
                card?.visibility = View.VISIBLE
                val button = view?.findViewById<Button>(R.id.grant_alarm_permission_button)
                button?.setOnClickListener {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                }
            } else {
                card?.visibility = View.GONE
            }
        }
    }
}
