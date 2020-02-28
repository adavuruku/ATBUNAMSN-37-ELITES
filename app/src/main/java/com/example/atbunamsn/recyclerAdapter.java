package com.example.atbunamsn;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sherif146 on 01/01/2018.
 */

public class recyclerAdapter extends RecyclerView.Adapter<recyclerAdapter.RecyclerHolder>{
    private LayoutInflater inflater;
    private List<Product> contacts;
    private String stat;
    private Activity activity;
    public recyclerAdapter(RecyclerView recyclerView, List<Product> contacts, Activity context,String stat){
        this.activity = context;
        this.inflater = LayoutInflater.from(context);
        this.contacts = contacts;
        this.stat = stat;
    }
    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //pick a view for the view holder
       View view = inflater.inflate(R.layout.custom_row,parent,false);
       //send the view to the view holder class
        RecyclerHolder holder= new RecyclerHolder(view);
        return holder;
    }
    int prevpos=0;
    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        //the point adding valuesto each itemdisplay in the recycler viewer and putting in Recycler viewer
        //get the instance of the contacs array and the value set to it
        try {
            int g = holder.getAdapterPosition();
            Product contact = contacts.get(g);
            holder.email.setText(contact.getEmail());
            holder.name.setText(contact.getName());
            holder.dept.setText(contact.getLevel() + " Level - " + contact.getDept());
            if (stat.equals("online")) {
                Glide.with(activity).load(contact.getPics_path()).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).thumbnail(0.5f).into(holder.pics);
            }

            if (stat.equals("ofline")) {
                //  byte[] image_data = cursor.getBlob(cursor.getColumnIndex(dbColumnList.userDetails.COLUMN_PICS));
                Bitmap bitmap = BitmapFactory.decodeByteArray(contact.getBLOB(), 0, contact.getBLOB().length);
                holder.pics.setImageBitmap(bitmap);
                // Glide.with(activity).load(contact.getPics_path()).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).thumbnail(0.5f).into(holder.pics);
            }
            ///disway toaccess the animation - AnimationUtils.animate(holder);
            //or you check if scrolling up or down
            if (position > prevpos) {
                //you are scrooling down
                AnimationUtils.animate(holder, true);
                // AnimationUtils.animategroup(holder,true);

            } else {
                //no you are scroolingup
                AnimationUtils.animate(holder, false);
                //AnimationUtils.animategroup(holder,false);
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
        TextView email, name,dept;
        ImageView pics;
        public RecyclerHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            dept = (TextView) itemView.findViewById(R.id.dept);
            email = (TextView) itemView.findViewById(R.id.email);
            pics = (ImageView) itemView.findViewById(R.id.profile);
        }
    }
    public void setFilter(ArrayList<Product> newList){
        contacts = new ArrayList<>();
        contacts.addAll(newList);
        notifyDataSetChanged();
    }
}
