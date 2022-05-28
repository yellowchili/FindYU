package com.example.navermapex_2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TourAdapater extends RecyclerView.Adapter<ViewHolder> {
    private static Context context;
    private LayoutInflater mInflate;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflate.from(parent.getContext()).inflate(R.layout.recycler_tour_items, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        this.context = parent.getContext();

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

class ViewHolder extends RecyclerView.ViewHolder {

    private TextView tourName, tourDate;
    private ImageView tourImg;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        tourName = itemView.findViewById(R.id.tour_name);
        tourDate = itemView.findViewById(R.id.tour_date);
        tourImg = itemView.findViewById(R.id.tour_img);

    }
}