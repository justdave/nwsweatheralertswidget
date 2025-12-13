package net.justdave.nwsweatheralertswidget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.noties.markwon.Markwon

class PrivacyPolicyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.privacy_policy_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById<TextView>(R.id.privacy_policy_text)
        val markdown = requireContext().assets.open("PRIVACY.md").bufferedReader().use { it.readText() }

        // get an instance of Markwon
        val markwon = Markwon.create(requireContext())

        // set markdown
        markwon.setMarkdown(textView, markdown)
    }
}
