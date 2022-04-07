package encomenda.doces;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import encomenda.doces.modelos.Doce;

public class AdapterSpinnerDoces extends BaseAdapter {

    Context context;
    ArrayList<Doce> doces;

    public AdapterSpinnerDoces(Context context, ArrayList<Doce> doces) {
        this.context = context;
        this.doces = doces;
    }

    @Override
    public int getCount() {
        return doces.size();
    }

    @Override
    public Doce getItem(int i) {
        return doces.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.uma_linha_spinner_doce, viewGroup, false);
        ImageView imagem = rootView.findViewById(R.id.imageViewUmaLinhaSpinnerImagemDoce);
        TextView txtNome = rootView.findViewById(R.id.textViewUmaLinhaSpinnerNomeDoce);
        TextView  txtValor = rootView.findViewById(R.id.textViewUmaLinhaSpinnerValorDoce);

        imagem.setImageResource(doces.get(i).getImagem());
        txtNome.setText(doces.get(i).getNome());
        txtValor.setText(String.valueOf(doces.get(i).getValor()));

        return rootView;
    }

    public ArrayList<Doce> getDoces() {
        return doces;
    }

    public void setDoces(ArrayList<Doce> doces) {
        this.doces = doces;
    }
}
