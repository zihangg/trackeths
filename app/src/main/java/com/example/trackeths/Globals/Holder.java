package com.example.trackeths.Globals;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackeths.R;

public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView mDescription, mAmount;
    TransactionClickListener transactionClickListener;

    public Holder(@NonNull View itemView) {
        super(itemView);

        this.mAmount = itemView.findViewById(R.id.rowAmount);
        this.mDescription = itemView.findViewById(R.id.rowDescription);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        this.transactionClickListener.onTransactionClickListener(v, getLayoutPosition());
    }

    public void setTransactionClickListener(TransactionClickListener ic){
        this.transactionClickListener = ic;
    }
}
