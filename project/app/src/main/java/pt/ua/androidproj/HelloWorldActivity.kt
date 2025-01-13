package pt.ua.androidproj

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.get
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HelloWorldActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private var mapFragment: MapFragment? = null
    private var homeFragment: HomeFragment? = null

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
                    0 -> {
                        homeFragment = HomeFragment()
                        homeFragment!!
                    }
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

        // Listener para abas
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> homeFragment?.refreshContent()
                    1 -> mapFragment?.refreshMap()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    homeFragment?.refreshContent()
                }
            }
        })
    }

    // Fragmento Home
    class HomeFragment : Fragment() {

        private lateinit var adapter: ArrayAdapter<String>
        private val pinList = mutableListOf<String>()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View? {
            val layout = inflater.inflate(R.layout.fragment_home, container, false)

            val listView: ListView = layout.findViewById(R.id.listView)
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, pinList)
            listView.adapter = adapter

            // Load pins initially
            refreshContent()

            // Handle pin click to fetch and display sensor data
            listView.setOnItemClickListener { _, _, position, _ ->
                val pinName = pinList[position]
                fetchLatestReadingForPin(pinName)
            }

            // Configure logout button
            val logoutButton: Button = layout.findViewById(R.id.logoutButton)
            logoutButton.setOnClickListener {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

            return layout
        }

        fun refreshContent() {
            val db = FirebaseFirestore.getInstance()
            val pins = db.collection("pins")

            pins.get().addOnSuccessListener { documents ->
                pinList.clear()
                for (document in documents) {
                    val pinName = document.getString("pinName") ?: "Unnamed Pin"
                    pinList.add(pinName)
                }
                adapter.notifyDataSetChanged()
            }
        }

        private fun fetchLatestReadingForPin(pinName: String) {
            val db = FirebaseFirestore.getInstance()
            db.collection("pins")
                .whereEqualTo("pinName", pinName)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val document = result.documents[0]
                        val sensors = document["sensors"] as? List<Map<String, Any>> ?: emptyList()

                        val sensorInfoList = sensors.map { sensor ->
                            val description = sensor["description"] as? String ?: "No Description"
                            val values = sensor["value"] as? List<Double> ?: emptyList()
                            val unit = sensor["unit"] as? String ?: "Unknown Unit"

                            val lastReading = if (values.isNotEmpty()) {
                                "${values.last()} $unit"
                            } else {
                                "No Reading Available"
                            }

                            "Description: $description\nLast Reading: $lastReading"
                        }

                        // Show sensor information in a dialog
                        showSensorDetailsDialog(pinName, sensorInfoList)
                    } else {
                        println("No sensors found for pin: $pinName")
                    }
                }
                .addOnFailureListener { e ->
                    println("Error fetching data: ${e.message}")
                }
        }

        private fun showSensorDetailsDialog(pinName: String, sensorInfoList: List<String>) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Sensors for $pinName")
                .setItems(sensorInfoList.toTypedArray(), null)
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            dialog.show()
        }
    }

    // Fragmento Perfil
    // Fragmento Perfil
    class ProfileFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View? {
            val layout = inflater.inflate(R.layout.fragment_profile, container, false)

            val uid = activity?.intent?.getStringExtra("uid")
            val profileImage: ImageView = layout.findViewById(R.id.profileImage)
            val profileText: TextView = layout.findViewById(R.id.profileText)
            val dobText: TextView = layout.findViewById(R.id.dob_text)
            val colorText: TextView = layout.findViewById(R.id.color_text)

            val user = FirebaseAuth.getInstance().currentUser
            val email = user?.email
            val username = email?.split("@")?.get(0)

            // Set default profile picture
            profileImage.setImageResource(R.drawable.profile)

            // Fetch color and DOB from Firestore
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("user_info").document(uid!!)

            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val dobTimestamp = document.getTimestamp("dob")
                    val dob = dobTimestamp?.toDate()?.toString()?.substring(0, 10) ?: "Unknown DOB"
                    val color = document.getString("color") ?: "Unknown Color"

                    dobText.text = "Date of Birth: $dob"
                    colorText.text = "Favorite Color: $color"
                }
            }.addOnFailureListener { exception ->
                println("Error fetching user data: $exception")
            }

            profileText.text = "Welcome, $username!"

            return layout
        }
    }

}
