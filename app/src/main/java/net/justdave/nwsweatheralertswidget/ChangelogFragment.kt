package net.justdave.nwsweatheralertswidget

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.noties.markwon.Markwon

class ChangelogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_changelog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById<TextView>(R.id.changelog_text)
        val markwon = Markwon.create(requireContext())

        try {
            val markdown = requireContext().assets.open("CHANGES.md").bufferedReader().use { it.readText() }
            markwon.setMarkdown(textView, markdown)
            textView.movementMethod = LinkMovementMethod.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
            textView.setText(R.string.changelog_error)
        }
    }
}
