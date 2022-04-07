package encomenda.doces;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
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

public class AdapterRecycleViewDocesEncomenda extends RecyclerView.Adapter<AdapterRecycleViewDocesEncomenda.ViewHolder> {

    private Context context;
    private ArrayList<Doce> doces;


    public AdapterRecycleViewDocesEncomenda (Context context, ArrayList<Doce>doces) {
        this.context = context;
        this.doces = doces;
    }


    public ArrayList<Doce> getDoces() {
        return doces;
    }

    public void setDoces(ArrayList<Doce> doces) {
        this.doces = doces;
    }


    @NonNull
    @Override
    public AdapterRecycleViewDocesEncomenda.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.uma_linha_doce, parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRecycleViewDocesEncomenda.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.imagem.setImageResource(doces.get(position).getImagem());
        holder.txtNome.setText(doces.get(position).getNome());
        holder.txtValor.setText(String.valueOf(doces.get(position).getQtd()));

        //MUDAR QTD DOCE DA LISTA
        holder.constraint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialogo_editar_doce);

                ImageView imagem = dialog.findViewById(R.id.imageViewDialogoEditarDoce);
                TextView titulo = dialog.findViewById(R.id.textViewDialogoEditarDoce);
                EditText valor = dialog.findViewById(R.id.editTextNumberDecimal);
                Button botaoOK = dialog.findViewById(R.id.buttonDialogoEditarDoceOK);

                valor.setInputType(InputType.TYPE_CLASS_NUMBER);

                imagem.setImageResource(doces.get(position).getImagem());
                titulo.setText("Mudar Quantidade "+ doces.get(position).getNome()+ " ?");
                valor.setText(String.valueOf(doces.get(position).getQtd()));
                dialog.show();

                botaoOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            //MUDAR QTD DOCE DA LISTA
                            int qtdAntes = doces.get(position).getQtd();
                            doces.get(position).setQtd(Integer.parseInt(valor.getText().toString()));
                            holder.txtValor.setText(String.valueOf(doces.get(position).getQtd()));

                            if(context instanceof CadastrarEncomendaActivity){
                                ((CadastrarEncomendaActivity)context).mudouQuantidadeRecyclerView(qtdAntes, doces.get(position));
                                dialog.dismiss();
                            }
                            if(context instanceof EncomendaActivity) {
                                ((EncomendaActivity) context).mudouQuantidadeRecyclerView(qtdAntes, doces.get(position));
                                dialog.dismiss();
                            }
                        }catch (Exception e ){
                            Toast.makeText(context,"Quantidade Inválida", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //TIRAR DOCE DA LISTA
        holder.constraint.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialogo_remover_doce);

                ImageView imagem = dialog.findViewById(R.id.imageViewDialogoRemoverDoce);
                TextView titulo = dialog.findViewById(R.id.textViewDialogoRemoverDoce);
                Button botaoOK = dialog.findViewById(R.id.buttonDialogoRemoverDoceOK);
                Button botaoCancelar = dialog.findViewById(R.id.buttonDialogoRemoverDoceCancelar);

                imagem.setImageResource(doces.get(position).getImagem());
                titulo.setText("Remover "+ doces.get(position).getNome()+ " ?");
                dialog.show();

                botaoOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(context instanceof CadastrarEncomendaActivity){
                            ((CadastrarEncomendaActivity)context).removeuDaRecyclerView(doces.get(position));
                        }
                        if(context instanceof EncomendaActivity){
                            ((EncomendaActivity)context).removeuDaRecyclerView(doces.get(position));
                        }
                        dialog.dismiss();
                    }
                });

                botaoCancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            dialog.dismiss();
                    }
                });

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
