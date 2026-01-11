package com.gym.fitconnectpro.fragments.trainer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gym.fitconnectpro.R;
import com.gym.fitconnectpro.database.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class MyClientsAdapter extends RecyclerView.Adapter<MyClientsAdapter.ViewHolder> implements Filterable {

    private List<Member> clientList;
    private List<Member> clientListFull;
    private Context context;
    private ClientActionListener listener;

    public interface ClientActionListener {
        void onViewProfile(Member client);
        void onCreatePlan(Member client);
        void onMessage(Member client);
    }

    public MyClientsAdapter(Context context, List<Member> clientList, ClientActionListener listener) {
        this.context = context;
        this.clientList = clientList;
        this.clientListFull = new ArrayList<>(clientList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_client, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member client = clientList.get(position);
        holder.tvClientName.setText(client.getFullName());
        holder.tvJoinDate.setText("Joined: " + (client.getCreatedAt() != null ? client.getCreatedAt() : "N/A"));
        
        // Status color logic optional, keeping simple for now
        holder.tvClientStatus.setText(client.getStatus());
        
        holder.btnViewProfile.setOnClickListener(v -> listener.onViewProfile(client));
        holder.btnCreatePlan.setOnClickListener(v -> listener.onCreatePlan(client));
        holder.btnMessage.setOnClickListener(v -> listener.onMessage(client));
    }

    @Override
    public int getItemCount() {
        return clientList.size();
    }
    
    public void updateList(List<Member> list) {
        this.clientList = list;
        this.clientListFull = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return clientFilter;
    }

    private Filter clientFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Member> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(clientListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Member item : clientListFull) {
                    if (item.getFullName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clientList.clear();
            clientList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvJoinDate, tvClientStatus;
        Button btnViewProfile, btnCreatePlan, btnMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvJoinDate = itemView.findViewById(R.id.tvJoinDate);
            tvClientStatus = itemView.findViewById(R.id.tvClientStatus);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
            btnCreatePlan = itemView.findViewById(R.id.btnCreatePlan);
            btnMessage = itemView.findViewById(R.id.btnMessage);
        }
    }
}
