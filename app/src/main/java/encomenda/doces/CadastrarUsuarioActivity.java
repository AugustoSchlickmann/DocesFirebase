package encomenda.doces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class CadastrarUsuarioActivity extends AppCompatActivity {

    EditText nome, email, senha, confirmarSenha;
    Button cadastrar;
    TextView jaSouCadastrado;
    String aviso;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cadastrar);

        setar();

        jaSouCadastrado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CadastrarUsuarioActivity.this, EntrarActivity.class));
            }
        });

        cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validarDados()){
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(), senha.getText().toString()).
                            addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    //Salvar os dados do usuario no FireBase
                                    salvarDadosUsuarioFireStore();
                                } else {
                                    try {
                                        throw task.getException();
                                    }catch (FirebaseAuthWeakPasswordException senhaFraca) {
                                        Toast.makeText(CadastrarUsuarioActivity.this, "Senha Fraca", Toast.LENGTH_LONG).show();
                                        senha.setError("Senha Fraca");
                                    }catch (FirebaseAuthUserCollisionException emailJaExiste){
                                        Toast.makeText(CadastrarUsuarioActivity.this, "E-mail Em Uso", Toast.LENGTH_LONG).show();
                                        email.setError("E-mail em uso");
                                    }catch (FirebaseAuthInvalidCredentialsException emailMalFormado){
                                        Toast.makeText(CadastrarUsuarioActivity.this, "E-mail Inválido", Toast.LENGTH_LONG).show();
                                        email.setError("E-mail Inválido");
                                    }catch (Exception e){
                                        Log.i("CadastrarUsuario", "Erro = " + e.getMessage()+"\n"+e.getStackTrace());
                                        Toast.makeText(CadastrarUsuarioActivity.this, "Erro Interno", Toast.LENGTH_LONG).show();
                                    }
                                }
                        }
                    });

                }else{
                    Snackbar snackbar = Snackbar.make(view, aviso, Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(getResources().getColor(R.color.rosinha));
                    snackbar.show();
                }

            }
        });

    }

    private  void setar(){
        nome = findViewById(R.id.testeInputEditTextNomeCadastrar);
        email = findViewById(R.id.testeInputEditTextEmailCadastrar);
        senha = findViewById(R.id.testeInputEditTextSenhaCadastrar);
        confirmarSenha = findViewById(R.id.testeInputEditTextConfirmarSenhaCadastrar);
        cadastrar = findViewById(R.id.botaoCadastrar);
        jaSouCadastrado = findViewById(R.id.textViewJaSouCadastrado);
        progressBar = findViewById(R.id.progressBarCadastrarUsuario);
    }

    private boolean validarDados(){
        if(nome.getText().toString().equals("")){
            aviso = "Preencha o Nome";
            return false;
        }
        if(nome.getText().toString().length()<3){
            aviso = "Nome Muito Curto";
            return false;
        }

        if(email.getText().toString().equals("")){
            aviso = "Preencha o E-mail ";
            return false;
        }

        if(senha.getText().toString().equals("")){
            aviso = "Preencha a Senha";
            return false;
        }

        if(confirmarSenha.getText().toString().equals("")){
            aviso = "Confirme a Senha";
            return false;
        }
        if(!senha.getText().toString().equals(confirmarSenha.getText().toString())){
            aviso = "Senhas Diferentes";
            return false;
        }

        return true;
    }

    private void salvarDadosUsuarioFireStore(){

        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore banco = FirebaseFirestore.getInstance();
        Map<String ,Object> usuarios = new HashMap<>();
        usuarios.put("nome", nome.getText().toString());
        String usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = banco.collection("Usuarios").document(usuarioID);

        documentReference.set(usuarios).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                FirebaseMessaging.getInstance().subscribeToTopic("todos")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i("CadastroUsuario", "Se inscreveu!");
                        }else{
                            Log.i("CadastroUsuario", "Erro se inscrevendo");
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CadastrarUsuarioActivity.this, "Cadastrado Com Sucesso!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(CadastrarUsuarioActivity.this, MainActivity.class));
                            }
                        },2000);
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("FireStore", "Erro salvando nome do ususario\n"+e.toString());
            }
        });

    }
}