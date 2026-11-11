package com.flowpebble.microguides.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flowpebble.microguides.R
import com.flowpebble.microguides.data.local.MicroGuideEntity
import com.flowpebble.microguides.databinding.ItemGuideCardBinding

class GuideAdapter(
    private val onGuideClicked: (MicroGuideEntity) -> Unit,
    private val onBookmarkClicked: (MicroGuideEntity) -> Unit
) : ListAdapter<MicroGuideEntity, GuideAdapter.GuideViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val binding = ItemGuideCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GuideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getGuideAt(position: Int): MicroGuideEntity? {
        return if (position in 0 until itemCount) getItem(position) else null
    }

    inner class GuideViewHolder(
        private val binding: ItemGuideCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(guide: MicroGuideEntity) {
            val context = binding.root.context
            val stepCount = guide.getStepsList().size
            binding.tvEmojiBadge.text = guide.emoji
            binding.tvGuideTitle.text = guide.title
            binding.tvGuideMeta.text = context.getString(
                R.string.guide_meta_format,
                guide.category,
                stepCount
            )
            binding.tvGuideSummary.text = guide.summary
            binding.btnBookmark.setImageResource(
                if (guide.isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
            )

            binding.cardGuideRoot.setOnClickListener {
                onGuideClicked(guide)
            }
            binding.btnBookmark.setOnClickListener {
                onBookmarkClicked(guide)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<MicroGuideEntity>() {
        override fun areItemsTheSame(oldItem: MicroGuideEntity, newItem: MicroGuideEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MicroGuideEntity, newItem: MicroGuideEntity): Boolean {
            return oldItem == newItem
        }
    }
}
