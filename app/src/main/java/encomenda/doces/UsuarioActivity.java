package encomenda.doces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import encomenda.doces.databinding.ActivityUsuarioBinding;
import encomenda.doces.modelos.Doce;
import encomenda.doces.modelos.Usuario;

public class UsuarioActivity extends DrawerBaseActivity {

    ActivityUsuarioBinding activityUsuarioBinding;
    EditText nome, email;
    Button sair, atualizar;
    boolean editando = false;
    MenuItem cancelarEdicao;
    FirebaseFirestore banco = FirebaseFirestore.getInstance();
    Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUsuarioBinding = ActivityUsuarioBinding.inflate(getLayoutInflater());

        setContentView(activityUsuarioBinding.getRoot());
        setar();


        sair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UsuarioActivity.this, EntrarActivity.class));
                finish();
            }
        });

        atualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!usuario.getNome().equals(nome.getText().toString())) {
                    DocumentReference docRefEncomenda = banco.collection("Usuarios").document(usuario.getId());
                    docRefEncomenda.update("nome", nome.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(UsuarioActivity.this, "Nome Atualizado", Toast.LENGTH_SHORT).show();
                            vendo();
                        }
                    });
                } else {
                    Toast.makeText(UsuarioActivity.this, "Não há o que atualizar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        usuario = new Usuario();
        usuario.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        usuario.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        DocumentReference documentReference = banco.collection("Usuarios").document(usuario.getId());

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if(documentSnapshot!=null){
                    usuario.setNome(documentSnapshot.getString("nome"));
                    nome.setText(usuario.getNome());
                    email.setText(usuario.getEmail());
                }
            }
        });
    }


    private void setar(){
        nome = findViewById(R.id.editTextUsuarioNome);
        email = findViewById(R.id.editTextUsuarioEmail);
        sair = findViewById(R.id.botaoSair);
        atualizar = findViewById(R.id.botaoAtualizarUsuario);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        cancelarEdicao = menu.findItem(R.id.menu_CancelarEdicaoUsuario);
        vendo();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.usuario_menu,menu);
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

            case R.id.menu_EditarUsuario:
               editando();
                break;

            case R.id.menu_MudarSenha:
                Dialog dialog = new Dialog(UsuarioActivity.this);
                dialog.setContentView(R.layout.dialogo_enqueci_senha);

                EditText enderecoEmail = dialog.findViewById(R.id.testeInputEditTextDialogoEsqueciSenha);
                Button botaoEnviar = dialog.findViewById(R.id.buttonDialogoEsqueciSenha);
                TextView titulo = dialog.findViewById(R.id.textViewDialogoEsqueciSenha);
                enderecoEmail.setVisibility(View.GONE);
                titulo.setText("Enviar E-mail de mudança de senha para "+usuario.getEmail()+"?");
                dialog.show();

                botaoEnviar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(usuario.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(UsuarioActivity.this, "E-mail enviado", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(UsuarioActivity.this, "Erro...", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
                break;

            case R.id.menu_ExcluirUsuario:
                AlertDialog.Builder builder = new AlertDialog.Builder(UsuarioActivity.this);
                builder.setMessage("Você tem certeza que deseja deletar sua conta?").setTitle("Confirmar Ação")
                        .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String iduser= usuario.getId();
                                FirebaseAuth.getInstance().getCurrentUser().delete();
                                FirebaseAuth.getInstance().signOut();
                                banco.collection("Usuarios").document(iduser).delete();
                                Toast.makeText(UsuarioActivity.this,"Conta Deletada", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(UsuarioActivity.this, EntrarActivity.class));
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).setIcon(R.drawable.ic_baseline_warning_24).show();
                break;

            case R.id.menu_CancelarEdicaoUsuario:
                vendo();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void vendo(){
        editando = false;
        allocateActivityTitle("Meu Perfil");
        cancelarEdicao.setVisible(false);
        sair.setVisibility(View.VISIBLE);
        atualizar.setVisibility(View.INVISIBLE);

        nome.setFocusable(false);
        email.setFocusable(false);

    }

    private void editando(){
        editando = true;
        allocateActivityTitle("Editando Meu Perfil");
        cancelarEdicao.setVisible(true);
        sair.setVisibility(View.GONE);
        atualizar.setVisibility(View.VISIBLE);

        nome.setFocusableInTouchMode(true);

    }
}