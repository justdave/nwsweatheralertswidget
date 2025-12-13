package net.justdave.nwsweatheralertswidget

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.justdave.nwsweatheralertswidget.objects.NWSAlert

class DebugAlertsAdapter(private val alerts: List<NWSAlert>) :
    RecyclerView.Adapter<DebugAlertsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView: ImageView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.alert_item_text)
            imageView = view.findViewById(R.id.alert_item_icon)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.alerts_widget_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val alert = alerts[position]
        viewHolder.textView.text = alert.event
        viewHolder.imageView.setImageResource(alert.getIcon())
        viewHolder.itemView.setBackgroundResource(alert.getBackground())
        viewHolder.itemView.setOnClickListener { view ->
            val context = view.context
            val intent = Intent(context, AlertDetailActivity::class.java).apply {
                putExtra("alert", alert)
            }
            context.startActivity(intent)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = alerts.size

}
