package com.example.trackeths.Globals;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackeths.R;

public class CategHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView category;
    CategoryClickListener categoryClickListener;

    public CategHolder(@NonNull View itemView) {
        super(itemView);

        this.category = itemView.findViewById(R.id.rowCategory);

        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        this.categoryClickListener.onCategoryClickListener(v, getLayoutPosition());
    }

    public void setCategoryClickListener(CategoryClickListener ic){
        this.categoryClickListener = ic;
    }
}
