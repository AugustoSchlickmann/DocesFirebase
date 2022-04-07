package encomenda.doces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import encomenda.doces.databinding.ActivityEncomendaBinding;
import encomenda.doces.modelos.Doce;
import encomenda.doces.modelos.Encomenda;

public class EncomendaActivity extends DrawerBaseActivity {

    ActivityEncomendaBinding activityEncomendaBinding;
    EditText nomeCliente, telefoneCliente, data, hora;
    TextView  textViewValorTotal;
    RecyclerView recyclerView;
    FloatingActionButton floatingActionButton;
    Button botaoSalvar;
    ImageView avisoObs;

    Spinner spinnerDoces;
    AdapterSpinnerDoces adapterSpinnerDoces;
    AdapterRecycleViewDocesEncomenda adapterRecycleViewDocesEncomenda;

    Encomenda encomenda;
    boolean editando = false;
    MenuItem cancelarEdicao;
    String ultimoCaracterDigitado = "";
    DataBase dataBase;
    double valorTotal;
    DecimalFormat decimalFormat = new DecimalFormat("#.##");
    FirebaseFirestore banco = FirebaseFirestore.getInstance();
    String avisoInputs="";
    Calendar calendar;
    String idFirebase="";
    boolean mudouCliente = false;
    boolean mudouDataHora = false;
    boolean mudouDoces = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityEncomendaBinding = ActivityEncomendaBinding.inflate(getLayoutInflater());

        setContentView(activityEncomendaBinding.getRoot());

        setar();

        encomenda = new Encomenda();
        idFirebase = getIntent().getStringExtra("encomendaIdFirebase");

