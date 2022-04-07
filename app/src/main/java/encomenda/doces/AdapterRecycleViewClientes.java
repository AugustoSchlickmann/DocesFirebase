package encomenda.doces;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import encomenda.doces.modelos.Cliente;

public class AdapterRecycleViewClientes extends RecyclerView.Adapter<AdapterRecycleViewClientes.ViewHolder> {

    private ArrayList<Cliente> clientes;
    private Context context;

    public AdapterRecycleViewClientes(Context context) {
        this.context = context;
    }

    public ArrayList<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(ArrayList<Cliente> clientes) {
        this.clientes = clientes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.uma_linha_cliente, parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRecycleViewClientes.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.nomeCliente.setText(clientes.get(position).getNome());
        holder.telefoneCliente.setText(clientes.get(position).getTelefone());

        holder.constraint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ClienteActivity.class);
                intent.putExtra("nome_telefone" ,clientes.get(position).getNome_telefone());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return clientes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder extends  RecyclerView.ViewHolder {

        private ConstraintLayout constraint;
        private TextView idCliente, nomeCliente, telefoneCliente;

        public ViewHolder(@NonNull View itemview) {
            super(itemview);
            constraint = itemview.findViewById(R.id.constraintLayoutUmaLinhaCliente);
            nomeCliente = itemview.findViewById(R.id.textViewUmaLinhaClienteNome);
            telefoneCliente = itemview.findViewById(R.id.textViewUmaLinhaClienteTelefone);

        }
    }
}
