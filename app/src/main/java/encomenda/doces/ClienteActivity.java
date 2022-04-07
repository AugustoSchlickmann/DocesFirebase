package encomenda.doces;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import encomenda.doces.databinding.ActivityClienteBinding;
import encomenda.doces.modelos.Cliente;
import encomenda.doces.modelos.Encomenda;

public class ClienteActivity extends DrawerBaseActivity {

    ActivityClienteBinding activityClienteBinding;
    EditText nomeCliente, telefoneCliente;
    Button botaoSalvar, botaoVerEncomendas;

    Cliente cliente;
    boolean vendo = true;
    MenuItem cancelarEdicaoCliente;
    String ultimoCaracterDigitado = "";
    FirebaseFirestore banco = FirebaseFirestore.getInstance();
    boolean mudouCliente = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityClienteBinding = ActivityClienteBinding.inflate(getLayoutInflater());

        setContentView(activityClienteBinding.getRoot());

        cliente = new Cliente();
        cliente.setNome_telefone(getIntent().getStringExtra("nome_telefone"));
        setar();

        DocumentReference documentReference = banco.collection("Clientes").document(cliente.getNome_telefone());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
               if(documentSnapshot.getString("nome")!=null){
                   cliente = documentSnapshot.toObject(Cliente.class);
                   Log.i("ClienteActivity", "nome_telefone cliente: " + cliente.getNome_telefone());

                   colocarDados();
               }else{
                   //Vindo do clique no nome do cliente na EncomendaActivity, porém o cliente foi editado na tela do ClienteActivity,
                   // pois se editar na tela da encomenda atualiza o cliente na coleção Cliente e na Encomenda = arrumei!
                   Toast.makeText(ClienteActivity.this,"Talvez o Cliente Foi Deletado ou Alterado...", Toast.LENGTH_SHORT).show();
                   botaoVerEncomendas.setEnabled(false);
                   botaoSalvar.setEnabled(false);
               }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ClienteActivity.this,"Erro no banco de dados", Toast.LENGTH_SHORT).show();
            }
        });

        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idFirebaseCliente = cliente.getNome_telefone();
                if(!cliente.getNome().equals(nomeCliente.getText().toString())){
                    cliente.setNome(nomeCliente.getText().toString());
                    cliente.setNome_telefone(cliente.getNome()+"_"+cliente.getTelefone());
                    mudouCliente=true;
                }
                if(!cliente.getTelefone().equalsIgnoreCase(telefoneCliente.getText().toString())){
                    cliente.setTelefone(telefoneCliente.getText().toString());
                    cliente.setNome_telefone(cliente.getNome()+"_"+cliente.getTelefone());
                    mudouCliente=true;
                }

                if(mudouCliente){
                    banco.collection("Clientes").document(idFirebaseCliente).delete();
                    DocumentReference docRefCliente = banco.collection("Clientes").document(cliente.getNome_telefone());
                    docRefCliente.set(cliente, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            vendo();
                            Toast.makeText(ClienteActivity.this,"Cliente Atualizado", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //Atualizo TODAS as encomendas do cliente? SIM!
                    //Caso esse cliente possua  encomendas cadastrada, quero atualizar lá também!
                    banco.collection("Encomendas").whereEqualTo("cliente.nome_telefone",idFirebaseCliente).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            banco.collection("Encomendas").document(document.getId()).update("cliente", cliente);
                                        }
                                    } else {
                                    }
                                }
                            });
                }else{
                    vendo();
                    Toast.makeText(ClienteActivity.this,"Não há o que atualizar", Toast.LENGTH_SHORT).show();
                }


            }
        });

        botaoVerEncomendas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClienteActivity.this, ListaEncomendasActivity.class);
                intent.putExtra("nome_telefone" ,cliente.getNome_telefone());
                intent.putExtra("nomeCliente" ,cliente.getNome());
                ClienteActivity.this.startActivity(intent);
            }
        });

        telefoneCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vendo){
                    //ABRIR ZAP
                    String url = "https://api.whatsapp.com/send?phone="+telefoneCliente.getText().toString();
                    Intent zap = new Intent(Intent.ACTION_VIEW);
                    zap.setData(Uri.parse(url));
                    startActivity(zap);
                }
            }
        });

        telefoneCliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                Integer tamanhoTelefone = telefoneCliente.getText().toString().length();
                if(tamanhoTelefone>1){
                    ultimoCaracterDigitado = telefoneCliente.getText().toString().substring(tamanhoTelefone-1);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                Integer tamanhoTelefone = telefoneCliente.getText().toString().length();
                if(tamanhoTelefone==2){
                    if(!ultimoCaracterDigitado.equals(" ")){
                        telefoneCliente.append(" ");
                    }else{
                        telefoneCliente.getText().delete(tamanhoTelefone-1, tamanhoTelefone);
                    }
                }else if(tamanhoTelefone == 5){
                    if(!ultimoCaracterDigitado.equals(" ")){
                        telefoneCliente.append(" ");
                    }else{
                        telefoneCliente.getText().delete(tamanhoTelefone-1, tamanhoTelefone);
                    }
                }else if(tamanhoTelefone == 11){
                    if(!ultimoCaracterDigitado.equals("-")){
                        telefoneCliente.append("-");
                    }else{
                        telefoneCliente.getText().delete(tamanhoTelefone-1, tamanhoTelefone);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void setar() {
        nomeCliente = findViewById(R.id.editTextClienteNomeCliente);
        telefoneCliente = findViewById(R.id.editTextClienteTelefoneCliente);
        botaoSalvar = findViewById(R.id.botaoClienteSalvar);
        botaoVerEncomendas = findViewById(R.id.botaoVerEncomendasCliente);
    }

    private  void colocarDados(){
        nomeCliente.setText(cliente.getNome());
        telefoneCliente.setText(cliente.getTelefone());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        cancelarEdicaoCliente = menu.findItem(R.id.menu_CancelarEdicaoCliente);
        vendo();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cliente_menu,menu);
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

            case R.id.menu_Cadastrar_Encomenda:
                Intent intent = new Intent(ClienteActivity.this, CadastrarEncomendaActivity.class);
                intent.putExtra("cliente" ,cliente);
                ClienteActivity.this.startActivity(intent);
                break;

            case R.id.menu_EditarCliente:
                editando();
                break;

            case R.id.menu_ExcluirCliente:
                AlertDialog.Builder builder = new AlertDialog.Builder(ClienteActivity.this);
                builder.setMessage("Deletar "+ cliente.getNome()+" e Todas as Suas Encomendas?").setTitle("Confirmar Ação")
                        .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                banco.collection("Clientes").document(cliente.getNome_telefone()).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(ClienteActivity.this,"Cliente Deletado", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ClienteActivity.this,"Erro ao Deletar Cliente", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                banco.collection("Encomendas").whereEqualTo("cliente.nome_telefone", cliente.getNome_telefone()).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                banco.collection("Encomendas").document(document.getId()).delete();
                                            }
                                            Intent i = new Intent(ClienteActivity.this, MainActivity.class);
                                            startActivity(i);
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).setIcon(R.drawable.ic_baseline_warning_24).show();
                break;

            case R.id.menu_CancelarEdicaoCliente:
                vendo();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void vendo(){
        vendo=true;
        allocateActivityTitle(cliente.getNome());
        cancelarEdicaoCliente.setVisible(false);
        botaoSalvar.setVisibility(View.INVISIBLE);
        nomeCliente.setFocusable(false);
        telefoneCliente.setFocusable(false);
    }

    public void editando(){
        vendo=false;
        allocateActivityTitle("Editando Cliente...");
        cancelarEdicaoCliente.setVisible(true);
        botaoSalvar.setVisibility(View.VISIBLE);
        nomeCliente.setFocusableInTouchMode(true);
        telefoneCliente.setFocusableInTouchMode(true);

    }


}