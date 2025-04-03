package com.smartapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartapp.R;
import com.smartapp.models.Material;

import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {

    private List<Material> materials;
    private Context context;
    private OnMaterialClickListener listener;

    public interface OnMaterialClickListener {
        void onMaterialClick(Material material);
        void onDownloadClick(Material material);
    }

    public MaterialAdapter(Context context, List<Material> materials, OnMaterialClickListener listener) {
        this.context = context;
        this.materials = materials;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        Material material = materials.get(position);
        
        holder.titleText.setText(material.getTitle());
        holder.descriptionText.setText(material.getDescription());
        holder.subjectText.setText(material.getSubject());
        holder.fileSizeText.setText(material.getFormattedFileSize());

        // Set file type icon based on file type
        setFileTypeIcon(holder.fileTypeIcon, material.getFileType());

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMaterialClick(material);
            }
        });

        holder.downloadButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDownloadClick(material);
            }
        });
    }

    @Override
    public int getItemCount() {
        return materials != null ? materials.size() : 0;
    }

    public void updateData(List<Material> newMaterials) {
        this.materials = newMaterials;
        notifyDataSetChanged();
    }

    private void setFileTypeIcon(ImageView imageView, String fileType) {
        if (fileType == null) return;

        int iconRes;
        switch (fileType.toLowerCase()) {
            case "pdf":
                iconRes = android.R.drawable.ic_menu_agenda; // Replace with your PDF icon
                break;
            case "docx":
            case "doc":
                iconRes = android.R.drawable.ic_menu_edit; // Replace with your DOC icon
                break;
            default:
                iconRes = android.R.drawable.ic_menu_help; // Default icon
        }
        imageView.setImageResource(iconRes);
    }

    static class MaterialViewHolder extends RecyclerView.ViewHolder {
        ImageView fileTypeIcon;
        TextView titleText;
        TextView descriptionText;
        TextView subjectText;
        TextView fileSizeText;
        ImageButton downloadButton;

        MaterialViewHolder(View itemView) {
            super(itemView);
            fileTypeIcon = itemView.findViewById(R.id.fileTypeIcon);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            subjectText = itemView.findViewById(R.id.subjectText);
            fileSizeText = itemView.findViewById(R.id.fileSizeText);
            downloadButton = itemView.findViewById(R.id.downloadButton);
        }
    }
}