package com.example.ac2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText edtTitulo, edtAutor, edtAno, edtBusca;
    private Spinner spnGenero, spnStatus, spnFiltroGenero;
    private CheckBox chkFavorito, chkSoFavoritos;
    private Button btnSalvar;
    private RecyclerView recyclerLivros;
    private List<Livro> listaLivros = new ArrayList<>();
    private List<Livro> listaFiltrada = new ArrayList<>();
    private LivroAdapter adapter;
    private Livro livroEditando = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        edtTitulo = findViewById(R.id.edtTitulo);
        edtAutor = findViewById(R.id.edtAutor);
        edtAno = findViewById(R.id.edtAno);
        edtBusca = findViewById(R.id.edtBusca);
        spnGenero = findViewById(R.id.spnGenero);
        spnStatus = findViewById(R.id.spnStatus);
        spnFiltroGenero = findViewById(R.id.spnFiltroGenero);
        chkFavorito = findViewById(R.id.chkFavorito);
        chkSoFavoritos = findViewById(R.id.chkSoFavoritos);
        btnSalvar = findViewById(R.id.btnSalvar);
        recyclerLivros = findViewById(R.id.recyclerLivros);

        recyclerLivros.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LivroAdapter(listaFiltrada);
        recyclerLivros.setAdapter(adapter);

        adapter.setOnItemClickListener(livro -> carregarParaEdicao(livro));
        adapter.setOnItemLongClickListener(livro -> confirmarExclusao(livro));

        btnSalvar.setOnClickListener(v -> salvarLivro());
        findViewById(R.id.btnFiltrar).setOnClickListener(v -> aplicarFiltros());
        chkSoFavoritos.setOnCheckedChangeListener((buttonView, isChecked) -> aplicarFiltros());

        carregarLivros();
    }

    private void carregarLivros() {
        db.collection("livros")
                .get()
                .addOnSuccessListener(query -> {
                    listaLivros.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Livro l = doc.toObject(Livro.class);
                        l.setId(doc.getId());
                        listaLivros.add(l);
                    }
                    aplicarFiltros();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void aplicarFiltros() {
        String generoFiltro = spnFiltroGenero.getSelectedItem().toString();
        boolean soFavoritos = chkSoFavoritos.isChecked();
        String busca = edtBusca.getText().toString().trim().toLowerCase();

        listaFiltrada.clear();
        for (Livro l : listaLivros) {
            if (!generoFiltro.equals("Todos") && !generoFiltro.equals(l.getGenero())) {
                continue;
            }
            if (soFavoritos && !l.isFavorito()) {
                continue;
            }
            if (!busca.isEmpty() && (l.getTitulo() == null || !l.getTitulo().toLowerCase().contains(busca))) {
                continue;
            }
            listaFiltrada.add(l);
        }
        adapter.notifyDataSetChanged();
    }

    private boolean validarCampos() {
        if (edtTitulo.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Informe o título!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtAutor.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Informe o autor!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spnGenero.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o gênero!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtAno.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Informe o ano de publicação!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spnStatus.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o status de leitura!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void salvarLivro() {
        if (!validarCampos()) {
            return;
        }

        String titulo = edtTitulo.getText().toString().trim();
        String autor = edtAutor.getText().toString().trim();
        String genero = spnGenero.getSelectedItem().toString();
        int ano = Integer.parseInt(edtAno.getText().toString().trim());
        String status = spnStatus.getSelectedItem().toString();
        boolean favorito = chkFavorito.isChecked();

        if (livroEditando == null) {
            Livro livro = new Livro(null, titulo, autor, genero, ano, status, favorito);
            db.collection("livros")
                    .add(livro)
                    .addOnSuccessListener(doc -> {
                        livro.setId(doc.getId());
                        Toast.makeText(this, "Livro salvo!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarLivros();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            livroEditando.setTitulo(titulo);
            livroEditando.setAutor(autor);
            livroEditando.setGenero(genero);
            livroEditando.setAno(ano);
            livroEditando.setStatus(status);
            livroEditando.setFavorito(favorito);

            db.collection("livros").document(livroEditando.getId())
                    .set(livroEditando)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Livro atualizado!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarLivros();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void carregarParaEdicao(Livro livro) {
        edtTitulo.setText(livro.getTitulo());
        edtAutor.setText(livro.getAutor());
        selecionarNoSpinner(spnGenero, R.array.generos, livro.getGenero());
        edtAno.setText(String.valueOf(livro.getAno()));
        selecionarNoSpinner(spnStatus, R.array.status, livro.getStatus());
        chkFavorito.setChecked(livro.isFavorito());
        livroEditando = livro;
        btnSalvar.setText("Atualizar");
    }

    private void confirmarExclusao(Livro livro) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir livro")
                .setMessage("Deseja excluir \"" + livro.getTitulo() + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> excluirLivro(livro))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirLivro(Livro livro) {
        db.collection("livros").document(livro.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, livro.getTitulo() + " excluído!", Toast.LENGTH_SHORT).show();
                    if (livroEditando != null && livroEditando.getId().equals(livro.getId())) {
                        limparCampos();
                    }
                    carregarLivros();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void selecionarNoSpinner(Spinner spinner, int arrayId, String valor) {
        String[] opcoes = getResources().getStringArray(arrayId);
        for (int i = 0; i < opcoes.length; i++) {
            if (opcoes[i].equals(valor)) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0);
    }

    private void limparCampos() {
        edtTitulo.setText("");
        edtAutor.setText("");
        spnGenero.setSelection(0);
        edtAno.setText("");
        spnStatus.setSelection(0);
        chkFavorito.setChecked(false);
        livroEditando = null;
        btnSalvar.setText("Salvar");
    }
}
