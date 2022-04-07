package encomenda.doces.modelos;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

public class Encomenda implements Parcelable {

    String data;
    String hora;
    Cliente cliente;
    ArrayList<Doce> doces;
    String obs;
    Date dataJava;
    String idFirebase;
    ArrayList<Integer> arrayIdDoces;

    public Encomenda(){
        this.obs="";
        cliente = new Cliente();
        doces = new ArrayList<>();
        arrayIdDoces = new ArrayList<>();
    }

    protected Encomenda(Parcel in) {
        data = in.readString();
        hora = in.readString();
        obs = in.readString();
        idFirebase = in.readString();
    }

    public static final Creator<Encomenda> CREATOR = new Creator<Encomenda>() {
        @Override
        public Encomenda createFromParcel(Parcel in) {
            return new Encomenda(in);
        }

        @Override
        public Encomenda[] newArray(int size) {
            return new Encomenda[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(data);
        parcel.writeString(hora);
        parcel.writeString(obs);
        parcel.writeString(idFirebase);
    }

    public String getData() { return data; }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() { return hora; }

    public void setHora(String hora) { this.hora = hora; }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }


    public ArrayList<Doce> getDoces() {
        return doces;
    }

    public void setDoces(ArrayList<Doce> doces) {
        this.doces = doces;
    }


    public String getObs() { return obs; }

    public void setObs(String obs) { this.obs = obs; }

    public Date getDataJava() {
        return dataJava;
    }

    public void setDataJava(Date dataJava) {
        this.dataJava = dataJava;
    }

    public String getIdFirebase() {
        return idFirebase;
    }

    public void setIdFirebase(String idFirebase) {
        this.idFirebase = idFirebase;
    }

    public ArrayList<Integer> getArrayIdDoces() { return arrayIdDoces; }

    public void setArrayIdDoces(ArrayList<Integer> arrayIdDoces) { this.arrayIdDoces = arrayIdDoces; }
}
