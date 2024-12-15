package pt.ua.androidproj

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class HelloWorldActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private var mapFragment: MapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_world)

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

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View {
            val layout = inflater.inflate(R.layout.fragment_map, container, false)

            // Configura o User-Agent do OSMDroid
            Configuration.getInstance().userAgentValue = "pt.ua.androidproj"

            // Encontra o container do mapa
            mapContainer = layout.findViewById(R.id.mapContainer)

            // Adiciona o MapView ao container
            setupMap()

            // Configura o botão de Refresh
            val refreshButton: FloatingActionButton = layout.findViewById(R.id.refreshButton)
            refreshButton.setOnClickListener {
                refreshMap()
            }

            return layout
        }

        fun setupMap() {
            // Remove o mapa antigo
            mapView?.onDetach()
            mapContainer.removeAllViews()

            // Adiciona um novo MapView
            mapView = MapView(requireContext()).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.apply {
                    setZoom(15.0)
                    setCenter(GeoPoint(40.6405, -8.6538))
                }
            }
            mapContainer.addView(mapView)
        }

        fun refreshMap() {
            setupMap() // Recarrega o mapa
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

            val profileText: TextView = layout.findViewById(R.id.profileText)
            profileText.text = "Bem-vindo ao Perfil!"

            return layout
        }
    }
}
