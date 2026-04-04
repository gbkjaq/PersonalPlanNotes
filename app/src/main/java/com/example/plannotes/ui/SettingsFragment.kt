package com.example.plannotes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.plannotes.MainActivity
import com.example.plannotes.R
import com.example.plannotes.data.Config

class SettingsFragment : Fragment() {
    
    private lateinit var etQuantity: EditText
    private lateinit var etCoefficient: EditText
    private lateinit var tvCurrentConfig: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        etQuantity = view.findViewById(R.id.et_quantity)
        etCoefficient = view.findViewById(R.id.et_coefficient)
        tvCurrentConfig = view.findViewById(R.id.tv_current_config)
        val btnSave: Button = view.findViewById(R.id.btn_save)
        
        loadConfig()
        
        btnSave.setOnClickListener {
            saveConfig()
        }
    }
    
    private fun loadConfig() {
        val config = (activity as MainActivity).dataManager.getConfig()
        etQuantity.setText(config.quantity.toString())
        etCoefficient.setText(config.coefficient.toString())
        updateCurrentConfigText(config)
    }
    
    private fun updateCurrentConfigText(config: Config) {
        tvCurrentConfig.text = getString(
            R.string.current_config,
            config.quantity,
            config.coefficient
        )
    }
    
    private fun saveConfig() {
        val quantityStr = etQuantity.text.toString()
        val coefficientStr = etCoefficient.text.toString()
        
        if (quantityStr.isEmpty() || coefficientStr.isEmpty()) {
            Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }
        
        val quantity = quantityStr.toIntOrNull()
        val coefficient = coefficientStr.toIntOrNull()
        
        if (quantity == null || coefficient == null || quantity <= 0 || coefficient <= 0) {
            Toast.makeText(requireContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show()
            return
        }
        
        val config = Config(quantity = quantity, coefficient = coefficient)
        (activity as MainActivity).dataManager.saveConfig(config)
        updateCurrentConfigText(config)
        
        Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show()
    }
}
