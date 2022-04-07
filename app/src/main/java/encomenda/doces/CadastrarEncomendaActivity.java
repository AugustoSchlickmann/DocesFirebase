package encomenda.doces;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;


import java.text.DecimalFormat;
import java.util.Calendar;

import java.util.HashMap;
import java.util.TimeZone;

import encomenda.doces.databinding.ActivityCadastrarEncomendaBinding;
import encomenda.doces.modelos.Cliente;
import encomenda.doces.modelos.Doce;
import encomenda.doces.modelos.Encomenda;
import encomenda.doces.modelos.Usuario;

public class CadastrarEncomendaActivity extends DrawerBaseActivity {

    ActivityCadastrarEncomendaBinding activityCadastrarEncomendaBinding;
    EditText nomeCliente, telefoneCliente, data, hora;
    TextView  textViewValorTotal;
    RecyclerView recyclerView;
    FloatingActionButton floatingActionButton;
    Button botaoSalvar;
    ImageView avisoObs;
    Spinner spinnerDoces;

    AdapterSpinnerDoces adapterSpinnerDoces;
    AdapterRecycleViewDocesEncomenda adapterRecycleViewDocesEncomenda;

    DataBase dataBase;
    Calendar calendar;
    Encomenda encomenda;

    String ultimoCaracterDigitado = "";

    FirebaseFirestore banco = FirebaseFirestore.getInstance();
    String avisoInputs="";
    double valorTotal;
    DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCadastrarEncomendaBinding = ActivityCadastrarEncomendaBinding.inflate(getLayoutInflater());

        setContentView(activityCadastrarEncomendaBinding.getRoot());
        allocateActivityTitle("Cadastrar Encomenda");

        setar();

        adapterSpinnerDoces = new AdapterSpinnerDoces(CadastrarEncomendaActivity.this, dataBase.pegarTodosDoces());
        spinnerDoces.setAdapter(adapterSpinnerDoces);

