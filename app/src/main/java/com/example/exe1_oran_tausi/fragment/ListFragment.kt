package com.example.exe1_oran_tausi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exe1_oran_tausi.callback.ScoreClickedCallBack
import com.example.exe1_oran_tausi.data.ScoreEntry
import com.example.exe1_oran_tausi.databinding.ActivityListFragmentBinding
import com.example.exe1_oran_tausi.databinding.ItemScoreBinding
import java.util.Locale

class ListFragment : Fragment() {

    private var _binding: ActivityListFragmentBinding? = null
    private val binding get() = _binding!!

    private var listener: ScoreClickedCallBack? = null

    private val adapter = ScoresAdapter { lat, lon ->
        listener?.onScoreClicked(lat, lon)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityListFragmentBinding.inflate(inflater, container, false)

        binding.recyclerScores.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerScores.adapter = adapter

        return binding.root
    }

    fun setListener(cb: ScoreClickedCallBack) {
        listener = cb
    }

    fun submitList(list: List<ScoreEntry>) {
        adapter.setItems(list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //////////////////////////////////////////// Adapter///////////////////////////////

    private class ScoresAdapter(
        private val onClick: (Double, Double) -> Unit
    ) : RecyclerView.Adapter<ScoresAdapter.VH>() {

        private val items = mutableListOf<ScoreEntry>()

        fun setItems(newItems: List<ScoreEntry>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        class VH(val b: ItemScoreBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]

            holder.b.txtRank.text = "${position + 1}."
            holder.b.txtName.text = item.playerName
            holder.b.txtCoins.text = "${item.coins} coins"
            holder.b.txtKm.text = String.format(Locale.US, "%.1f km", item.km)

            holder.itemView.setOnClickListener {
                onClick(item.lat, item.lon)
            }
        }


    }
}
