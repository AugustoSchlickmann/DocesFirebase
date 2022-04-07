package encomenda.doces;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import encomenda.doces.modelos.Doce;

public class AdapterRecycleViewDoces extends RecyclerView.Adapter<AdapterRecycleViewDoces.ViewHolder> {

    private ArrayList<Doce> doces;
    private Context context;
    DataBase dataBase;

    public AdapterRecycleViewDoces(Context context) {
        this.context = context;
        dataBase = new DataBase(context);
    }

    public ArrayList<Doce> getDoces() {
        return doces;
    }

    public void setDoces(ArrayList<Doce> doces) {
        this.doces = doces;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.uma_linha_doce, parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRecycleViewDoces.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.imagem.setImageResource(doces.get(position).getImagem());
        holder.txtNome.setText(doces.get(position).getNome());

        if(((ListaDocesActivity)context).operacao==0){
            holder.txtValor.setText(String.valueOf(doces.get(position).getValor()));

            holder.constraint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialogo_editar_doce);

                    ImageView imagem = dialog.findViewById(R.id.imageViewDialogoEditarDoce);
                    TextView titulo = dialog.findViewById(R.id.textViewDialogoEditarDoce);
                    EditText valor = dialog.findViewById(R.id.editTextNumberDecimal);
                    Button botaoOK = dialog.findViewById(R.id.buttonDialogoEditarDoceOK);

                    imagem.setImageResource(doces.get(position).getImagem());
                    titulo.setText("Mudar Valor "+ doces.get(position).getNome()+ " ?");
                    valor.setText(String.valueOf(doces.get(position).getValor()));
                    dialog.show();

                    botaoOK.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                doces.get(position).setValor(Double.parseDouble(valor.getText().toString()));
                                holder.txtValor.setText(String.valueOf(doces.get(position).getValor()));
                                dialog.dismiss();
                                dataBase.atualizaValorDoce(doces.get(position));
                            }catch (Exception e ){
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });

        }else{
            holder.txtValor.setText(String.valueOf(doces.get(position).getQtd()));

            holder.constraint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //LISTAR PROXIMAS ENCOMENDAS QUE TEM O DOCE CLICADO
                    Intent intent5 = new Intent(context, ListaEncomendasActivity.class);
                    intent5.putExtra("idDoce" ,doces.get(position).getId());
                    intent5.putExtra("imagemDoce" ,doces.get(position).getImagem());
                    context.startActivity(intent5);
                }
            });
        }

        holder.constraint.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(context, "LONG CLICK " + doces.get(position).getNome(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return doces.size();
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
        private ImageView imagem;
        private TextView txtNome, txtValor;

        public  ViewHolder(View itemview){
            super(itemview);
            constraint = itemview.findViewById(R.id.constraintLayoutUmaLinhaSpinnerDoce);
            imagem = itemview.findViewById(R.id.imageViewUmaLinhaSpinnerImagemDoce);
            txtNome = itemview.findViewById(R.id.textViewUmaLinhaSpinnerNomeDoce);
            txtValor = itemview.findViewById(R.id.textViewUmaLinhaSpinnerValorDoce);
        }

    }

}
