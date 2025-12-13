package net.justdave.nwsweatheralertswidget

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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

        val notificationInstructionsTitle = view.findViewById<TextView>(R.id.notification_instructions_title)
        val tiramisuInstructions = view.findViewById<TextView>(R.id.notification_instructions_tiramisu)
        val android12Instructions = view.findViewById<TextView>(R.id.notification_instructions_android_12)
        val oreoInstructions = view.findViewById<TextView>(R.id.notification_instructions_oreo)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationInstructionsTitle.visibility = View.VISIBLE
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    tiramisuInstructions.visibility = View.VISIBLE
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    android12Instructions.visibility = View.VISIBLE
                }
                else -> {
                    oreoInstructions.visibility = View.VISIBLE
                }
            }
        }

        view.findViewById<Button>(R.id.about_button).setOnClickListener {
            val aboutDialog = AboutDialog(requireContext())
            aboutDialog.show()
        }

        view.findViewById<Button>(R.id.debug_button).setOnClickListener {
            findNavController().navigate(R.id.debugFragment)
        }
    }
}
