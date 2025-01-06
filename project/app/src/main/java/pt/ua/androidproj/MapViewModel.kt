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
    private val _latestSensorValueWithUnit = MutableLiveData<List<Triple<String, Double, String>>>()
    val latestSensorValueWithUnit: LiveData<List<Triple<String, Double, String>>> = _latestSensorValueWithUnit

    // Coroutine para simulação
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Função para buscar os dados mais recentes de um sensor no Firestore.
     */

    fun fetchLatestSensorDataWithUnit(latitude: Double, longitude: Double) {
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
                            val unit = sensor["unit"] as? String ?: "" // Pega a unidade (ou vazio)
                            if (typeSensor != null && values != null && values.isNotEmpty()) {
                                Triple(typeSensor, values.last(), unit) // Inclui tipo, valor e unidade
                            } else null
                        }
                        _latestSensorValueWithUnit.postValue(latestSensorData)
                    } else {
                        _latestSensorValueWithUnit.postValue(emptyList()) // Nenhum sensor encontrado
                    }
                } else {
                    Log.e("MapViewModel", "Nenhum documento encontrado para o pino.")
                    _latestSensorValueWithUnit.postValue(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapViewModel", "Erro ao buscar sensores: ${exception.message}")
                _latestSensorValueWithUnit.postValue(emptyList())
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

                                // Gera valores aleatórios com base no tipo de sensor
                                val newValue = when (typeSensor) {
                                    "temperatura" -> (20..35).random().toDouble() // Temperatura em °C
                                    "humidade" -> (30..70).random().toDouble()    // Humidade em %
                                    else -> 0.0
                                }

                                // Adiciona o novo valor e timestamp
                                values.add(newValue)
                                timestamps.add(System.currentTimeMillis())

                                // Retorna o sensor atualizado
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
