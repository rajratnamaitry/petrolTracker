package com.example.petroltracker;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

class PetrolAdapter extends RecyclerView.Adapter<PetrolAdapter.MyViewHolder> {
    public final List<Petrol> data;
    private Context context;

    public PetrolAdapter(List<Petrol> petrolDataList) {
        this.data = petrolDataList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTotalKm,txtMeter, txtLiter, txtAmount,txtPoint,txtDate,txtRate,txtRateDiff,txtAvg,txtDays;
        ImageButton btn;

        public MyViewHolder(View view) {
            super(view);
            context = view.getContext();
            txtMeter  = view.findViewById(R.id.txtMeter);
            txtLiter    = view.findViewById(R.id.txtLiter);
            txtAmount   = view.findViewById(R.id.txtAmount);
            txtDate     = view.findViewById(R.id.txtDate);
            txtPoint  = view.findViewById(R.id.txtPoint);

            txtTotalKm  = view.findViewById(R.id.txtTotalKm);
            txtRate    = view.findViewById(R.id.txtRate);
            txtRateDiff   = view.findViewById(R.id.txtRateDiff);
            txtAvg     = view.findViewById(R.id.txtAvg);
            txtDays     = view.findViewById(R.id.txtDays);

            btn = view.findViewById(R.id.btnEdit);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)  {
        final Petrol rowData = this.data.get(position);
        holder.txtMeter.setText(rowData.getMeter());
        holder.txtLiter.setText(rowData.getLiter());
        holder.txtAmount.setText(rowData.getAmount());
        holder.txtDate.setText(rowData.getDate());

        holder.txtPoint.setText(rowData.getPetrolPoint());
        holder.txtTotalKm.setText(rowData.getTotalKm());
        holder.txtRate.setText(rowData.getRate());
        holder.txtRateDiff.setText(rowData.getPreRate());
        holder.txtAvg.setText(rowData.getAvg());
        holder.txtDays.setText(rowData.getDays());

        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final Intent intent;
            intent = new Intent(context, addDataActivity.class);
            intent.putExtra("getMeter",rowData.getMeter());
            intent.putExtra("getLiter",rowData.getLiter());
            intent.putExtra("getDate",rowData.getDate());
            intent.putExtra("getAmount",rowData.getAmount());
            intent.putExtra("getId",rowData.getId());
            context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }
}
