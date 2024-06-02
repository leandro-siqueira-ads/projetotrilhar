package com.example.telapi;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class atv_cadastro extends AppCompatActivity implements View.OnClickListener {

    private AutoCompleteTextView autoCompleteCategoria;
    private EditText edtDescricao, edtValor, edtVencimento;
    private Button btnGravar, btnExcluir;
    private Despesa despesa;
    private String acao;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atv_cadastro);

        autoCompleteCategoria = findViewById(R.id.autoCompleteCategoria);
        edtDescricao = findViewById(R.id.edtDescricao);
        edtValor = findViewById(R.id.edtValor);
        edtVencimento = findViewById(R.id.edtVencimento);
        btnGravar = findViewById(R.id.btnGravar);
        btnExcluir = findViewById(R.id.btnExcluir);
        db = FirebaseFirestore.getInstance();

        ImageView imgCalendario = findViewById(R.id.imgCalendario);
        imgCalendario.setOnClickListener(v -> abrirCalendario());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{});
        autoCompleteCategoria.setAdapter(adapter);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            acao = extras.getString("acao");
            despesa = (Despesa) extras.getSerializable("obj");
        } else {
            acao = "Inserir";
            despesa = null;
        }

        btnGravar.setText(acao);

        if ("Alterar".equals(acao) && despesa != null) {
            preencherCamposDespesa();
        } else {
            edtValor.setText("R$0,00");
        }

        btnGravar.setOnClickListener(v -> {
            Despesa despesaAtualizada = criarDespesa();
            if (despesaAtualizada != null) {
                DespesaCRUD despesaCRUD = new DespesaCRUD();
                if ("Inserir".equals(acao)) {
                    despesaCRUD.adicionarDespesa(despesaAtualizada);
                    Toast.makeText(atv_cadastro.this, "Despesa adicionada com sucesso: " + despesaAtualizada.toString(), Toast.LENGTH_SHORT).show();
                } else if ("Alterar".equals(acao)) {
                    despesaCRUD.alterarDespesa(despesaAtualizada);
                    Toast.makeText(atv_cadastro.this, "Despesa atualizada com sucesso: " + despesaAtualizada.toString(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("despesa_atualizada", despesaAtualizada);
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });

        btnExcluir.setOnClickListener(v -> {
            if (despesa != null) {
                DespesaCRUD despesaCRUD = new DespesaCRUD();
                despesaCRUD.removerDespesa(despesa.getId());
            }
            finish();
        });

        edtVencimento.setOnClickListener(v -> abrirCalendario());

        FloatingActionButton btnAddCategoria = findViewById(R.id.btnAddCategoria);
        btnAddCategoria.setOnClickListener(v -> abrirModalCategoria());

        configurarTecladoNumerico();
        getCategorias();
    }


    private Despesa criarDespesa() {
        String categoria = autoCompleteCategoria.getText().toString();
        String descricao = edtDescricao.getText().toString();
        double valor = extrairValorDigitado(edtValor.getText().toString());
        String dataVencimentoStr = edtVencimento.getText().toString();


        if (categoria.isEmpty() || descricao.isEmpty() || valor <= 0 || dataVencimentoStr.isEmpty()) {
            // Exibir mensagem de erro para o usuário
            Toast.makeText(this, "Por favor, preencha todos os campos corretamente.", Toast.LENGTH_SHORT).show();
            return null;
        }
        Log.d(TAG, "Categoria: " + categoria);
        Log.d(TAG, "Descrição: " + descricao);
        Log.d(TAG, "Valor: " + valor);
        Log.d(TAG, "Data de Vencimento: " + dataVencimentoStr);

        Timestamp vencimento;
        try {
            vencimento = new Timestamp(parseData(dataVencimentoStr));
        } catch (Exception e) {
            // Exibir mensagem de erro para o usuário
            Toast.makeText(this, "Formato de data inválido.", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Criar nova despesa com os dados coletados
        return new Despesa(categoria, descricao, valor, vencimento);


    }


    private void configurarTecladoNumerico() {
        findViewById(R.id.button0).setOnClickListener(this);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
        findViewById(R.id.button7).setOnClickListener(this);
        findViewById(R.id.button8).setOnClickListener(this);
        findViewById(R.id.button9).setOnClickListener(this);

        findViewById(R.id.buttonErase).setOnClickListener(v -> apagarUltimoCaractere());
    }

    private void apagarUltimoCaractere() {
        String currentValue = edtValor.getText().toString();
        if (!currentValue.isEmpty()) {
            currentValue = currentValue.substring(0, currentValue.length() - 1);
        }
        edtValor.setText(currentValue);
    }

    private void onNumberClick(View v) {
        TextView button = (TextView) v;
        String buttonText = button.getText().toString();
        String currentValue = edtValor.getText().toString().replace("R$", "").replace(",", "");

        if (currentValue.equals("0")) {
            currentValue = buttonText;
        } else {
            currentValue = currentValue.replaceAll("^0+(?!$)", "");
            currentValue += buttonText;
        }

        StringBuilder formattedValue = new StringBuilder(currentValue);
        if (formattedValue.length() >= 3) {
            formattedValue.insert(formattedValue.length() - 2, ",");
        } else {
            formattedValue.insert(0, "0,");
        }

        edtValor.setText("R$" + formattedValue.toString());
    }

    @Override
    public void onClick(View v) {
        onNumberClick(v);
    }

    private void abrirCalendario() {
        final Calendar calendario = Calendar.getInstance();
        int ano = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        calendario datePickerDialog = new calendario(this, (view, year, monthOfYear, dayOfMonth) -> {
            String dataSelecionada = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);
            edtVencimento.setText(dataSelecionada);
        }, ano, mes, dia);

        datePickerDialog.show();
    }

    private void preencherCamposDespesa() {
        autoCompleteCategoria.setText(despesa.getCategoria());
        edtDescricao.setText(despesa.getDescricao());
        edtValor.setText(String.valueOf(despesa.getValor()));
        edtVencimento.setText(formatarData(despesa.getVencimento().toDate()));
    }



    private String formatarData(java.util.Date data) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(data);
    }

    private java.util.Date parseData(String dataString) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        try {
            return sdf.parse(dataString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private double extrairValorDigitado(String valorDigitado) {
        if (valorDigitado.isEmpty()) return 0.0;

        return Double.parseDouble(valorDigitado.replace("R$", "").replace(",", "."));
    }

    private void abrirModalCategoria() {
        modal_categoria modal = new modal_categoria();

        modal.setCategoriaDialogListener(new modal_categoria.CategoriaDialogListener() {
            @Override
            public void onCategoriaAdicionada(String categoria) {
                autoCompleteCategoria.setText(categoria);
            }

            @Override
            public void onCategoriaRemovida(String categoriaId) {
                CategoriaCRUD categoriaCRUD = new CategoriaCRUD();
                categoriaCRUD.removerCategoria(categoriaId);
            }
        });

        modal.show(getSupportFragmentManager(), "modal_categoria");
    }

    private void getCategorias() {
        CollectionReference categoriasRef = db.collection("categorias");

        categoriasRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> categorias = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    String nomeCategoria = document.getString("nome");
                    categorias.add(nomeCategoria);
                }

                String[] categoriasArray = categorias.toArray(new String[0]);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(atv_cadastro.this, android.R.layout.simple_dropdown_item_1line, categoriasArray);
                autoCompleteCategoria.setAdapter(adapter);
            } else {
                Log.e("getCategorias", "Erro ao buscar categorias", task.getException());
            }
        });
    }
}

