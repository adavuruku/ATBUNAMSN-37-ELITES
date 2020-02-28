package com.example.atbunamsn;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.example.atbunamsn.R.mipmap.ic_phone;

/**
 * Created by sherif146 on 11/01/2018.
 */

public class phoneAdapter extends RecyclerView.Adapter<phoneAdapter.RecyclerHolder>{
    private LayoutInflater inflater;
    private List<phoneList> phonelist;
    private RecyclerView recyclerView;
  private  OnItemClickListener mlistener;
  //  private RecyclerItemClickListener listener;
    private Context activity;

    public phoneAdapter(List<phoneList> contacts, Context context,OnItemClickListener listener){
        this.activity = context;
       // this.recyclerView = recyclerView;
        this.mlistener = listener;
        this.inflater = LayoutInflater.from(context);
        this.phonelist = contacts;
    }
    public interface OnItemClickListener{
        void onMessageClick(View v,int position);
        void onCallClick(View v, int position);
    }
    public void setOnitemClickListener(OnItemClickListener listener){mlistener = listener;}
    @Override
    public phoneAdapter.RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.my_row_layout), parent, false), mlistener);

        View view = inflater.inflate(R.layout.phone_row,parent,false);
        phoneAdapter.RecyclerHolder holder= new phoneAdapter.RecyclerHolder(view,mlistener);
        return holder;
    }
    @Override
    public void onBindViewHolder(phoneAdapter.RecyclerHolder holder, int position) {
        phoneList contact = phonelist.get(position);
        holder.phone.setText((contact.getPhone()));
        holder.phonebutton.setImageResource(R.drawable.phone);
        holder.msgbutton.setImageResource(R.drawable.msg);

    }

    @Override
    public int getItemCount() {
        return phonelist.size();
    }

    //create the holder class
    class RecyclerHolder extends RecyclerView.ViewHolder {
        TextView phone;
       ImageButton phonebutton,msgbutton;
        public RecyclerHolder(final View itemView,final OnItemClickListener listener) {
            super(itemView);
            phone = (TextView) itemView.findViewById(R.id.phoneitem);
            phonebutton = (ImageButton) itemView.findViewById(R.id.phonebut);
            msgbutton = (ImageButton) itemView.findViewById(R.id.msgbut);

            phonebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onCallClick(view, position);
                        }
                    }
                }
            });

            msgbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onMessageClick(view, position);
                        }
                    }
                }
            });
        }
    }
}