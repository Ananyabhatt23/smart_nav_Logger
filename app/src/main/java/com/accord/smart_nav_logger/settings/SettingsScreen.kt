package com.accord.smart_nav_logger.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.accord.smart_nav_logger.R

class SettingsScreen : AppCompatActivity() {

    private lateinit var path1: Spinner
    private lateinit var path2: Spinner
    private lateinit var baudrate1: Spinner
    private lateinit var baudrate2: Spinner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_screen)

        path1 = findViewById(R.id.path1)
        path2 = findViewById(R.id.path2)
        baudrate1 = findViewById(R.id.baudrate1)
        baudrate2 = findViewById(R.id.baudrate2)


        val option_path1= arrayOf("Item 1", "Item 2", "Item 3")
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, option_path1)
        path1.adapter = adapter1

        val option_path2= arrayOf("Item 1", "Item 2", "Item 3")
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, option_path2)
        path2.adapter = adapter2

        val option_baudrate1= arrayOf("Item 1", "Item 2", "Item 3")
        val adapter3 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, option_baudrate1)
        baudrate1.adapter = adapter3

        val option_baudrate2= arrayOf("Item 1", "Item 2", "Item 3")
        val adapter4 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, option_baudrate2)
        baudrate2.adapter = adapter4

    }
}