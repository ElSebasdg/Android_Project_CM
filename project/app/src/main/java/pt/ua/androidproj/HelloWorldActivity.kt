package pt.ua.androidproj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class HelloWorldActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_world)

        // Vincular TabLayout e ViewPager
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        // Configurar ViewPager2 com os fragmentos
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2 // Número de abas
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> HomeFragment()
                    1 -> MapFragment()
                    else -> throw IllegalArgumentException("Invalid position")
                }
            }
        }
        viewPager.adapter = adapter

        // Conectar TabLayout com ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Home" else "Map"
        }.attach()
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

        override fun onCreateView(
            inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?
        ): android.view.View? {
            val layout = inflater.inflate(R.layout.fragment_map, container, false)

            // Configurar o MapView
            mapView = layout.findViewById(R.id.mapView)
            Configuration.getInstance().load(
                requireContext(),
                requireContext().getSharedPreferences("osm_prefs", MODE_PRIVATE)
            )
            mapView?.setTileSource(TileSourceFactory.MAPNIK)
            mapView?.setMultiTouchControls(true)
            mapView?.controller?.setZoom(15.0)
            mapView?.controller?.setCenter(GeoPoint(40.6405, -8.6538))
            return layout
        }

        override fun onDestroyView() {
            super.onDestroyView()
            mapView?.onDetach()
            mapView = null
        }
    }
}
