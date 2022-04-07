package encomenda.doces;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import encomenda.doces.modelos.Cliente;
import encomenda.doces.modelos.Doce;
import encomenda.doces.modelos.Encomenda;

public class DataBase extends SQLiteOpenHelper {

    public static final  String TABELA_DOCES = "TABELA_DOCES";
    public static final  String COLUNA_ID_DOCE = "ID_DOCE";
    public static final  String COLUNA_NOME_DOCE = "NOME_DOCE";
    public static final  String COLUNA_VALOR_DOCE = "VALOR_DOCE";
    public static final  String COLUNA_IMAGEM_DOCE = "IMAGEM_DOCE";


    public DataBase(@Nullable Context context) {
        super(context, "basedoces.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String  criarTabelaDoces = "CREATE TABLE " +TABELA_DOCES+ " ("+ COLUNA_ID_DOCE+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUNA_NOME_DOCE + " TEXT, "
                + COLUNA_VALOR_DOCE + " REAL, "
                +COLUNA_IMAGEM_DOCE + " TEXT)";

        sqLiteDatabase.execSQL(criarTabelaDoces);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public boolean adicionarUmDoceNoBanco(Doce doce){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUNA_NOME_DOCE, doce.getNome());
        cv.put(COLUNA_VALOR_DOCE, doce.getValor());
        cv.put(COLUNA_IMAGEM_DOCE, doce.getImagem());

        long insert = db.insert(TABELA_DOCES, null, cv);

        if(insert==-1){
            return  false;
        }else{
            return true;
        }
    }


    public ArrayList<Doce> pegarTodosDoces(){
        ArrayList<Doce> retorno = new ArrayList  <>();

        String query = "SELECT * FROM " +TABELA_DOCES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                int    doceId     = cursor.getInt(0);
                String doceNome   = cursor.getString(1);
                double  doceValor  = cursor.getDouble(2);
                int    doceImagem = cursor.getInt(3);
                Doce   doce       = new Doce(doceId, doceNome, doceValor, doceImagem);
                retorno.add(doce);
            }while (cursor.moveToNext());
        }else{
        }
        cursor.close();
        db.close();
        return  retorno;
    }


    public Doce pegarUmDoce(int idDoce){

        String query = "SELECT * FROM " +TABELA_DOCES+" WHERE " +COLUNA_ID_DOCE+ " = " + idDoce;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            int    doceId     = cursor.getInt(0);
            String doceNome   = cursor.getString(1);
            double  doceValor  = cursor.getDouble(2);
            int    doceImagem = cursor.getInt(3);
            Doce   doce       = new Doce(doceId, doceNome, doceValor, doceImagem);
            cursor.close();
            db.close();
            return doce;
        }else{
            cursor.close();
            db.close();
            return  new Doce(-1,"Doce bugado",0,0);
        }

    }


    public boolean deletarTodosDocesDoBanco() {

        String query = "DELETE FROM " +TABELA_DOCES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            return true;
        }else{
            return false;
        }
    }

    public boolean atualizaValorDoce(Doce doce){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUNA_VALOR_DOCE, doce.getValor());

        db.update(TABELA_DOCES, cv,COLUNA_ID_DOCE+" = "+doce.getId(),null);
        db.close();
        return true;
    }


}
