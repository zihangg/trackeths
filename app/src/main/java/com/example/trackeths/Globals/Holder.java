package com.example.trackeths.Globals;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackeths.R;

public class Holder extends RecyclerView.ViewHolder {

    public TextView mDescription, mAmount;

    public Holder(@NonNull View itemView) {
        super(itemView);

        this.mAmount = itemView.findViewById(R.id.rowAmount);
        this.mDescription = itemView.findViewById(R.id.rowDescription);
    }
}
