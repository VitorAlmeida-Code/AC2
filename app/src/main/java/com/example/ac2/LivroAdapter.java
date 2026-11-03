package com.example.ac2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LivroAdapter extends RecyclerView.Adapter<LivroAdapter.LivroViewHolder> {

    private List<Livro> listaLivros;
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;

    public interface OnItemClickListener {
        void onItemClick(Livro livro);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Livro livro);
    }

    public LivroAdapter(List<Livro> listaLivros) {
        this.listaLivros = listaLivros;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }

    @NonNull
    @Override
    public LivroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_livro, parent, false);
        return new LivroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LivroViewHolder holder, int position) {
        Livro livro = listaLivros.get(position);

        holder.txtTitulo.setText(livro.getTitulo());
        holder.txtAutor.setText("Autor: " + livro.getAutor());
        holder.txtGeneroAno.setText(livro.getGenero() + " - " + livro.getAno());
        String favorito = livro.isFavorito() ? " | Favorito" : "";
        holder.txtStatus.setText("Status: " + livro.getStatus() + favorito);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(livro);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                longListener.onItemLongClick(livro);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaLivros.size();
    }

    static class LivroViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtAutor, txtGeneroAno, txtStatus;

        LivroViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtAutor = itemView.findViewById(R.id.txtAutor);
            txtGeneroAno = itemView.findViewById(R.id.txtGeneroAno);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}
