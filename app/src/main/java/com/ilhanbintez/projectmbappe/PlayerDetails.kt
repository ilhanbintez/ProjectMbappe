package com.ilhanbintez.projectmbappe

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ilhanbintez.projectmbappe.databinding.ActivityPlayerDetailsBinding
import java.io.ByteArrayOutputStream

class PlayerDetails : AppCompatActivity() {

    private var selectedBitmap: Bitmap? = null
    private lateinit var binding: ActivityPlayerDetailsBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Players", Context.MODE_PRIVATE,null)

        registerLauncher()

        val info = intent.getStringExtra("info")


        if (info == "new") {
            binding.editTextName.setText("")
            binding.editTextAge.setText("")
            binding.editTextPosition.setText("")
            binding.editTextTeam.setText("")
            binding.editTextStrengths.setText("")
            binding.editTextWeaknesses.setText("")
            binding.editTextReport.setText("")
            binding.saveButton.visibility = View.VISIBLE

            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.avatar)
            binding.imageView.setImageBitmap(selectedImageBackground)
            setFieldsEditable(true)
        } else {
            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 1)
            // Load data from database and set to views
            val cursor = database.rawQuery("SELECT * FROM players WHERE id = ?", arrayOf(selectedId.toString()))

            val nameIx = cursor.getColumnIndex("name")
            val ageIx = cursor.getColumnIndex("age")
            val positionIx = cursor.getColumnIndex("position")
            val teamIx = cursor.getColumnIndex("team")
            val strengthsIx = cursor.getColumnIndex("strengths")
            val weaknessesIx = cursor.getColumnIndex("weaknesses")
            val reportIx = cursor.getColumnIndex("report")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()) {
                binding.editTextName.setText(cursor.getString(nameIx))
                binding.editTextAge.setText(cursor.getString(ageIx))
                binding.editTextPosition.setText(cursor.getString(positionIx))
                binding.editTextTeam.setText(cursor.getString(teamIx))
                binding.editTextStrengths.setText(cursor.getString(strengthsIx))
                binding.editTextWeaknesses.setText(cursor.getString(weaknessesIx))
                binding.editTextReport.setText(cursor.getString(reportIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }

            cursor.close()
            setFieldsEditable(false)
        }



    }

    fun saveButtonClicked(view: View) {
        val name = binding.editTextName.text.toString()
        val age = binding.editTextAge.text.toString()
        val position = binding.editTextPosition.text.toString()
        val team = binding.editTextTeam.text.toString()
        val strengths = binding.editTextStrengths.text.toString()
        val weaknesses = binding.editTextWeaknesses.text.toString()
        val report = binding.editTextReport.text.toString()

        if (selectedBitmap != null) {
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {

                database.execSQL("CREATE TABLE IF NOT EXISTS players (id INTEGER PRIMARY KEY, name VARCHAR, age VARCHAR, position VARCHAR, team VARCHAR, strengths VARCHAR, weaknesses VARCHAR, report VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO players (name, age, position, team, strengths, weaknesses, report, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, name)
                statement.bindString(2, age)
                statement.bindString(3, position)
                statement.bindString(4, team)
                statement.bindString(5, strengths)
                statement.bindString(6, weaknesses)
                statement.bindString(7, report)
                statement.bindBlob(8, byteArray)
                statement.execute()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    fun makeSmallerBitmap(image: Bitmap, maximumSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio: Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            width = maximumSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maximumSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun selectImage(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(this@PlayerDetails.contentResolver, imageData!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(this@PlayerDetails.contentResolver, imageData)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(RequestPermission()) { result ->
            if (result) {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                Toast.makeText(this@PlayerDetails, "Permission needed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setFieldsEditable(editable: Boolean) {
        binding.editTextName.isEnabled = editable
        binding.editTextAge.isEnabled = editable
        binding.editTextPosition.isEnabled = editable
        binding.editTextTeam.isEnabled = editable
        binding.editTextStrengths.isEnabled = editable
        binding.editTextWeaknesses.isEnabled = editable
        binding.editTextReport.isEnabled = editable
        binding.imageView.isClickable = editable
    }
}
