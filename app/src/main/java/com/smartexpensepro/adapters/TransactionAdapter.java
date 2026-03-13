package com.smartexpensepro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.smartexpensepro.R;
import com.smartexpensepro.models.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final Context context;
    private List<Transaction> transactions;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Transaction transaction, int position);
    }

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void updateData(List<Transaction> newData) {
        this.transactions = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);

        holder.tvAmount.setText(String.format("₹ %.2f", t.getAmount()));
        holder.tvCategory.setText(t.getCategory());
        holder.tvDescription.setText(t.getDescription() != null && !t.getDescription().isEmpty()
                ? t.getDescription() : "No description");
        holder.tvDate.setText(t.getDate().length() > 10 ? t.getDate().substring(0, 10) : t.getDate());

        setCategoryStyle(holder, t.getCategory());

        if (t.isAutoDetected()) {
            holder.tvAutoTag.setVisibility(View.VISIBLE);
        } else {
            holder.tvAutoTag.setVisibility(View.GONE);
        }

        holder.cardView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(t, holder.getAdapterPosition());
            }
            return true;
        });
    }

    private void setCategoryStyle(ViewHolder holder, String category) {
        int iconRes;
        int colorRes;

        switch (category) {
            case "Food":
                iconRes = R.drawable.ic_food;
                colorRes = R.color.category_food;
                break;
            case "Travel":
                iconRes = R.drawable.ic_travel;
                colorRes = R.color.category_travel;
                break;
            case "Bills":
                iconRes = R.drawable.ic_bills;
                colorRes = R.color.category_bills;
                break;
            case "Shopping":
                iconRes = R.drawable.ic_shopping;
                colorRes = R.color.category_shopping;
                break;
            case "Health":
                iconRes = R.drawable.ic_health;
                colorRes = R.color.category_health;
                break;
            case "Entertainment":
                iconRes = R.drawable.ic_entertainment;
                colorRes = R.color.category_entertainment;
                break;
            case "Education":
                iconRes = R.drawable.ic_education;
                colorRes = R.color.category_education;
                break;
            default:
                iconRes = R.drawable.ic_others;
                colorRes = R.color.category_others;
                break;
        }

        holder.ivCategoryIcon.setImageResource(iconRes);
        holder.ivCategoryIcon.setColorFilter(context.getColor(colorRes));
        holder.tvCategory.setTextColor(context.getColor(colorRes));
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView ivCategoryIcon;
        TextView tvAmount, tvCategory, tvDescription, tvDate, tvAutoTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTransaction);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAutoTag = itemView.findViewById(R.id.tvAutoTag);
        }
    }
}
