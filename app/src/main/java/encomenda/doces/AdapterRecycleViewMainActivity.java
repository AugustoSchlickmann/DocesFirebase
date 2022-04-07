package encomenda.doces;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import encomenda.doces.modelos.Doce;
import encomenda.doces.modelos.Encomenda;

public class AdapterRecycleViewMainActivity extends RecyclerView.Adapter<AdapterRecycleViewMainActivity.ViewHolder> {

    private Context context;
    private ArrayList<Encomenda> encomendas;
    DataBase dataBase;

    public AdapterRecycleViewMainActivity(Context context, ArrayList<Encomenda> encomendas) {
        this.context = context;
        this.encomendas = encomendas;
        dataBase = new DataBase(context);

    }

    public AdapterRecycleViewMainActivity(Context context) {
        this.context = context;
    }

    public ArrayList<Encomenda> getEncomendas() {
        return encomendas;
    }

    public void setEncomendas(ArrayList<Encomenda> encomendas) {
        this.encomendas = encomendas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.uma_linha_encomenda, parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRecycleViewMainActivity.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txtNomeCliente.setText(encomendas.get(position).getCliente().getNome());
        int qtdDocesTotal=0;
        for (Doce doce : encomendas.get(position).getDoces()){
            qtdDocesTotal += doce.getQtd();
        }
        holder.txtQtdDoces.setText(String.valueOf(qtdDocesTotal)+" Doces");
        holder.txtData.setText(encomendas.get(position).getData());
        holder.txtHora.setText(encomendas.get(position).getHora());

        if(encomendas.get(position).getObs().equalsIgnoreCase("")){ holder.avisoObs.setVisibility(View.GONE); }

        holder.constraint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EncomendaActivity.class);
                intent.putExtra("encomendaIdFirebase", encomendas.get(position).getIdFirebase());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return encomendas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public class ViewHolder extends  RecyclerView.ViewHolder{

        private ConstraintLayout constraint;
        private TextView txtNomeCliente, txtQtdDoces, txtData, txtHora;
        private ImageView avisoObs;

        public  ViewHolder(View itemview){
            super(itemview);
            constraint = itemview.findViewById(R.id.constraintLayoutUmaLinhaEncomenda);
            txtNomeCliente = itemview.findViewById(R.id.textViewUmaLinhaEncomendaNomeCliente);
            txtQtdDoces = itemview.findViewById(R.id.textViewUmaLinhaEncomendaQtdDoces);
            txtData = itemview.findViewById(R.id.textViewUmaLinhaEncomendaData);
            txtHora = itemview.findViewById(R.id.textViewUmaLinhaEncomendaHora);
            avisoObs = itemview.findViewById(R.id.imageViewUmaLinhaEncomendaAvisoObs);


        }

    }
}
