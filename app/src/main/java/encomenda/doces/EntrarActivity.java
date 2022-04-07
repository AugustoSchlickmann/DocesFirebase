package encomenda.doces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;

public class EntrarActivity extends AppCompatActivity {

    EditText email, senha;
    Button entrar;
    TextView naoSouCadastrado, esqueciSenha;
    String aviso;
    ProgressBar progressBar;
    public static String nomeUsuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        if(usuarioAtual!=null){
            //Ir pra tela Inicial
            FirebaseFirestore banco = FirebaseFirestore.getInstance();

            DocumentReference documentReference = banco.collection("Usuarios").document(usuarioAtual.getUid());

            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                    if(documentSnapshot!=null){
                        nomeUsuario = documentSnapshot.getString("nome");
                    }
                }
            });
            startActivity(new Intent(EntrarActivity.this, MainActivity.class));
        }else{

        }

        setTheme(R.style.Theme_Doces);
        setContentView(R.layout.activity_entrar);
        setar();

        naoSouCadastrado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EntrarActivity.this, CadastrarUsuarioActivity.class));
            }
        });

        esqueciSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(EntrarActivity.this);
                dialog.setContentView(R.layout.dialogo_enqueci_senha);

                EditText enderecoEmail = dialog.findViewById(R.id.testeInputEditTextDialogoEsqueciSenha);
                Button botaoEnviar = dialog.findViewById(R.id.buttonDialogoEsqueciSenha);

                dialog.show();

                botaoEnviar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(enderecoEmail.getText().toString().length()>0) {


                            FirebaseAuth.getInstance().sendPasswordResetEmail(enderecoEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EntrarActivity.this, "E-mail enviado", Toast.LENGTH_LONG).show();
                                    } else {
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthInvalidCredentialsException emailMalFormado) {
                                            Toast.makeText(EntrarActivity.this, "E-mail ou Senha Inválido", Toast.LENGTH_LONG).show();
                                        } catch (FirebaseAuthInvalidUserException emailNaoCadastrado) {
                                            Toast.makeText(EntrarActivity.this, "E-mail não cadastrado", Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            Log.i("CadastrarUsuario", "Erro = " + e.toString());
                                            Toast.makeText(EntrarActivity.this, "Erro Interno", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                            dialog.dismiss();
                        }else{
                            Toast.makeText(EntrarActivity.this, "Digite um e-mail", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validarDados()){

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(), senha.getText().toString()).
                            addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                //Progress Bar girando?
                                progressBar.setVisibility(View.VISIBLE);
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
                                                        startActivity(new Intent(EntrarActivity.this, MainActivity.class));
                                                    }
                                                },2000);
                                            }
                                        });

                            } else {
                                try {
                                    throw task.getException();
                                }catch (FirebaseAuthInvalidCredentialsException emailMalFormado){
                                        Toast.makeText(EntrarActivity.this, "E-mail ou Senha Inválido", Toast.LENGTH_LONG).show();
                                }catch (FirebaseAuthInvalidUserException usuarioInvalido) {
                                    Toast.makeText(EntrarActivity.this, "E-mail Inexistente", Toast.LENGTH_LONG).show();
                                    email.setError("Senha Fraca");
                                }catch (Exception e){
                                    Log.i("CadastrarUsuario", "Erro = " +e.toString());
                                    Toast.makeText(EntrarActivity.this, "Erro Interno", Toast.LENGTH_LONG).show();
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

    //Deixar o usuário logado sempre, a menos que ele clique em Sair
    @Override
    protected void onStart() {
        super.onStart();
    }

    private  void setar(){
        email = findViewById(R.id.testeInputEditTextEmailEntrar);
        senha = findViewById(R.id.testeInputEditTextSenhaEntrar);
        entrar = findViewById(R.id.botaoEntrar);
        naoSouCadastrado = findViewById(R.id.textViewNaoSouCadastrado);
        progressBar = findViewById(R.id.progressBarEntrar);
        esqueciSenha = findViewById(R.id.textViewEsqueciSenha);
    }

    private boolean validarDados(){

        if(email.getText().toString().equals("")){
            aviso = "Preencha o E-mail ";
            return false;
        }

        if(senha.getText().toString().equals("")){
            aviso = "Preencha a Senha";
            return false;
        }

        return true;
    }
}