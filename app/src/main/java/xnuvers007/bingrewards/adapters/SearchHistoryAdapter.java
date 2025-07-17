package xnuvers007.bingrewards.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import xnuvers007.bingrewards.R;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {

    private List<String> searchHistory;

    public SearchHistoryAdapter(List<String> searchHistory) {
        this.searchHistory = searchHistory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String searchItem = searchHistory.get(position);
        holder.tvSearchItem.setText(searchItem);

        // Set different colors for recent searches
        if (position == 0) {
            holder.tvSearchItem.setTextColor(0xFF4CAF50); // Green for latest
        } else if (position < 3) {
            holder.tvSearchItem.setTextColor(0xFF2196F3); // Blue for recent
        } else {
            holder.tvSearchItem.setTextColor(0xFF757575); // Gray for older
        }
    }

    @Override
    public int getItemCount() {
        return searchHistory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSearchItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSearchItem = itemView.findViewById(R.id.tvSearchItem);
        }
    }
}