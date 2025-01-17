package com.app.opcodeextractor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ModelRVAdapter extends RecyclerView.Adapter<ModelRVAdapter.ViewHolder> {

    Context context1;
    List<String> stringList;

    public ModelRVAdapter(Context context, List<String> list) {
        context1 = context;
        stringList = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView_Model_Name;
        CardView cardView;

        public ViewHolder(View view) {
            super(view);
            textView_Model_Name = view.findViewById(R.id.Model_Name);
            cardView = view.findViewById(R.id.card_view);
        }
    }

    @NonNull
    @Override
    public ModelRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view2 = LayoutInflater.from(context1).inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view2);
    }

    @Override
    public void onBindViewHolder(ModelRVAdapter.ViewHolder viewHolder, int position) {
        ModelSelector models = new ModelSelector();
        final String ModelName = stringList.get(position);
        String APIHeader = models.GetApiHeader(ModelName);
        viewHolder.textView_Model_Name.setText(ModelName);

        viewHolder.cardView.setOnClickListener(view -> {
            ExtractionActivity.pBar.setVisibility(View.VISIBLE);
            Model ob = new Model(context1);
            AtomicReference<String> result = new AtomicReference<>("Invalid");
            ExecutorService executor = Executors.newFixedThreadPool(1);
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                try {
                    result.set(ob.main(ExtractionActivity.opcodeMap, APIHeader));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    handler.post(() -> {
                        ExtractionActivity.pBar.setVisibility(View.GONE);
                        Toast.makeText(context1, "The app is "+result, Toast.LENGTH_LONG).show();
                    });
                }
            });

            executor.shutdown();
        });
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }
}