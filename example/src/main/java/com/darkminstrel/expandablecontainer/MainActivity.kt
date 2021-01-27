package com.darkminstrel.expandablecontainer

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.darkminstrel.expandable.ExpandableContainer
import com.darkminstrel.expandablecontainer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply{
            btnToggle.apply {
                setText(if(containerWithImage.isExpanded()) R.string.collapse else R.string.expand)
                setOnClickListener {
                    containerWithImage.toggle()
                    setText(if(containerWithImage.isExpanded()) R.string.collapse else R.string.expand)
                }
            }

            val lorem = getString(R.string.test_string)
            var len = 100
            val step = 50
            textDynamic.text = lorem.substring(0, len)
            btnMoreText.setOnClickListener {
                len = (len+step).coerceIn(step, lorem.length)
                textDynamic.text = lorem.substring(0, len)
            }
            btnLessText.setOnClickListener {
                len = (len-step).coerceIn(step, lorem.length)
                textDynamic.text = lorem.substring(0, len)
            }
            btnToggleText.apply {
                setText(if(containerWithText.isExpanded()) R.string.collapse else R.string.expand)
                setOnClickListener {
                    containerWithText.toggle()
                    setText(if(containerWithText.isExpanded()) R.string.collapse else R.string.expand)
                }
            }
            containerWithText.setOnContentFitsChangeListener { contentFits ->
                btnToggleText.visibility = if(contentFits) View.GONE else View.VISIBLE
            }
        }
    }


}