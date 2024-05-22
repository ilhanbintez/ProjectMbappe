package com.ilhanbintez.projectmbappe

    import android.content.Context
    import android.content.Intent
    import android.os.Bundle
    import android.view.Menu
    import android.view.MenuItem
    import androidx.appcompat.app.AppCompatActivity
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.ilhanbintez.projectmbappe.databinding.ActivityMainBinding

    class MainActivity : AppCompatActivity() {

        private lateinit var binding: ActivityMainBinding
        private lateinit var playerList: ArrayList<Player>
        private lateinit var playerAdapter: PlayerAdapter

        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            val view = binding.root
            setContentView(view)

            setSupportActionBar(binding.toolbar)

            playerList = ArrayList()
            playerAdapter = PlayerAdapter(playerList)
            binding.recyclerview.layoutManager = LinearLayoutManager(this)
            binding.recyclerview.adapter = playerAdapter

            try {
                val database = this.openOrCreateDatabase("Players", Context.MODE_PRIVATE, null)
                val cursor = database.rawQuery("SELECT * FROM players", null)
                val nameIx = cursor.getColumnIndex("name")
                val idIx = cursor.getColumnIndex("id")

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIx)
                    val id = cursor.getInt(idIx)
                    val player = Player(name, id)
                    playerList.add(player)
                }

                playerAdapter.notifyDataSetChanged()

                cursor.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            println("ok")
            menuInflater.inflate(R.menu.player_menu, menu)
            return super.onCreateOptionsMenu(menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if (item.itemId == R.id.add_player_report) {
                println("ok")
                val intent = Intent(this, PlayerDetails::class.java)
                intent.putExtra("info", "new")
                startActivity(intent)
            }
            return super.onOptionsItemSelected(item)
        }
    }
