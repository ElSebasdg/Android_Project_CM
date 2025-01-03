package pt.ua.androidproj

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem

class MapViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _latestSensorValue = MutableLiveData<Pair<String, Double>>()
    val latestSensorValue: LiveData<Pair<String, Double>> = _latestSensorValue

    fun fetchLatestSensorData(latitude: Double, longitude: Double) {
        db.collection("pins")
            .whereEqualTo("latitude", latitude)
            .whereEqualTo("longitude", longitude)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents[0]
                    val sensors = document["sensors"] as? List<Map<String, Any>>
                    if (sensors != null && sensors.isNotEmpty()) {
                        val sensor = sensors[0]
                        val values = sensor["value"] as? List<Double>
                        val typeSensor = sensor["typeSensor"] as? String
                        if (values != null && values.isNotEmpty() && typeSensor != null) {
                            _latestSensorValue.value = typeSensor to values.last()
                        } else {
                            _latestSensorValue.value = "Erro nos valores do sensor" to -1.0
                        }
                    } else {
                        _latestSensorValue.value = "Nenhum sensor encontrado" to -1.0
                    }
                } else {
                    _latestSensorValue.value = "Nenhum documento encontrado" to -1.0
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapViewModel", "Erro ao buscar sensor: ${exception.message}")
                _latestSensorValue.value = "Erro ao buscar sensor" to -1.0
            }
    }

    fun savePinToFirestore(latitude: Double, longitude: Double) {
        val pinData = mapOf(
            "latitude" to latitude,
            "longitude" to longitude
        )
        db.collection("pins").add(pinData)
            .addOnSuccessListener { Log.d("MapViewModel", "Pino salvo com sucesso.") }
            .addOnFailureListener { Log.e("MapViewModel", "Erro ao salvar pino: ${it.message}") }
    }

        fun loadPins(callback: (List<OverlayItem>) -> Unit) {
            db.collection("pins").get()
                .addOnSuccessListener { result ->
                    val pins = result.documents.mapNotNull { doc ->
                        val latitude = doc.getDouble("latitude")
                        val longitude = doc.getDouble("longitude")
                        if (latitude != null && longitude != null) {
                            OverlayItem("Firestore Pin", "Pino carregado", GeoPoint(latitude, longitude))
                        } else null
                    }
                    callback(pins)
                }
                .addOnFailureListener {
                    Log.e("MapViewModel", "Erro ao carregar pinos: ${it.message}")
                    callback(emptyList())
                }
        }
    }
