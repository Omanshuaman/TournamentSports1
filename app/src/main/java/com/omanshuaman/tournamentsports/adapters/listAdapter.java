package com.omanshuaman.tournamentsports.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.omanshuaman.tournamentsports.R;
import com.example.adminuser.models.ListModel;
import com.squareup.picasso.Picasso;

import java.util.List;


public class listAdapter extends RecyclerView.Adapter<listAdapter.MyHolder>{

    Context context;
    List<ListModel> postList;

    public listAdapter(Context context, List<ListModel> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.activity_list, parent, false);

        return new MyHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {
        //get data
        final String uid = postList.get(i).getId();
        String uBed = postList.get(i).getBed();
        String uBedroom = postList.get(i).getBedroom();
        String uImage = postList.get(i).getImage();
        final String pOldprice = postList.get(i).getOldprice();
        final String pTitle = postList.get(i).getTitle();
        final String ptotalprice = postList.get(i).getTotalPrice();
        final String type = postList.get(i).getType();


            try {
                Picasso.get().load(uImage).into(MyHolder.uImageView);
            }
            catch (Exception e){

            }
        }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    static class MyHolder extends RecyclerView.ViewHolder {

        public static ImageView uImageView;
        //views from row_post.xml
        TextView pBed, pBedroom, pDescription, pOld_price, pNew_price,pTotal_price;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uImageView = itemView.findViewById(R.id.pImageView);
            pBed = itemView.findViewById(R.id.pBed);
            pBedroom = itemView.findViewById(R.id.pBedroom);
            pDescription = itemView.findViewById(R.id.pDescription);
            pOld_price = itemView.findViewById(R.id.pOld_price);
            pNew_price = itemView.findViewById(R.id.pNew_price);

        }
    }

}