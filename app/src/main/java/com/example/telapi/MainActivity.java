package com.example.telapi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMeses;
    private ListView listViewDespesas;
    private DespesaAdapter despesaAdapter;
    private CollectionReference despesasRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton btnAdicionar = findViewById(R.id.btnAdicionar);
        btnAdicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, atv_cadastro.class);
                startActivity(intent);
            }
        });

        // Inicializar Firebase Firestore
        despesasRef = FirebaseFirestore.getInstance().collection("despesas");

        // Inicializar Spinner de Meses
        spinnerMeses = findViewById(R.id.spnMeses);
        ArrayAdapter<CharSequence> mesesAdapter = ArrayAdapter.createFromResource(this,
                R.array.meses, android.R.layout.simple_spinner_item);
        mesesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeses.setAdapter(mesesAdapter);

        // Inicializar ListView de Despesas
        listViewDespesas = findViewById(R.id.lstDespesas);
        despesaAdapter = new DespesaAdapter(this, new ArrayList<>());
        listViewDespesas.setAdapter(despesaAdapter);

        // Configurar Listener para o Spinner de Meses
        spinnerMeses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String mesSelecionado = parent.getItemAtPosition(position).toString();
                // Atualizar a lista de despesas de acordo com o mês selecionado
                atualizarListaDespesas(mesSelecionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nenhuma ação necessária se nada for selecionado
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualizar a lista de despesas quando a MainActivity é retomada
        if (despesaAdapter != null) {
            String mesSelecionado = spinnerMeses.getSelectedItem().toString();
            atualizarListaDespesas(mesSelecionado);
        }
    }

    private void atualizarListaDespesas(String mesSelecionado) {
        despesasRef.whereEqualTo("mes", mesSelecionado)
                .orderBy("data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Despesa> despesas = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Despesa despesa = document.toObject(Despesa.class);
                                despesas.add(despesa);
                            }

                            despesaAdapter.atualizarDespesas(despesas);
                        } else {

                            Toast.makeText(MainActivity.this, "Erro ao carregar despesas: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void adicionarDespesa(View view) {
        // Abrir a atividade de cadastro para adicionar uma nova despesa
        Intent intent = new Intent(this, atv_cadastro.class);
        startActivity(intent);
    }
}
