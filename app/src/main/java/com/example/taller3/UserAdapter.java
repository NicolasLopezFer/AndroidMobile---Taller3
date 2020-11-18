package com.example.taller3;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taller3.Model.Usuario;

import java.util.List;

public class UserAdapter extends BaseAdapter {

    private Context context;
    private List<Usuario> usuarios;

    public UserAdapter (Context context, List<Usuario> usuarios){
        this.context = context;
        this.usuarios = usuarios;
    }

    @Override
    public int getCount() {
        return usuarios.size();
    }

    @Override
    public Object getItem(int i) {
        return usuarios.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = LayoutInflater.from(this.context).inflate(R.layout.activity_users_adapter, viewGroup, false);
        }

        ImageView imagenUsuario;
        TextView nombreUsuario;
        Button bottonUsuario;

        imagenUsuario = view.findViewById(R.id.imageViewUsuarioAdapter);
        nombreUsuario = view.findViewById(R.id.textViewUsuarioAdapter);
        bottonUsuario = view.findViewById(R.id.buttonUsuarioAdapter);

        //Asignar imagen

        nombreUsuario.setText(usuarios.get(i).getNombre());


        return view;
    }
}
