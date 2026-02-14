package com.example.dailyflows.ui.agenda;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import com.example.dailyflows.R;
import com.example.dailyflows.data.local.entities.TaskEntity;
import com.example.dailyflows.util.DateTimeUtil;

public class TaskListAdapter extends ListAdapter<TaskEntity, TaskListAdapter.VH> {

    public interface Listener {
        void onToggleDone(TaskEntity task, boolean done);
        void onClick(TaskEntity task);
    }

    private final Listener listener;

    public TaskListAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    static final DiffUtil.ItemCallback<TaskEntity> DIFF = new DiffUtil.ItemCallback<TaskEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskEntity oldItem, @NonNull TaskEntity newItem) {
            boolean noteEq = (oldItem.note == null && newItem.note == null)
                    || (oldItem.note != null && oldItem.note.equals(newItem.note));

            return oldItem.done == newItem.done
                    && oldItem.dueAtMillis == newItem.dueAtMillis
                    && oldItem.priority == newItem.priority
                    && oldItem.title.equals(newItem.title)
                    && noteEq;
        }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        TaskEntity t = getItem(pos);

        h.tvTitle.setText(t.title);
        String meta = "";
        if (t.dueAtMillis > 0) meta = DateTimeUtil.formatTime(t.dueAtMillis);
        meta += "  •  приоритет " + t.priority;
        h.tvMeta.setText(meta);

        h.cbDone.setOnCheckedChangeListener(null);
        h.cbDone.setChecked(t.done);
        h.cbDone.setOnCheckedChangeListener((btn, checked) -> listener.onToggleDone(t, checked));

        h.itemView.setOnClickListener(v -> listener.onClick(t));
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cbDone;
        MaterialTextView tvTitle;
        MaterialTextView tvMeta;

        VH(@NonNull View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cbDone);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }
}
