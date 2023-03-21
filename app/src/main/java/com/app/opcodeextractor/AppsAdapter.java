package com.app.opcodeextractor;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {

    Context context1;
    List<String> stringList;

    public AppsAdapter(Context context, List<String> list) {
        context1 = context;
        stringList = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView imageView;
        public TextView textView_App_Name;
        public TextView textView_App_Package_Name;

        public ViewHolder(View view) {
            super(view);

            cardView = view.findViewById(R.id.card_view);
            imageView = view.findViewById(R.id.imageview);
            textView_App_Name = view.findViewById(R.id.Apk_Name);
            textView_App_Package_Name = view.findViewById(R.id.Apk_Package_Name);
        }
    }

    @NonNull
    @Override
    public AppsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view2 = LayoutInflater.from(context1).inflate(R.layout.cardview_layout, parent, false);
        return new ViewHolder(view2);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context1);
        OpCodeExtractor opCodeExtractor = new OpCodeExtractor(context1);
        final String ApplicationPackageName = stringList.get(position);
        String ApplicationLabelName = apkInfoExtractor.GetAppName(ApplicationPackageName);
        Drawable drawable = apkInfoExtractor.getAppIconByPackageName(ApplicationPackageName);
        viewHolder.textView_App_Name.setText(ApplicationLabelName);
        viewHolder.textView_App_Package_Name.setText(ApplicationPackageName);
        viewHolder.imageView.setImageDrawable(drawable);

        viewHolder.cardView.setOnClickListener(view -> {
            try {
                opCodeExtractor.main(ApplicationPackageName, apkInfoExtractor.getApk(ApplicationPackageName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }
}