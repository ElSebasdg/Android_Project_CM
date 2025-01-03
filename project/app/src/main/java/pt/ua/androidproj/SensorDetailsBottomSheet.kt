package pt.ua.androidproj

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val sensorTypeTextView: TextView = view.findViewById(R.id.sensorTypeTextView)
        val sensorValueTextView: TextView = view.findViewById(R.id.sensorValueTextView)

        // Observa mudanças no LiveData para atualizar os dados exibidos
        mapViewModel.latestSensorValue.observe(viewLifecycleOwner) { sensorData ->
            val (type, value) = sensorData
            sensorTypeTextView.text = "Tipo de Sensor: $type"
            sensorValueTextView.text = "Valor Atual: $value"
        }

        // Inicia a busca dos dados mais recentes do sensor
        mapViewModel.fetchLatestSensorData(latitude, longitude)

        return view
    }


}
