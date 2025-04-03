package com.smartapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.smartapp.R;
import com.smartapp.models.Assignment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    private List<Assignment> assignments;
    private Context context;
    private OnAssignmentClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnAssignmentClickListener {
        void onAssignmentClick(Assignment assignment);
        void onFileClick(Assignment assignment);
    }

    public AssignmentAdapter(Context context, List<Assignment> assignments, OnAssignmentClickListener listener) {
        this.context = context;
        this.assignments = assignments;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Assignment assignment = assignments.get(position);
        
        holder.titleText.setText(assignment.getTitle());
        holder.subjectText.setText(assignment.getSubject());
        holder.descriptionText.setText(assignment.getDescription());
        holder.dueDateText.setText("Due: " + dateFormat.format(assignment.getDueDate()));
        
        // Set status chip
        setupStatusChip(holder.statusChip, assignment);

        // Set file info
        holder.fileNameText.setText(assignment.getFileName());
        holder.fileSizeText.setText(assignment.getFormattedFileSize());

        // Show grade if available
        if (assignment.isGraded() && assignment.getGrade() != null) {
            holder.gradeText.setVisibility(View.VISIBLE);
            holder.gradeText.setText(String.format(Locale.getDefault(), 
                "Grade: %d%%", assignment.getGrade()));
        } else {
            holder.gradeText.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAssignmentClick(assignment);
            }
        });

        holder.fileContainer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFileClick(assignment);
            }
        });
    }

    private void setupStatusChip(Chip chip, Assignment assignment) {
        int bgColor;
        String status;
        
        if (assignment.isGraded()) {
            status = "Graded";
            bgColor = R.color.success;
        } else if (assignment.isSubmitted()) {
            status = "Submitted";
            bgColor = R.color.primary;
        } else if (assignment.isOverdue()) {
            status = "Overdue";
            bgColor = R.color.error;
        } else {
            status = "Pending";
            bgColor = R.color.secondary_text;
        }

        chip.setText(status);
        chip.setChipBackgroundColorResource(bgColor);
        chip.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    @Override
    public int getItemCount() {
        return assignments != null ? assignments.size() : 0;
    }

    public void updateData(List<Assignment> newAssignments) {
        this.assignments = newAssignments;
        notifyDataSetChanged();
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        Chip statusChip;
        TextView subjectText;
        TextView dueDateText;
        TextView descriptionText;
        View fileContainer;
        ImageView fileIcon;
        TextView fileNameText;
        TextView fileSizeText;
        TextView gradeText;

        AssignmentViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            statusChip = itemView.findViewById(R.id.statusChip);
            subjectText = itemView.findViewById(R.id.subjectText);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            fileContainer = itemView.findViewById(R.id.fileContainer);
            fileIcon = itemView.findViewById(R.id.fileIcon);
            fileNameText = itemView.findViewById(R.id.fileNameText);
            fileSizeText = itemView.findViewById(R.id.fileSizeText);
            gradeText = itemView.findViewById(R.id.gradeText);
        }
    }
}