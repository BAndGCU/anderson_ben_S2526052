package com.example.anderson_ben_s2526052;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SituationAdapter extends RecyclerView.Adapter<SituationAdapter.VH> {
    public interface OnClick { void on(Situation s); }

    private List<Situation> items = new ArrayList<>();
    private final OnClick listener;

    public SituationAdapter(OnClick l) { listener = l; }

    public void setItems(List<Situation> list) {
        items = list;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_situation, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Situation s = items.get(pos);
        h.type.setText(UiHelper.typeLabel(s));
        h.comment.setText(s.comment);
        h.icon.setImageResource(UiHelper.iconFor(s));
        h.itemView.setBackgroundColor(UiHelper.delayColor(s));
        h.itemView.setOnClickListener(v -> listener.on(s));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon; TextView type, comment;
        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            type = v.findViewById(R.id.type);
            comment = v.findViewById(R.id.comment);
        }
    }
}