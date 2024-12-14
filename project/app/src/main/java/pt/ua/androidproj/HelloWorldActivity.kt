package pt.ua.androidproj

import android.os.Bundle
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
import pt.ua.androidproj.databinding.ActivityHelloWorldBinding

class HelloWorldActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelloWorldBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Conectar o layout
        binding = ActivityHelloWorldBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar o ViewPager e TabLayout
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2 // Número de abas (Home e Map)
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> HomeFragment()
                    1 -> MapFragment()
                    else -> throw IllegalArgumentException("Invalid position")
                }
            }
        }
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Home" else "Map"
        }.attach()
    }

    // Fragmento para a aba "Home"
    class HomeFragment : Fragment() {
        override fun onCreateView(
            inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?
        ): android.view.View? {
            val layout = inflater.inflate(R.layout.fragment_home, container, false)
            val sensorData = listOf(
                "Temperature: 0°C",
                "Humidity: 0%",
                "Pressure: 0 hPa",
                "Lux: 0 lx"
            )
            val listView: android.widget.ListView = layout.findViewById(R.id.listView)
            listView.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sensorData)
            return layout
        }
    }

    // Fragmento para a aba "Map"
    class MapFragment : Fragment() {
        private var mapView: MapView? = null
        override fun onCreateView(
            inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?
        ): android.view.View? {
            val layout = inflater.inflate(R.layout.fragment_map, container, false)
            mapView = layout.findViewById(R.id.mapView)
            Configuration.getInstance().load(context, context?.getSharedPreferences("osm_prefs", 0))
            mapView?.setTileSource(TileSourceFactory.MAPNIK)
            mapView?.controller?.setZoom(17.0)
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
