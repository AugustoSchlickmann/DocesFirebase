package encomenda.doces;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

import encomenda.doces.databinding.ActivityListaClientesBinding;
import encomenda.doces.modelos.Cliente;

public class ListaClientesActivity extends DrawerBaseActivity {

    ActivityListaClientesBinding activityListaClientesBinding;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private AppBarLayout barLayout;

    AdapterRecycleViewClientes adapterRecycleViewClientes;

    FirebaseFirestore banco = FirebaseFirestore.getInstance();
    ArrayList<Cliente> clientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityListaClientesBinding = ActivityListaClientesBinding.inflate(getLayoutInflater());

        setContentView(activityListaClientesBinding.getRoot());
        allocateActivityTitle("Clientes");

        recyclerView = findViewById(R.id.recycleViewClientes);
        searchView = findViewById(R.id.searchViewListaClientes);
        barLayout= findViewById(R.id.appBarListaClientes);

        clientes = new ArrayList<>();

        banco.collection("Clientes").orderBy("nome").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                clientes.add(document.toObject(Cliente.class));
                            }
                            adapterRecycleViewClientes = new AdapterRecycleViewClientes(ListaClientesActivity.this);
                            adapterRecycleViewClientes.setClientes(clientes);
                            recyclerView.setAdapter(adapterRecycleViewClientes);
                            recyclerView.setLayoutManager(new LinearLayoutManager(ListaClientesActivity.this));

                        } else {
                            Log.i("ListaClientes", "Error getting documents: ", task.getException());
                        }
                    }
                });

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String texto) {
                filtrar(texto);
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

    private void filtrar(String textoPesquisado) {
        ArrayList<Cliente> clientesFiltrados = new ArrayList<>();

        for (Cliente cliente : clientes){
            if(cliente.getNome().toLowerCase().contains(textoPesquisado.toLowerCase())){
                clientesFiltrados.add(cliente);
            }
        }
        adapterRecycleViewClientes.setClientes(clientesFiltrados);
        adapterRecycleViewClientes.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lista_clientes_menu,menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_listaClienteAZ:
                Collections.sort(adapterRecycleViewClientes.getClientes(), Cliente.ClienteAZ);
                adapterRecycleViewClientes.notifyDataSetChanged();
                Toast.makeText(this, "Ordenando de A a Z", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_listaClienteZA:
                Collections.sort(adapterRecycleViewClientes.getClientes(), Cliente.ClienteZA);
                adapterRecycleViewClientes.notifyDataSetChanged();
                Toast.makeText(this, "Ordenando de Z a A", Toast.LENGTH_SHORT).show();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

}