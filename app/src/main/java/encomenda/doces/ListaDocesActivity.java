package encomenda.doces;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import encomenda.doces.databinding.ActivityListaDocesBinding;
import encomenda.doces.modelos.Doce;
import encomenda.doces.modelos.Encomenda;

public class ListaDocesActivity extends DrawerBaseActivity {

    ActivityListaDocesBinding activityListaDocesBinding;
    private RecyclerView recyclerView;

    DataBase dataBase;
    int operacao;

    FirebaseFirestore banco = FirebaseFirestore.getInstance();
    ArrayList<Doce> doces;
    ArrayList<Doce> docesaFazer;
    ArrayList<Encomenda> encomendas;
    Calendar calendar;
    AdapterRecycleViewDoces adapterRecycleViewDoces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityListaDocesBinding = ActivityListaDocesBinding.inflate(getLayoutInflater());
        setContentView(activityListaDocesBinding.getRoot());

        dataBase = new DataBase(this);
        recyclerView = findViewById(R.id.recycleViewDoces);
        adapterRecycleViewDoces = new AdapterRecycleViewDoces(this);

        calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        operacao = getIntent().getIntExtra("tipo",0);

        if(operacao == 0){
            allocateActivityTitle("Doces");
            adapterRecycleViewDoces.setDoces(dataBase.pegarTodosDoces());
            recyclerView.setAdapter(adapterRecycleViewDoces);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        }else{
            doces = dataBase.pegarTodosDoces();
            docesaFazer = new ArrayList<>();
            encomendas = new ArrayList<>();
            allocateActivityTitle("Doces a Fazer");
            banco.collection("Encomendas").whereGreaterThanOrEqualTo("dataJava",calendar.getTime()).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    encomendas.add(document.toObject(Encomenda.class));
                                    encomendas.get(encomendas.size()-1).setIdFirebase(document.getId());
                                }
                               //Triple For... SQL > all
                                juntarDoces();
                            } else {
                                Log.i("Main", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

    }

    private void juntarDoces(){
        for (Encomenda encomenda:encomendas){
            for (Doce doce: encomenda.getDoces()){
                for(Doce doceaFazer : doces){
                    if(doceaFazer.getId()==doce.getId()){
                        doceaFazer.setQtd(doceaFazer.getQtd()+doce.getQtd());
                    }
                }
            }
        }
        for (Doce doce:doces){
            if(doce.getQtd()>0){
                docesaFazer.add(doce);
            }
        }
        adapterRecycleViewDoces.setDoces(docesaFazer);
        recyclerView.setAdapter(adapterRecycleViewDoces);
        recyclerView.setLayoutManager(new LinearLayoutManager(ListaDocesActivity.this));

    }



}