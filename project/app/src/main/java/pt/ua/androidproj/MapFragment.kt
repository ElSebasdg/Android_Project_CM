package pt.ua.androidproj

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem

class MapFragment : Fragment() {
    private var mapView: MapView? = null
    private lateinit var mapContainer: FrameLayout
    private val overlayItems = mutableListOf<OverlayItem>()
    private lateinit var mapViewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.fragment_map, container, false)

        // Inicializar o ViewModel
        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        // Observar mudanças no LiveData
        mapViewModel.latestSensorValue.observe(viewLifecycleOwner) { sensorData ->
            val (type, value) = sensorData
            Toast.makeText(requireContext(), "$type: $value", Toast.LENGTH_SHORT).show()
        }

        // Configuração do mapa
        Configuration.getInstance().userAgentValue = requireContext().packageName
        mapContainer = layout.findViewById(R.id.mapContainer)
        setupMap()

        // Configurar botão de refresh
        val refreshButton: FloatingActionButton = layout.findViewById(R.id.refreshButton)
        refreshButton.setOnClickListener {
            refreshMap() // Recarregar o mapa ao pressionar o botão
        }

        return layout
    }

    private fun setupMap() {
        // Inicializa o MapView
        mapView = MapView(requireContext()).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.apply {
                setZoom(15.0)
                setCenter(GeoPoint(40.6405, -8.6538))
            }
        }

        // Remove o mapa anterior, se houver, e adiciona o novo
        mapContainer.removeAllViews()
        mapContainer.addView(mapView)

        // Adiciona overlay para capturar toques no mapa
        val overlay = object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val geoPoint = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt())
                addPin(geoPoint) // Adiciona um novo pino
                return true
            }

            override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {}
        }
        mapView?.overlays?.add(overlay)

        loadPinsFromFirestore() // Carregar os pinos existentes
    }

    fun refreshMap() {
        setupMap() // Recria o mapa e recarrega os pinos
    }

    private fun addPin(geoPoint: IGeoPoint) {
        // Adiciona um novo pino ao mapa
        val pin = OverlayItem("Novo Pino", "Pino criado pelo utilizador", geoPoint)
        overlayItems.add(pin)

        savePinToFirestore(pin) // Salvar o pino no Firestore
        refreshPins() // Atualizar os pinos no mapa
    }

    private fun savePinToFirestore(pin: OverlayItem) {
        mapViewModel.savePinToFirestore(pin.point.latitude, pin.point.longitude)
        Toast.makeText(requireContext(), "Pino salvo em ${pin.point}", Toast.LENGTH_SHORT).show()
    }

    private fun loadPinsFromFirestore() {
        mapViewModel.loadPins { pins ->
            overlayItems.clear()
            overlayItems.addAll(pins)
            refreshPins()
        }
    }

    private fun refreshPins() {
        val overlay = ItemizedOverlayWithFocus(
            requireContext(),
            overlayItems,
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    item?.let {
                        val lat = it.point.latitude
                        val lon = it.point.longitude
                        Log.d("MapFragment", "Pin clicado: ($lat, $lon)")
                        mapViewModel.fetchLatestSensorData(lat, lon)
                    } ?: run {
                        Toast.makeText(requireContext(), "Erro: item nulo", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean = false
            }
        )
        overlay.setFocusItemsOnTap(true)
        mapView?.overlays?.add(overlay)
        mapView?.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDetach() // Libera os recursos do mapa
        mapView = null
    }
}
