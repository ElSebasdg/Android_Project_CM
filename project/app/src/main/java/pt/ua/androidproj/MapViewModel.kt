package pt.ua.androidproj

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem
import kotlin.random.Random

class MapViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // LiveData para exibir dados do sensor
    private val _latestSensorValue = MutableLiveData<List<Pair<String, Double>>>()
    val latestSensorValue: LiveData<List<Pair<String, Double>>> = _latestSensorValue


    // Coroutine para simulação
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Função para buscar os dados mais recentes de um sensor no Firestore.
     */

    fun fetchLatestSensorData(latitude: Double, longitude: Double) {
        Log.d("MapViewModel", "Buscando sensores para: ($latitude, $longitude)")
        db.collection("pins")
            .whereEqualTo("latitude", latitude)
            .whereEqualTo("longitude", longitude)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents[0]
                    val sensors = document["sensors"] as? List<Map<String, Any>>
                    if (sensors != null && sensors.isNotEmpty()) {
                        val latestSensorData = sensors.mapNotNull { sensor ->
                            val typeSensor = sensor["typeSensor"] as? String
                            val values = sensor["value"] as? List<Double>
                            if (typeSensor != null && values != null && values.isNotEmpty()) {
                                Pair(typeSensor, values.last())
                            } else null
                        }
                        _latestSensorValue.postValue(latestSensorData)
                    } else {
                        _latestSensorValue.postValue(emptyList()) // Nenhum sensor encontrado
                    }
                } else {
                    Log.e("MapViewModel", "Nenhum documento encontrado para o pino.")
                    _latestSensorValue.postValue(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapViewModel", "Erro ao buscar sensores: ${exception.message}")
                _latestSensorValue.postValue(emptyList())
            }
    }



    /**
     * Função para salvar novos dados de pino no Firestore.
     */
    fun savePinToFirestore(latitude: Double, longitude: Double) {
        val pinData = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "sensors" to listOf(
                mapOf(
                    "typeSensor" to "temperatura",
                    "value" to listOf<Double>(),
                    "timestamp" to listOf<Long>()
                )
            )
        )
        db.collection("pins").add(pinData)
            .addOnSuccessListener { Log.d("MapViewModel", "Pino salvo com sucesso.") }
            .addOnFailureListener { Log.e("MapViewModel", "Erro ao salvar pino: ${it.message}") }
    }

    /**
     * Função para carregar todos os pinos do Firestore.
     */
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

    /**
     * Função para iniciar a simulação de um sensor.
     */
    fun startSensorSimulation(latitude: Double, longitude: Double) {
        ioScope.launch {
            while (isActive) {
                db.collection("pins")
                    .whereEqualTo("latitude", latitude)
                    .whereEqualTo("longitude", longitude)
                    .get()
                    .addOnSuccessListener { result ->
                        if (!result.isEmpty) {
                            val document = result.documents[0]
                            val sensors = document["sensors"] as? List<Map<String, Any>> ?: return@addOnSuccessListener
                            val updatedSensors = sensors.map { sensor ->
                                val typeSensor = sensor["typeSensor"] as? String ?: return@map sensor
                                val values = (sensor["value"] as? MutableList<Double>)?.toMutableList() ?: mutableListOf()
                                val timestamps = (sensor["timestamp"] as? MutableList<Long>)?.toMutableList() ?: mutableListOf()

                                // Adiciona novos valores aleatórios
                                values.add(Random.nextDouble(20.0, 30.0)) // Exemplo de faixa de valores
                                timestamps.add(System.currentTimeMillis())

                                sensor.toMutableMap().apply {
                                    this["value"] = values
                                    this["timestamp"] = timestamps
                                }
                            }

                            // Atualiza os sensores no Firestore
                            db.collection("pins").document(document.id)
                                .update("sensors", updatedSensors)
                                .addOnSuccessListener {
                                    Log.d("MapViewModel", "Sensores atualizados com sucesso.")
                                }
                                .addOnFailureListener {
                                    Log.e("MapViewModel", "Erro ao atualizar sensores: ${it.message}")
                                }
                        }
                    }
                delay(5000) // Atualiza os valores a cada 5 segundos
            }
        }
    }
    /**
     * Função para atualizar os dados de um sensor no Firestore.
     */

    private fun updateSensorData(latitude: Double, longitude: Double, value: Int, timestamp: Long) {
        Log.d("MapViewModel", "Atualizando sensor para: ($latitude, $longitude) com valor $value e timestamp $timestamp")
        db.collection("pins")
            .whereEqualTo("latitude", latitude)
            .whereEqualTo("longitude", longitude)
            .get()
            .addOnSuccessListener { result ->
                Log.d("MapViewModel", "Documentos encontrados para atualização: ${result.size()}")
                if (!result.isEmpty) {
                    val document = result.documents[0]
                    val sensors = document["sensors"] as? List<Map<String, Any>> ?: listOf()

                    // Atualiza o primeiro sensor da lista
                    val sensor = sensors.getOrNull(0)?.toMutableMap() ?: mutableMapOf(
                        "typeSensor" to "temperatura",
                        "value" to mutableListOf<Double>(),
                        "timestamp" to mutableListOf<Long>()
                    )

                    val values = (sensor["value"] as? MutableList<Double>) ?: mutableListOf()
                    val timestamps = (sensor["timestamp"] as? MutableList<Long>) ?: mutableListOf()

                    values.add(value.toDouble())
                    timestamps.add(timestamp)

                    sensor["value"] = values
                    sensor["timestamp"] = timestamps

                    val updatedSensors = sensors.toMutableList()
                    if (updatedSensors.isEmpty()) {
                        updatedSensors.add(sensor)
                    } else {
                        updatedSensors[0] = sensor
                    }

                    db.collection("pins").document(document.id)
                        .update("sensors", updatedSensors)
                        .addOnSuccessListener {
                            Log.d("MapViewModel", "Sensor atualizado com sucesso no Firestore.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("MapViewModel", "Erro ao atualizar sensor: ${e.message}")
                        }
                } else {
                    Log.e("MapViewModel", "Erro: Nenhum documento encontrado para atualizar o sensor.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapViewModel", "Erro ao buscar pino para atualização: ${exception.message}")
            }
    }


    /**
     * Cancelar a simulação ao encerrar o ViewModel.
     */
    override fun onCleared() {
        super.onCleared()
        ioScope.cancel() // Cancelar o job ao encerrar o ViewModel
    }
}
