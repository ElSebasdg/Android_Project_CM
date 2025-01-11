package pt.ua.androidproj

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.get
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

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

            data class Sensor(val description: String = "") {
                override fun toString() = description
            }

            val db = FirebaseFirestore.getInstance()
            val pins = db.collection("pins")

            val sensorList = mutableListOf<Sensor>()
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sensorList)
            val listView: android.widget.ListView = layout.findViewById(R.id.listView)
            listView.adapter = adapter

            pins.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val sensors = document.data["sensors"] as? List<Map<String, Any>> ?: emptyList()
                    for (sensorMap in sensors) {
                        val desc = sensorMap["description"] as? String ?: "Unknown"
                        sensorList.add(Sensor(desc))
                    }
                }
                adapter.notifyDataSetChanged()
            }

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

    // Fragmento Perfil
    class ProfileFragment : Fragment() {
        override fun onCreateView(
            inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?
        ): android.view.View? {
            val layout = inflater.inflate(R.layout.fragment_profile, container, false)
            val uid = activity?.intent?.getStringExtra("uid")
            val profileText: android.widget.TextView = layout.findViewById(R.id.profileText)
            val dob_text: android.widget.TextView = layout.findViewById(R.id.dob_text)
            val color_text: android.widget.TextView = layout.findViewById(R.id.color_text)

            val user = FirebaseAuth.getInstance().currentUser
            val email = user?.email
            val username = email?.split("@")?.get(0)

            // fetch color and dob from firestore collection "user_info" with document "uid" and fields "dob" and "color"
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("user_info").document(uid!!)

            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val dob_timestamp = document.getTimestamp("dob")
                    val dob_dirty = dob_timestamp?.toDate().toString()
                    val dob = dob_dirty.substring(0, 10)
                    val color = document.getString("color")

                    dob_text.text = "Date of Birth: $dob"
                    color_text.text = "Favorite Color: $color"
                }
            }.addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }

            profileText.text = "Welcome user $username"

            return layout
        }
    }
}
