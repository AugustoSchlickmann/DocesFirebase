package encomenda.doces;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import encomenda.doces.databinding.ActivityListaEncomendasBinding;
import encomenda.doces.modelos.Cliente;
import encomenda.doces.modelos.Encomenda;

public class ListaEncomendasActivity extends  DrawerBaseActivity{

    ActivityListaEncomendasBinding activityListaEncomendasBinding;
    private RecyclerView recyclerView;
    DataBase dataBase;
    ArrayList<Encomenda> encomendas;
    private TextView textViewDataSelecionada;
    int ano;
    int mes;
    int dia;
    int operacao=0;
    private SearchView searchView;
    private AppBarLayout barLayout;
    MenuItem calendarioIcon;

    FirebaseFirestore banco = FirebaseFirestore.getInstance();
    Calendar  calendar;
    AdapterRecycleViewMainActivity adapterRecycleViewMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityListaEncomendasBinding = ActivityListaEncomendasBinding.inflate(getLayoutInflater());

        setContentView(activityListaEncomendasBinding.getRoot());

        encomendas = new ArrayList<>();
        dataBase= new DataBase(this);
        adapterRecycleViewMainActivity = new AdapterRecycleViewMainActivity(ListaEncomendasActivity.this, encomendas);

        recyclerView = findViewById(R.id.recyclerViewListaEncomendas);
        recyclerView.setAdapter(adapterRecycleViewMainActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(ListaEncomendasActivity.this));

        textViewDataSelecionada = findViewById(R.id.textViewListaEncomendas);
        textViewDataSelecionada.setVisibility(View.GONE);

        barLayout = findViewById(R.id.appBarListaEncomendas);
        searchView = findViewById(R.id.editTextListaPesquisa);
        searchView.setVisibility(View.GONE);

        calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
        ano = calendar.get(Calendar.YEAR);
        mes = calendar.get(Calendar.MONDAY);
        dia = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        operacao = getIntent().getIntExtra("tipo",0);
        int idDoce = getIntent().getIntExtra("idDoce",-1);

        if(operacao==1) {
            //Procurar encomendas escolhendo uma data
            allocateActivityTitle("Escolha a Data");
        } else if(operacao==2){
            //Procurar por nome de cliente
            searchView.setVisibility(View.VISIBLE);
            allocateActivityTitle("Digite Um Nome");
        }else if(idDoce!=-1){
            //Listar encomendas que possuem o doce clicado na recyclerView dos doces a fazer
            int imagemDoce = getIntent().getIntExtra("imagemDoce",-1);
            allocateActivityTitleLogo("   Encomendados",imagemDoce);
            pesquisarEncomendasFuturasComIdDoce(idDoce);
        }else{
            //Listar encomendas do cliente, clicando no botao do perfil dele
            allocateActivityTitle(getIntent().getStringExtra("nomeCliente"));
            String nomeTelefone = getIntent().getStringExtra("nome_telefone");
            pesquisarEncomendasDoCliente(nomeTelefone);

        }


        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                allocateActivityTitle(query);
                banco.collection("Encomendas").whereEqualTo("cliente.nome",query).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            encomendas.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                encomendas.add(document.toObject(Encomenda.class));
                                encomendas.get(encomendas.size()-1).setIdFirebase(document.getId());
                            }
                            if(encomendas.size()==0){
                                textViewDataSelecionada.setVisibility(View.VISIBLE);
                                textViewDataSelecionada.setText("Não Há Encomendas Com Este Nome");
                            }else{
                                textViewDataSelecionada.setVisibility(View.GONE);
                            }
                            adapterRecycleViewMainActivity.setEncomendas(encomendas);
                            adapterRecycleViewMainActivity.notifyDataSetChanged();
                            recyclerView.setAdapter(adapterRecycleViewMainActivity);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ListaEncomendasActivity.this));
                        } else {
                            Log.i("ListaEncomendasAct", "Error getting documents: ", task.getException());
                        }
                    }
                });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String texto) {
                return false;
            }
        });

        barLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.onActionViewExpanded();
            }
        });

    }


    private void abrirCalendario() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(ListaEncomendasActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                       if(operacao==1){
                           String dataBR = String.format("%02d%02d%02d",day, month+1, year);
                           dataBR = String.format("%s/%s/%s", dataBR.substring(0, 2), dataBR.substring(2, 4), dataBR.substring(4, 8));
                           allocateActivityTitle(dataBR);

                            banco.collection("Encomendas").whereEqualTo("data",dataBR).orderBy("dataJava").get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        encomendas.clear();
                                       for (QueryDocumentSnapshot document : task.getResult()) {
                                           Log.i("ListaEncomendas", document.getId() + " => " + document.getData());
                                           encomendas.add(document.toObject(Encomenda.class));
                                           encomendas.get(encomendas.size()-1).setIdFirebase(document.getId());
                                       }
                                        adapterRecycleViewMainActivity.setEncomendas(encomendas);
                                        adapterRecycleViewMainActivity.notifyDataSetChanged();
                                        if(encomendas.size()==0){
                                            textViewDataSelecionada.setVisibility(View.VISIBLE);
                                            textViewDataSelecionada.setText("Não Há Encomendas Nesta Data");
                                        }else{
                                            textViewDataSelecionada.setVisibility(View.GONE);
                                        }
                                   } else {
                                       Log.i("Main", "Error getting documents: ", task.getException());
                                   }
                                    for(Encomenda encomenda:encomendas){
                                        Log.i("ListaEncomendas", "idFirebase" + " => " + encomenda.getIdFirebase());
                                    }
                               }
                           });
                       }else{
                           return;
                       }
                    }
                    }, ano, mes, dia);
        datePickerDialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        calendarioIcon = menu.findItem(R.id.menu_lista_encomenda_AbrirCalendario);
        if(operacao == 1){
            calendarioIcon.setVisible(true);
        }else{
            calendarioIcon.setVisible(false);
        }return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lista_encomenda_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_lista_encomenda_AbrirCalendario:
               abrirCalendario();
               break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pesquisarEncomendasFuturasComIdDoce(int idDoce) {

        banco.collection("Encomendas").whereArrayContains("arrayIdDoces",idDoce).whereGreaterThanOrEqualTo("dataJava",calendar.getTime()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            encomendas.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                encomendas.add(document.toObject(Encomenda.class));
                                encomendas.get(encomendas.size()-1).setIdFirebase(document.getId());
                            }
                            adapterRecycleViewMainActivity.setEncomendas(encomendas);
                            recyclerView.setAdapter(adapterRecycleViewMainActivity);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ListaEncomendasActivity.this));
                        } else {
                            Log.i("ListaEncomendasAct", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void pesquisarEncomendasDoCliente(String nome_telefone) {
        banco.collection("Encomendas").whereEqualTo("cliente.nome_telefone",nome_telefone).orderBy("dataJava").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            encomendas.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                encomendas.add(document.toObject(Encomenda.class));
                                encomendas.get(encomendas.size()-1).setIdFirebase(document.getId());
                            }
                            adapterRecycleViewMainActivity.setEncomendas(encomendas);
                            recyclerView.setAdapter(adapterRecycleViewMainActivity);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ListaEncomendasActivity.this));
                        } else {
                            Log.i("ListaEncomendasAct", "Erro pegando encomendas do: "+nome_telefone, task.getException());
                        }
                    }
                });
    }
}
