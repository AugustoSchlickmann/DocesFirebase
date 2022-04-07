package encomenda.doces.modelos;

import android.os.Parcel;
import android.os.Parcelable;

public class Doce implements Parcelable {

    int id;
    String nome;
    double valor;
    int imagem;
    int qtd = 0;

    public Doce(){

    }


    public Doce(String nome, double valor, int imagem, int qtd) {
        this.nome = nome;
        this.valor = valor;
        this.imagem = imagem;
        this.qtd = qtd;
    }


    public Doce(String nome, double valor, int imagem) {
        this.nome = nome;
        this.valor = valor;
        this.imagem = imagem;
    }

    public Doce(int id, String nome, double valor, int imagem) {
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.imagem = imagem;
    }

    public Doce(int id, String nome, double valor, int imagem, int qtd) {
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.imagem = imagem;
        this.qtd = qtd;
    }

    protected Doce(Parcel in) {
        id = in.readInt();
        nome = in.readString();
        valor = in.readFloat();
        imagem = in.readInt();
        qtd = in.readInt();
    }

    public static final Creator<Doce> CREATOR = new Creator<Doce>() {
        @Override
        public Doce createFromParcel(Parcel in) {
            return new Doce(in);
        }

        @Override
        public Doce[] newArray(int size) {
            return new Doce[size];
        }
    };

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public int getQtd() { return qtd; }

    public void setQtd(int qtd) { this.qtd = qtd; }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public int getImagem() { return imagem; }

    public void setImagem(int imagem) {
        this.imagem = imagem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(nome);
        parcel.writeDouble(valor);
        parcel.writeInt(imagem);
        parcel.writeInt(qtd);
    }
}
