package com.example.quanlyhocvien.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlyhocvien.R;
import com.example.quanlyhocvien.object.Log;

import java.util.ArrayList;
import java.util.List;

public class LoginHistoryRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Log> logs;
    public LoginHistoryRecyclerViewAdapter(List<Log> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView view = (CardView)LayoutInflater.from(parent.getContext()).inflate(R.layout.list_history, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder)holder;
        myViewHolder.cardView.setTag(position);
        Log log = logs.get(position);
        ((TextView)myViewHolder.cardView.findViewById(R.id.tvTime)).setText(log.realTime());
        ((TextView)myViewHolder.cardView.findViewById(R.id.tvDevice)).setText(log.getDevice());
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public void setLogs(ArrayList<Log> logs) {
        this.logs.clear();
        this.logs.addAll(logs);
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView)itemView;
        }
    }
}
