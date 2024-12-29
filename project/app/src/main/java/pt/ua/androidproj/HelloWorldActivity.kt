package pt.ua.androidproj

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayItem


class HelloWorldActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private var mapFragment: MapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_world)

        // User from intent
        val uid = intent.getStringExtra("uid")

        // Vincular TabLayout e ViewPager2
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        // Configurar ViewPager2 com os fragmentos
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3 // Número de abas
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> HomeFragment()
                    1 -> {
                        mapFragment = MapFragment()
                        mapFragment!!
                    }
                    2 -> ProfileFragment()
                    else -> throw IllegalArgumentException("Invalid position")
                }
            }
        }
        viewPager.adapter = adapter

        // Configurar TabLayout com ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Home"
                1 -> "Map"
                2 -> "Perfil"
                else -> null
            }
        }.attach()

        // Listener para a aba "Map"
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 1) {
                    mapFragment?.refreshMap()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                if (tab.position == 1) {
                    mapFragment?.refreshMap()
                }
            }
        })
    }

    // Fragmento Home
    class HomeFragment : Fragment() {
        override fun onCreateView(
            inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?
        ): android.view.View? {
            val layout = inflater.inflate(R.layout.fragment_home, container, false)

            // Dados simulados
            val sensorData = listOf(
                "Temperature: 20°C",
                "Humidity: 65%",
                "Pressure: 1013 hPa",
                "Lux: 150 lx"
            )

            // Configurar ListView
            val listView: android.widget.ListView = layout.findViewById(R.id.listView)
            listView.adapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                sensorData
            )

            // Configurar botão de logout
            val logoutButton: Button = layout.findViewById(R.id.logoutButton)
            logoutButton.setOnClickListener {
                // Redirecionar para a MainActivity (tela de login)
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                activity?.finish() // Finalizar a atividade atual
            }

            return layout
        }
    }

    // Fragmento Map
    class MapFragment : Fragment() {

        private var mapView: MapView? = null
        private lateinit var mapContainer: FrameLayout
        private val overlayItems = mutableListOf<OverlayItem>()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View {
            val layout = inflater.inflate(R.layout.fragment_map, container, false)

            // Configure the User-Agent for OSMDroid
            Configuration.getInstance().userAgentValue = "pt.ua.androidproj"

            // Find the map container
            mapContainer = layout.findViewById(R.id.mapContainer)

            // Add the MapView to the container
            setupMap()

            // Configure the refresh button
            val refreshButton: FloatingActionButton = layout.findViewById(R.id.refreshButton)
            refreshButton.setOnClickListener {
                refreshMap()
            }

            return layout
        }

        fun setupMap() {
            // Remove the old map
            mapView?.onDetach()
            mapContainer.removeAllViews()

            // Add a new MapView
            mapView = MapView(requireContext()).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.apply {
                    setZoom(15.0)
                    setCenter(GeoPoint(40.6405, -8.6538))
                }
            }

            mapContainer.addView(mapView)

            // Add a touch overlay for map taps
            val overlay = object : Overlay() {
                override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                    // Capture the tap location
                    val geoPoint = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt())
                    // Add pin at the tap location
                    addPin(geoPoint)
                    // Debugging tap event
                    Toast.makeText(requireContext(), "Pin added at: $geoPoint", Toast.LENGTH_SHORT).show()
                    return true
                }

                override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
                    // No drawing needed for this overlay
                }
            }

            // Add the overlay to the map
            mapView?.overlays?.add(overlay)

            // Load Pins
            loadPinsFromFirestore()

            mapView?.invalidate() // Redraw the map
        }

        fun refreshMap() {
            setupMap() // Reloads the map
        }

        private fun addPin(geoPoint: IGeoPoint) {
            // Create a new OverlayItem for the pin
            val pin = OverlayItem("New Pin", "User-created pin", geoPoint)
            overlayItems.add(pin)

            savePinToFirestore(pin)

            // Update the overlay on the map
            addOverlayToMap()
        }

        private fun savePinToFirestore(pin: OverlayItem) {
            val uid = activity?.intent?.getStringExtra("uid")
            val db = FirebaseFirestore.getInstance()

            val pinData = hashMapOf(
                "uid" to uid,
                "latitude" to pin.point.latitude,
                "longitude" to pin.point.longitude
            )

            db.collection("pins")
                .add(pinData)
        }

        private fun loadPinsFromFirestore() {
            val uid = activity?.intent?.getStringExtra("uid")
            val db = FirebaseFirestore.getInstance()

            db.collection("pins")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener { result ->
                    overlayItems.clear()
                    for (document in result) {
                        val latitude = document.getDouble("latitude")
                        val longitude = document.getDouble("longitude")
                        if (latitude != null && longitude != null) {
                            val pin = OverlayItem("Pin from Firestore", "Loaded from Firestore", GeoPoint(latitude, longitude))
                            overlayItems.add(pin)
                        }
                    }
                    addOverlayToMap()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Error loading pins: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        
        private fun addOverlayToMap() {
            val overlay = ItemizedOverlayWithFocus(
                requireContext(),
                overlayItems,
                object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                    override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                        // Handle single tap on overlay item
                        Toast.makeText(requireContext(), item?.title, Toast.LENGTH_SHORT).show()
                        return true
                    }
        
                    override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                        // Handle long press on overlay item
                        return false
                    }
                }
            )
            overlay.setFocusItemsOnTap(true)
            mapView?.overlays?.add(overlay)
            mapView?.invalidate() // Redraw the map
        }

        override fun onDestroyView() {
            super.onDestroyView()
            mapView?.onDetach()
            mapView = null
        }
    }

    // Fragmento Perfil
    class ProfileFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View {
            val layout = inflater.inflate(R.layout.fragment_profile, container, false)
            val uid = activity?.intent?.getStringExtra("uid")
            val profileText: TextView = layout.findViewById(R.id.profileText)
            profileText.text = "User ID: $uid"

            return layout
        }
    }
}