        adapterRecycleViewDocesEncomenda = new AdapterRecycleViewDocesEncomenda(this,encomenda.getDoces());
        recyclerView.setAdapter(adapterRecycleViewDocesEncomenda);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //ADICIONAR DOCE NA LISTA
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(adapterSpinnerDoces.getCount()>0){


                    Doce doceAdicionadoNaLista = adapterSpinnerDoces.getDoces().get(spinnerDoces.getSelectedItemPosition());
                    Dialog dialog = new Dialog(CadastrarEncomendaActivity.this);
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
                                    Toast.makeText(CadastrarEncomendaActivity.this,"Doce já está na lista", Toast.LENGTH_SHORT).show();
                                }else{
                                    doceAdicionadoNaLista.setQtd(Integer.parseInt(qtd.getText().toString()));
                                    adicionarNaRecyclerView(doceAdicionadoNaLista);
                                }
                                dialog.dismiss();
                            }catch (Exception e ){
                                Toast.makeText(CadastrarEncomendaActivity.this,"Quantidade Inválida", Toast.LENGTH_SHORT).show();

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
                       encomenda.setCliente(new Cliente(
                               nomeCliente.getText().toString(),
                               telefoneCliente.getText().toString(),
                               (nomeCliente.getText().toString()+"_"+telefoneCliente.getText().toString())));


                       DocumentReference documentCliente = banco.collection("Clientes").document(encomenda.getCliente().getNome_telefone());
                       //Cadastrar o cliente no banco ou atualizar
                       documentCliente.set(encomenda.getCliente(), SetOptions.merge());

                       banco.collection("Encomendas").add(encomenda).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                           @Override
                           public void onSuccess(DocumentReference documentReference) {
                              //Pegar o nome do usuario que cadastrou e botar na notificação
                               FcmNotificationSender fcmNotificationSender = new FcmNotificationSender(
                                       "/topics/todos",
                                       EntrarActivity.nomeUsuario+" cadastrou uma nova encomenda",
                                       "Data:"+encomenda.getData()+"\nHora:"+encomenda.getHora(),
                                       getApplicationContext(),
                                       CadastrarEncomendaActivity.this);
                               fcmNotificationSender.sendNotifications();

                               encomenda.setIdFirebase(documentReference.getId());
                               Toast.makeText(CadastrarEncomendaActivity.this,"Encomenda Salva!", Toast.LENGTH_SHORT).show();
                               Intent intent = new Intent(CadastrarEncomendaActivity.this, MainActivity.class);
                               CadastrarEncomendaActivity.this.startActivity(intent);

                               //Mandar mensagem para todos os usuarios!
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Toast.makeText(CadastrarEncomendaActivity.this,"Erro na Base de Dados", Toast.LENGTH_SHORT).show();
                           }
                       });

                   }catch (Exception e){
                       Log.i("CadastroEncomenda","Erro = " + e.toString());
                   }
               }else{
                   Toast.makeText(CadastrarEncomendaActivity.this,avisoInputs, Toast.LENGTH_SHORT).show();
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
                    //Colocar PRIMEIRO espaço  PAÍS
                    if(!ultimoCaracterDigitado.equals(" ")){
                        telefoneCliente.append(" ");
                    }else{
                        telefoneCliente.getText().delete(tamanhoTelefone-1, tamanhoTelefone);
                    }
                }else if(tamanhoTelefone == 5) {
                    //Colocar SEGUNDO espaço   DDD
                    if (!ultimoCaracterDigitado.equals(" ")) {
                        telefoneCliente.append(" ");
                    } else {
                        telefoneCliente.getText().delete(tamanhoTelefone - 1, tamanhoTelefone);
                    }
                }else if(tamanhoTelefone == 11){
                    //Colocar Hífen
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

    }

    public void removeuDaRecyclerView(Doce doceRemovido){
        encomenda.getArrayIdDoces().remove(doceRemovido.getId());
        encomenda.getDoces().remove(doceRemovido);
        adapterRecycleViewDocesEncomenda.notifyDataSetChanged();
        valorTotal -= doceRemovido.getValor() * doceRemovido.getQtd();
        textViewValorTotal.setText("R$: "+decimalFormat.format(valorTotal));

    }

    public void mudouQuantidadeRecyclerView(int qtdAntes, Doce doceAlterado){
        Log.i("CadastroEncomenda","Mudando Qtd do Doce = " + doceAlterado.getNome()+"  Qtd Antes = "+qtdAntes+
                "   Qtd Depois = "+doceAlterado.getQtd()+"   Tamanho lista doces encomenda = "+encomenda.getDoces().size());
        //Zerar
        valorTotal-=doceAlterado.getValor()*qtdAntes;
        //Adicinoar nova Qtd
        valorTotal+=doceAlterado.getValor()*doceAlterado.getQtd();
        textViewValorTotal.setText("R$: "+decimalFormat.format(valorTotal));
        adapterRecycleViewDocesEncomenda.notifyDataSetChanged();

    }


    private  void setar(){
        nomeCliente = findViewById(R.id.editTextCadastrarEncomendaNomeCliente);
        telefoneCliente = findViewById(R.id.editTextCadastrarEncomendaTelefoneCliente);
        data = findViewById(R.id.editTextCadastrarEncomendaData);
        hora = findViewById(R.id.editTextCadastrarEncomendaHora);
        recyclerView = findViewById(R.id.recyclerViewCadastrarEncomenda);
        floatingActionButton = findViewById(R.id.floatingActionButtonCadastrarEncomenda);
        botaoSalvar = findViewById(R.id.botaoCadastrarEncomendaSalvar);
        spinnerDoces = findViewById(R.id.spinnerCadastrarEncomendaDoces);
        avisoObs = findViewById(R.id.imageViewCadastrarEncomendaAvisoObs);

        encomenda = new Encomenda();
        dataBase = new DataBase(this);
        textViewValorTotal = findViewById(R.id.textViewCadastrarEncomendaValorTotal);
        textViewValorTotal.setText("R$: "+valorTotal);
        calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));

       Cliente cliente = getIntent().getParcelableExtra("cliente");
        if(cliente!=null){
            nomeCliente.setText(cliente.getNome());
            telefoneCliente.setText(cliente.getTelefone());
        }

    }


    private void abrirDialogoObs() {

        Dialog dialogoObs = new Dialog(CadastrarEncomendaActivity.this);
        dialogoObs.setContentView(R.layout.dialogo_obs_encomenda);
        EditText obsDialogo = dialogoObs.findViewById(R.id.editTextTextMultiLineDialogoObsObservacoes);
        Button botaoDialogoOK = dialogoObs.findViewById(R.id.buttonDialogoObsOK);

        obsDialogo.setText(encomenda.getObs());

        dialogoObs.show();

        botaoDialogoOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    encomenda.setObs(obsDialogo.getText().toString());
                    Toast.makeText(CadastrarEncomendaActivity.this,"Observações Salvas!", Toast.LENGTH_SHORT).show();
                    dialogoObs.dismiss();
            }
        });

    }

    private void abrirCalendario() {
       int ano = calendar.get(Calendar.YEAR);
       int mes = calendar.get(Calendar.MONDAY);
       int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(CadastrarEncomendaActivity.this,
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

                    }
                }, ano, mes, dia);
        datePickerDialog.show();
    }

    private void abrirRelogio(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(CadastrarEncomendaActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int horasTimePicker, int minutosTimePicker) {
                hora.setText(String.format("%02d:%02d", horasTimePicker, minutosTimePicker));
                calendar.set(Calendar.HOUR_OF_DAY, horasTimePicker);
                calendar.set(Calendar.MINUTE, minutosTimePicker);
                calendar.set(Calendar.SECOND, 0);

                encomenda.setHora(hora.getText().toString());
                encomenda.setDataJava(calendar.getTime());

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
                //Cadastro Duplo! remover e adicionar de novo = mais fácil, na vdd as duas listas de doces compartilham os mesmos Doces, se muda de uma lista muda das duas!
                //Desisto, simplesmente nao irei adicionar!
                return true;
            }
        }
        return false;
    }
}