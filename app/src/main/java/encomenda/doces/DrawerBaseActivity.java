package encomenda.doces;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class DrawerBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout)getLayoutInflater().inflate(R.layout.activity_drawer_base, null);
        FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);

        super.setContentView(drawerLayout);

        Toolbar toolbar = drawerLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar, R.string.menu_drawer_open, R.string.menu_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.nav_telaInicial:
                startActivity(new Intent(this, MainActivity.class));
                //smooth transition??
                overridePendingTransition(0,0);
                break;

            case R.id.nav_cadastrarEncomenda:
                startActivity(new Intent(this, CadastrarEncomendaActivity.class));
                overridePendingTransition(0,0);
                break;

            case R.id.nav_docesAfazer:
                Intent intentDocesaFazer = new Intent(this, ListaDocesActivity.class);
                intentDocesaFazer.putExtra("tipo" , 1);
                this.startActivity(intentDocesaFazer);
                overridePendingTransition(0,0);
                break;




            case R.id.nav_clientes:
                startActivity(new Intent(this, ListaClientesActivity.class));
                overridePendingTransition(0,0);
                break;

            case R.id.nav_doces:
                Intent intentDoces = new Intent(this, ListaDocesActivity.class);
                intentDoces.putExtra("tipo" , 0);
                this.startActivity(intentDoces);
                overridePendingTransition(0,0);
                break;

            case R.id.nav_usuario:
                startActivity(new Intent(this, UsuarioActivity.class));
                overridePendingTransition(0,0);
                break;




            case R.id.nav_listaPorData:
                Intent intent1 = new Intent(this, ListaEncomendasActivity.class);
                intent1.putExtra("tipo" , 1);
                this.startActivity(intent1);
                overridePendingTransition(0,0);
                break;


            case R.id.nav_listaPorCliente:
                Intent intent4 = new Intent(this, ListaEncomendasActivity.class);
                intent4.putExtra("tipo" ,2 );
                this.startActivity(intent4);
                overridePendingTransition(0,0);
                break;

        }

        return false;
    }

    protected  void allocateActivityTitle(String title){
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(title);
        }
    }

    protected  void allocateActivityTitleLogo(String title, int logo){
        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setLogo(logo);
        }
    }

}