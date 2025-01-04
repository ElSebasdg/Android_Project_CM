package pt.ua.androidproj

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SensorDetailsBottomSheet : BottomSheetDialogFragment() {

    private val mapViewModel: MapViewModel by viewModels()

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    companion object {
        fun newInstance(latitude: Double, longitude: Double): SensorDetailsBottomSheet {
            val fragment = SensorDetailsBottomSheet()
            fragment.latitude = latitude
            fragment.longitude = longitude
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sensor_details, container, false)
        val sensorContainer: LinearLayout = view.findViewById(R.id.sensorContainer)

        mapViewModel.latestSensorValue.observe(viewLifecycleOwner) { sensorData ->
            // Limpa os dados antigos
            sensorContainer.removeAllViews()

            if (sensorData.isNotEmpty()) {
                // Adiciona os sensores dinamicamente
                sensorData.forEach { (type, value) ->
                    val textView = TextView(requireContext()).apply {
                        text = "$type: $value"
                        textSize = 16f
                        setPadding(8, 8, 8, 8)
                    }
                    sensorContainer.addView(textView)
                }
            } else {
                // Exibe mensagem de nenhum sensor encontrado
                val textView = TextView(requireContext()).apply {
                    text = "Nenhum sensor encontrado."
                    textSize = 16f
                    setPadding(8, 8, 8, 8)
                }
                sensorContainer.addView(textView)
            }
        }

        // Inicia a busca dos sensores
        mapViewModel.fetchLatestSensorData(latitude, longitude)

        return view
    }


}
