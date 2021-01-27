package com.darkminstrel.expandablecontainer

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.darkminstrel.expandable.ExpandableContainer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val container = findViewById<ExpandableContainer>(R.id.container)
        val btnToggle = findViewById<Button>(R.id.btn_toggle)

        btnToggle.setText(if(container.isExpanded()) R.string.collapse else R.string.expand)
        btnToggle.setOnClickListener {
            container.toggle()
            btnToggle.setText(if(container.isExpanded()) R.string.collapse else R.string.expand)
        }
    }


}