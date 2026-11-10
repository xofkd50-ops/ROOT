package com.example.photoviewer;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    public static class PostItem {
        public int id;
        public String title;
        public String text;
        public String date;
        public Bitmap bitmap;

        public PostItem(int id, String title, String text, String date, Bitmap bitmap) {
            this.id = id;
            this.title = title;
            this.text = text;
            this.date = date;
            this.bitmap = bitmap;
        }
    }

    private List<PostItem> itemList;

    public ImageAdapter(List<PostItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        PostItem item = itemList.get(position);
        holder.itemTitle.setText(item.title);
        holder.itemText.setText(item.text);
        holder.itemDate.setText(item.date);
        holder.imageView.setImageBitmap(item.bitmap);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("침입 감지 상세 정보 (#" + (position + 1) + ")");
                builder.setMessage("• 주요 검출 객체: " + item.title + "\n"
                        + "• 전체 검출 클래스: " + item.text + "\n"
                        + "• 감지 시각: " + item.date);

                ImageView dialogImg = new ImageView(v.getContext());
                dialogImg.setImageBitmap(item.bitmap);
                dialogImg.setAdjustViewBounds(true);
                dialogImg.setPadding(24, 16, 24, 16);
                builder.setView(dialogImg);

                builder.setPositiveButton("확인", null);
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        TextView itemDate, itemTitle, itemText;
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            itemDate = itemView.findViewById(R.id.itemDate);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemText = itemView.findViewById(R.id.itemText);
            imageView = itemView.findViewById(R.id.imageViewItem);
        }
    }
}
