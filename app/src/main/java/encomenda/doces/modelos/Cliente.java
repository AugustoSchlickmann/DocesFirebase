package encomenda.doces.modelos;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class Cliente implements Parcelable {

    String nome;
    String telefone;
    String nome_telefone;

    public Cliente(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    public Cliente(String nome, String telefone, String nome_telefone) {
        this.nome = nome;
        this.telefone = telefone;
        this.nome_telefone = nome_telefone;
    }

    public Cliente() {
        this.nome = "";
        this.telefone = "";
        this.nome_telefone="";
    }

    protected Cliente(Parcel in) {
        nome = in.readString();
        telefone = in.readString();
        nome_telefone = in.readString();
    }

    public static final Creator<Cliente> CREATOR = new Creator<Cliente>() {
        @Override
        public Cliente createFromParcel(Parcel in) {
            return new Cliente(in);
        }

        @Override
        public Cliente[] newArray(int size) {
            return new Cliente[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(nome);
        parcel.writeString(telefone);
        parcel.writeString(nome_telefone);
    }

    public String getNome_telefone() {
        return nome_telefone;
    }

    public void setNome_telefone(String nome_telefone) {
        this.nome_telefone = nome_telefone;
    }


    public String getNome() { return nome; }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public static Comparator<Cliente> ClienteAZ = new Comparator<Cliente>() {
        @Override
        public int compare(Cliente c1, Cliente c2) {
            return c1.getNome().compareTo(c2.getNome());
        }
    };

    public static Comparator<Cliente> ClienteZA = new Comparator<Cliente>() {
        @Override
        public int compare(Cliente c1, Cliente c2) {
            return c2.getNome().compareTo(c1.getNome());
        }
    };

}
