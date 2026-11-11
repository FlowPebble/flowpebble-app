package com.flowpebble.microguides.ui.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flowpebble.microguides.databinding.ItemStepCheckboxBinding

class StepChecklistAdapter(
    private val onStepToggled: (Int) -> Unit
) : RecyclerView.Adapter<StepChecklistAdapter.StepViewHolder>() {

    private var steps: List<String> = emptyList()
    private var checkedMask: Int = 0

    fun submitSteps(newSteps: List<String>, newMask: Int) {
        steps = newSteps
        checkedMask = newMask
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemStepCheckboxBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val isChecked = (checkedMask and (1 shl position)) != 0
        holder.bind(position, steps[position], isChecked)
    }

    override fun getItemCount(): Int = steps.size

    inner class StepViewHolder(
        private val binding: ItemStepCheckboxBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(index: Int, instruction: String, isChecked: Boolean) {
            binding.tvStepNumber.text = (index + 1).toString()
            binding.tvStepInstruction.text = instruction
            binding.tvStepInstruction.paintFlags = if (isChecked) {
                binding.tvStepInstruction.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvStepInstruction.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            binding.cbStepDone.setOnCheckedChangeListener(null)
            binding.cbStepDone.isChecked = isChecked
            binding.cbStepDone.setOnCheckedChangeListener { _, _ ->
                onStepToggled(index)
            }
            binding.cardStepRoot.setOnClickListener {
                onStepToggled(index)
            }
        }
    }
}