        DocumentReference documentReference = banco.collection("Encomendas").document(idFirebase);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                encomenda = documentSnapshot.toObject(Encomenda.class);
                colocarDados();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EncomendaActivity.this,"Erro carregando encomenda", Toast.LENGTH_SHORT).show();
            }
        });

        dataBase = new DataBase(this);
        adapterSpinnerDoces = new AdapterSpinnerDoces(EncomendaActivity.this, dataBase.pegarTodosDoces());
        spinnerDoces.setAdapter(adapterSpinnerDoces);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapterSpinnerDoces.getCount()>0){

                    Doce doceAdicionadoNaLista = adapterSpinnerDoces.getDoces().get(spinnerDoces.getSelectedItemPosition());
                    Dialog dialog = new Dialog(EncomendaActivity.this);
                    dialog.setContentView(R.layout.dialogo_editar_doce);

                    ImageView imagem = dialog.findViewById(R.id.imageViewDialogoEditarDoce);
                    TextView titulo = dialog.findViewById(R.id.textViewDialogoEditarDoce);
                    EditText qtd = dialog.findViewById(R.id.editTextNumberDecimal);
                    Button botaoOK = dialog.findViewById(R.id.buttonDialogoEditarDoceOK);

                    qtd.setInputType(InputType.TYPE_CLASS_NUMBER);
                    imagem.setImageResource(doceAdicionadoNaLista.getImagem());
                    titulo.setText("Quantidade de "+ doceAdicionadoNaLista.getNome());
                    dialog.show();

                    botaoOK.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                if(cadastroDuplo(doceAdicionadoNaLista)){
                                    Toast.makeText(EncomendaActivity.this,"Doce já está na lista", Toast.LENGTH_SHORT).show();
                                }else{
                                    doceAdicionadoNaLista.setQtd(Integer.parseInt(qtd.getText().toString()));
                                    adicionarNaRecyclerView(doceAdicionadoNaLista);
                                }

                                dialog.dismiss();
                            }catch (Exception e ){
                                Toast.makeText(EncomendaActivity.this,"Quantidade Inválida", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });



        botaoSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validarInputs()){
                    try{
                        String idFirebaseCliente = encomenda.getCliente().getNome_telefone();
                        if(!encomenda.getCliente().getNome().equals(nomeCliente.getText().toString())){
                            encomenda.getCliente().setNome(nomeCliente.getText().toString());
                            encomenda.getCliente().setNome_telefone(encomenda.getCliente().getNome()+"_"+encomenda.getCliente().getTelefone());
                            mudouCliente=true;
                        }
                        if(!encomenda.getCliente().getTelefone().equals(telefoneCliente.getText().toString())){
                            encomenda.getCliente().setTelefone(telefoneCliente.getText().toString());
                            encomenda.getCliente().setNome_telefone(encomenda.getCliente().getNome()+"_"+encomenda.getCliente().getTelefone());
                            mudouCliente=true;
                        }

                        if(mudouCliente){
                            //Se mudou o cliente, vou é deletar ele e cadastrar um novo!
                            banco.collection("Clientes").document(idFirebaseCliente).delete();
                            DocumentReference docRefCliente = banco.collection("Clientes").document(encomenda.getCliente().getNome_telefone());
                            docRefCliente.set(encomenda.getCliente(), SetOptions.merge());
                            //Caso esse cliente possua outras encomendas cadastrada, quero atualizar lá também!
                            banco.collection("Encomendas").whereEqualTo("cliente.nome_telefone",idFirebaseCliente).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                   banco.collection("Encomendas").document(document.getId()).update("cliente", encomenda.getCliente());
                                                }
                                            } else {
                                                Log.i("Main", "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });
                        }
                        if(mudouDoces || mudouDataHora || mudouCliente){
                            DocumentReference docRefEncomenda = banco.collection("Encomendas").document(idFirebase);
                            docRefEncomenda.set(encomenda, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FcmNotificationSender fcmNotificationSender = new FcmNotificationSender(
                                            "/topics/todos",
                                            EntrarActivity.nomeUsuario+" mudou a encomenda de "+encomenda.getCliente().getNome(),
                                            "Data: "+encomenda.getData()+"\nHora: "+encomenda.getHora(),
                                            getApplicationContext(),
                                            EncomendaActivity.this);
                                    fcmNotificationSender.sendNotifications();

                                    Toast.makeText(EncomendaActivity.this,"Encomenda Atualizada", Toast.LENGTH_SHORT).show();
                                    vendo();
                                }
                            });
                        }else{
                            Toast.makeText(EncomendaActivity.this,"Não há o que atualizar", Toast.LENGTH_SHORT).show();
                        }


                    }catch (Exception e){
                        Toast.makeText(EncomendaActivity.this,"Erro na Edição", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(EncomendaActivity.this, avisoInputs, Toast.LENGTH_SHORT).show();
                }
            }
        });



        nomeCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editando){
                    Intent intent = new Intent(EncomendaActivity.this, ClienteActivity.class);
                    intent.putExtra("nome_telefone" ,encomenda.getCliente().getNome_telefone());
                    EncomendaActivity.this.startActivity(intent);
                }
            }
        });

        telefoneCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editando){
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
                }else if(tamanhoTelefone == 5) {
                    if (!ultimoCaracterDigitado.equals(" ")) {
                        telefoneCliente.append(" ");
                    } else {
                        telefoneCliente.getText().delete(tamanhoTelefone - 1, tamanhoTelefone);
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

        hora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirRelogio();
                nomeCliente.clearFocus();
                telefoneCliente.clearFocus();
            }
        });

        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirCalendario();
                nomeCliente.clearFocus();
                telefoneCliente.clearFocus();
            }
        });

        avisoObs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirDialogoObs();
                nomeCliente.clearFocus();
                telefoneCliente.clearFocus();
            }
        });
    }

    public void adicionarNaRecyclerView(Doce doceAdicionado){
        encomenda.getArrayIdDoces().add(doceAdicionado.getId());
        encomenda.getDoces().add(doceAdicionado);
        adapterRecycleViewDocesEncomenda.notifyDataSetChanged();
        valorTotal += doceAdicionado.getValor() * doceAdicionado.getQtd();
        textViewValorTotal.setText("R$: "+decimalFormat.format(valorTotal));
        mudouDoces=true;

    }

    public void removeuDaRecyclerView(Doce doceRemovido){
        encomenda.getArrayIdDoces().remove(doceRemovido.getId());
        encomenda.getDoces().remove(doceRemovido);
        adapterRecycleViewDocesEncomenda.notifyDataSetChanged();
        valorTotal -= doceRemovido.getValor() * doceRemovido.getQtd();
        textViewValorTotal.setText("R$: "+decimalFormat.format(valorTotal));
        mudouDoces=true;

    }

    public void mudouQuantidadeRecyclerView(int qtdAntes, Doce doceAlterado){
        //Zerar
        valorTotal-=doceAlterado.getValor()*qtdAntes;
        //Adicinoar nova Qtd
        valorTotal+=doceAlterado.getValor()*doceAlterado.getQtd();
        textViewValorTotal.setText("R$: "+decimalFormat.format(valorTotal));
        adapterRecycleViewDocesEncomenda.notifyDataSetChanged();
        mudouDoces=true;

    }

    private void colocarDados() {
        nomeCliente.setText(encomenda.getCliente().getNome());
        telefoneCliente.setText(encomenda.getCliente().getTelefone());
        data.setText(encomenda.getData());
        hora.setText(encomenda.getHora());

        if(encomenda.getObs().equalsIgnoreCase("")){
            avisoObs.setVisibility(View.GONE);
        }

        for(Doce doce:encomenda.getDoces()){
            valorTotal+=doce.getQtd()*doce.getValor();
        }
        textViewValorTotal.setText("R$: "+decimalFormat.format(valorTotal));

        adapterRecycleViewDocesEncomenda = new AdapterRecycleViewDocesEncomenda(this, encomenda.getDoces());
        recyclerView.setAdapter(adapterRecycleViewDocesEncomenda);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        vendo();
    }

    private void setar() {
        nomeCliente = findViewById(R.id.editTextEncomendaNomeCliente);
        telefoneCliente = findViewById(R.id.editTextEncomendaTelefoneCliente);
        data = findViewById(R.id.editTextEncomendaData);
        hora = findViewById(R.id.editTextEncomendaHora);
        recyclerView = findViewById(R.id.recyclerViewEncomenda);
        floatingActionButton = findViewById(R.id.floatingActionButtonEncomenda);
        botaoSalvar = findViewById(R.id.botaoEncomendaSalvar);
        spinnerDoces = findViewById(R.id.spinnerEncomendaDoces);
        avisoObs = findViewById(R.id.imageViewAvisoObs);
        textViewValorTotal = findViewById(R.id.textViewEncomendaValorTotal);

        calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        cancelarEdicao = menu.findItem(R.id.menu_CancelarEdicao);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.encomenda_menu,menu);
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
            case R.id.menu_EditarEncomenda:
                editando();
                break;

            case R.id.menu_ExcluirEncomenda:
                AlertDialog.Builder builder = new AlertDialog.Builder(EncomendaActivity.this);
                builder.setMessage("Deletar a Encomenda de "+ encomenda.getCliente().getNome()+"?").setTitle("Confirmar Ação")
                        .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                banco.collection("Encomendas").document(idFirebase).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(EncomendaActivity.this,"Encomenda Deletada", Toast.LENGTH_SHORT).show();
                                                Intent i = new Intent(EncomendaActivity.this, MainActivity.class);
                                                startActivity(i);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(EncomendaActivity.this,"Erro ao Deletar", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).setIcon(R.drawable.ic_baseline_warning_24).show();

                break;



            case R.id.menu_Obs:
                abrirDialogoObs();
                break;

            case R.id.menu_CancelarEdicao:
                vendo();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void vendo() {
        editando = false;
        allocateActivityTitle("Encomenda");
        floatingActionButton.setVisibility(View.INVISIBLE);
        botaoSalvar.setVisibility(View.INVISIBLE);
        cancelarEdicao.setVisible(false);

        nomeCliente.setFocusable(false);
        telefoneCliente.setFocusable(false);

        if(encomenda.getObs().equalsIgnoreCase("")){
            avisoObs.setVisibility(View.INVISIBLE);
        }else{
            avisoObs.setVisibility(View.VISIBLE);
        }

    }

    private void editando() {
        editando = true;
        allocateActivityTitle("Editando Encomenda...");
        floatingActionButton.setVisibility(View.VISIBLE);
        botaoSalvar.setVisibility(View.VISIBLE);
        cancelarEdicao.setVisible(true);

        nomeCliente.setFocusableInTouchMode(true);
        telefoneCliente.setFocusableInTouchMode(true);
        avisoObs.setVisibility(View.VISIBLE);

    }

    private void abrirDialogoObs() {

        Dialog dialogoObs = new Dialog(EncomendaActivity.this);
        dialogoObs.setContentView(R.layout.dialogo_obs_encomenda);
        EditText obsDialogo = dialogoObs.findViewById(R.id.editTextTextMultiLineDialogoObsObservacoes);
        Button botaoDialogoOK = dialogoObs.findViewById(R.id.buttonDialogoObsOK);

        obsDialogo.setText(encomenda.getObs());

        dialogoObs.show();

        botaoDialogoOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!obsDialogo.getText().toString().equalsIgnoreCase(encomenda.getObs())&& editando){
                    encomenda.setObs(obsDialogo.getText().toString());
                    Toast.makeText(EncomendaActivity.this,"Observações Salvas!", Toast.LENGTH_SHORT).show();
                    if(encomenda.getObs().equalsIgnoreCase("")){
                        avisoObs.setVisibility(View.GONE);
                    }else{
                        avisoObs.setVisibility(View.VISIBLE);
                    }
                }
                dialogoObs.dismiss();
            }
        });

    }

    private void abrirCalendario() {
    int ano = calendar.get(Calendar.YEAR);
    int mes = calendar.get(Calendar.MONDAY);
    int dia = calendar.get(Calendar.DAY_OF_MONTH);

    DatePickerDialog datePickerDialog = new DatePickerDialog(EncomendaActivity.this,
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    String dataBR = String.format("%02d%02d%02d",day, month+1, year);
                    dataBR = String.format("%s/%s/%s", dataBR.substring(0, 2), dataBR.substring(2, 4), dataBR.substring(4, 8));
                    data.setText(dataBR);
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);

                    encomenda.setData(dataBR);
                    encomenda.setDataJava(calendar.getTime());
                    mudouDataHora=true;
                }
            }, ano, mes, dia);
    datePickerDialog.show();
}

    private void abrirRelogio(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(EncomendaActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int horasTimePicker, int minutosTimePicker) {
                hora.setText(String.format("%02d:%02d", horasTimePicker, minutosTimePicker));
                calendar.set(Calendar.HOUR_OF_DAY, horasTimePicker);
                calendar.set(Calendar.MINUTE, minutosTimePicker);
                calendar.set(Calendar.SECOND, 0);

                encomenda.setHora(hora.getText().toString());
                encomenda.setDataJava(calendar.getTime());
                mudouDataHora=true;

            }
        }, 0,0,true);
        timePickerDialog.show();
    }

    private boolean validarInputs(){

        if(nomeCliente.getText().toString().equals("")){
            avisoInputs = "Preencha o Nome";
            return false;
        }

        if(telefoneCliente.getText().toString().equals("")){
            avisoInputs = "Preencha o Telefone ";
            return false;
        }

        if(telefoneCliente.getText().toString().length()<16){
            avisoInputs = "Telefone Inválido ";
            return false;
        }
        if(data.getText().toString().equals("")){
            avisoInputs = "Preencha a Data";
            return false;
        }

        if(hora.getText().toString().equals("")){
            avisoInputs = "Preecha a Hora";
            return false;
        }

        if(encomenda.getDoces().size()<1){
            avisoInputs = "Encomenda Vazia";
            return false;
        }

        return true;
    }

    private boolean cadastroDuplo(Doce doceDuplo){
        for (Doce doce: encomenda.getDoces()){
            if(doce.getId() == doceDuplo.getId()){
                return true;
            }
        }
        return false;
    }

}