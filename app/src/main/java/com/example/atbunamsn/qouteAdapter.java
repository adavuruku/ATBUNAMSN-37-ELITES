package com.example.atbunamsn;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sherif146 on 01/01/2018.
 */

public class qouteAdapter extends RecyclerView.Adapter<qouteAdapter.RecyclerHolder>{
    private LayoutInflater inflater;
    private List<Product> contacts;
    private String stat;
    private Context activity;
    private  OnItemClickListener mlistener;
    public qouteAdapter(List<Product> contacts, Context context, OnItemClickListener listener){
        this.activity = context;
        this.inflater = LayoutInflater.from(context);
        this.mlistener = listener;
        this.contacts = contacts;
    }
    public interface OnItemClickListener{
        void onNameClick(View v,int position);
    }
    public void setOnitemClickListener(OnItemClickListener listener){mlistener = listener;}

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View view = inflater.inflate(R.layout.custom_qoute_row,parent,false);
        RecyclerHolder holder= new RecyclerHolder(view,mlistener);
        return holder;
    }
    int prevpos=0;
    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        try {
            int g = holder.getAdapterPosition();
            Product contact = contacts.get(g);
            holder.Qouteby.setText(contact.getName());
            holder.theQoute.setText(contact.getEmail());
            if (position > prevpos) {
                //you are scrooling down
                AnimationUtils.animate(holder, true);
            } else {
                //no you are scroolingup
                AnimationUtils.animate(holder, false);
            }
            prevpos = position;
        }catch (Exception e){

        }

    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    //create the holder class
    class RecyclerHolder extends RecyclerView.ViewHolder{
        //the view items send here is from custom_row and is received here as itemView
        TextView theQoute, Qouteby;
        public RecyclerHolder(View itemView,final OnItemClickListener listener) {
            super(itemView);
            Qouteby = (TextView) itemView.findViewById(R.id.qouteBy);
            theQoute = (TextView) itemView.findViewById(R.id.theQoute);

            Qouteby.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onNameClick(view, position);
                        }
                    }
                }
            });

        }
    }

    public void setFilter(ArrayList<Product> newList){
        contacts = new ArrayList<>();
        contacts.addAll(newList);
        notifyDataSetChanged();
    }
}
