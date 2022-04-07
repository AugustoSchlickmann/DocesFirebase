package encomenda.doces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import encomenda.doces.databinding.ActivityMainBinding;
import encomenda.doces.modelos.Doce;
import encomenda.doces.modelos.Encomenda;

public class MainActivity extends DrawerBaseActivity{

    ActivityMainBinding activityMainBinding;
    private RecyclerView recyclerView;
    DataBase dataBase;
    FirebaseFirestore banco = FirebaseFirestore.getInstance();
    ArrayList<Encomenda> encomendas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        allocateActivityTitle("Encomendas");

        recyclerView = findViewById(R.id.recyclerViewMainActivity);


        dataBase= new DataBase(this);
         if(dataBase.pegarUmDoce(2).getId()==-1){ criarDocesBanco();}

        encomendas = new ArrayList<>();
        Calendar  calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

       // banco.collection("Encomendas").whereArrayContains("arrayIdDoces",9).get()

        banco.collection("Encomendas").whereGreaterThanOrEqualTo("dataJava",calendar.getTime()).orderBy("dataJava").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                encomendas.add(document.toObject(Encomenda.class));
                                encomendas.get(encomendas.size()-1).setIdFirebase(document.getId());
                            }
                            AdapterRecycleViewMainActivity adapterRecycleViewMainActivity = new AdapterRecycleViewMainActivity(MainActivity.this, encomendas);
                            recyclerView.setAdapter(adapterRecycleViewMainActivity);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        } else {
                            Log.i("Main", "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

    private void criarDocesBanco() {
        Log.i("Main", "CRIANDO OS DOCES NO SQLITE ");

        ArrayList<Doce> doces = new ArrayList<>();

        doces.add(new Doce("Beijinho", 1.10, R.drawable.beijinho));
        doces.add(new Doce("Brigadeiro", 1.10, R.drawable.brigadeiro));
        doces.add(new Doce("Brigadeiro Branco", 1.10, R.drawable.brigadeiro_branco));
        doces.add(new Doce("Caju", 1.10, R.drawable.caju));
        doces.add(new Doce("Casadinho", 1.10, R.drawable.casadinho));

        doces.add(new Doce("Churros", 1.10, R.drawable.churros));
        doces.add(new Doce("Morango", 1.10, R.drawable.morango));
        doces.add(new Doce("Rosado", 1.10, R.drawable.rosado));
        doces.add(new Doce("Ninho", 1.50, R.drawable.ninho));
        doces.add(new Doce("Nozes", 1.50, R.drawable.nozes));

        doces.add(new Doce("Olho de Sogra", 1.50, R.drawable.olho_sogra));
        doces.add(new Doce("Olho de Sogro", 1.50, R.drawable.olho_sogro));

        for (Doce doce : doces){
            dataBase.adicionarUmDoceNoBanco(doce);
        }
    }

}