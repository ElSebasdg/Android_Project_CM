package pt.ua.androidproj.viewmodels

import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class TemperatureViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // LiveData para a temperatura de pinos
    private val _temperatureUpdates = MutableLiveData<Pair<String, Double>>() // Pair<pinId, temperatura>
    val temperatureUpdates: LiveData<Pair<String, Double>> = _temperatureUpdates

    // Iniciar simulação de temperatura para um pino específico
    fun startTemperatureSimulation(pinId: String) {
        viewModelScope.launch {
            while (true) {
                // Gerar um valor aleatório de temperatura
                val newTemperature = Random.nextDouble(15.0, 35.0)

                // Atualizar LiveData para observar mudanças no Fragment
                _temperatureUpdates.postValue(Pair(pinId, newTemperature))

                // Atualizar o Firestore com a nova temperatura
                db.collection("pins").document(pinId)
                    .update("temperature", newTemperature)
                    .addOnSuccessListener {
                        println("Temperatura atualizada no Firestore: $newTemperature")
                    }
                    .addOnFailureListener { e ->
                        println("Erro ao atualizar temperatura: ${e.message}")
                    }

                // Esperar 5 segundos
                delay(5000)
            }
        }
    }
}
