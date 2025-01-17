package com.app.opcodeextractor;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
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
        final String ApplicationPackageName = stringList.get(position);
        String ApplicationLabelName = apkInfoExtractor.GetAppName(ApplicationPackageName);
        Drawable drawable = apkInfoExtractor.getAppIconByPackageName(ApplicationPackageName);
        File apkFile = apkInfoExtractor.getApk(ApplicationPackageName);
        viewHolder.textView_App_Name.setText(ApplicationLabelName);
        viewHolder.textView_App_Package_Name.setText(ApplicationPackageName);
        viewHolder.imageView.setImageDrawable(drawable);

        viewHolder.cardView.setOnClickListener(view -> {
            Intent intent = new Intent (context1, ExtractionActivity.class);
            intent.putExtra("apk", apkFile);
            intent.putExtra("package", ApplicationPackageName);
            context1.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }
}