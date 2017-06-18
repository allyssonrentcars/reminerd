package com.magnificus.reminerd.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.magnificus.reminerd.Activities.TaskFormActivity;
import com.magnificus.reminerd.Adapters.TasksAdapter;
import com.magnificus.reminerd.Entities.TaskEntity;
import com.magnificus.reminerd.R;
import com.magnificus.reminerd.Repositories.TaskRepository;

import java.util.List;

/**
 * Created by gui on 28/05/17.
 */

public class TasksFragment extends Fragment {

    private ListView tasksListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        tasksListView = (ListView) view.findViewById(R.id.lista_tasks);

        tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TaskEntity taskEntity = (TaskEntity) tasksListView.getItemAtPosition(position);

                Intent intent = new Intent(getContext(), TaskFormActivity.class);
                intent.putExtra("task", taskEntity);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        TaskRepository repository = new TaskRepository(getContext());
        List<TaskEntity> taskEntityList = repository.getTasks();
        repository.close();

        TasksAdapter adapter = new TasksAdapter(getContext(), R.layout.row_task, taskEntityList);
        tasksListView.setAdapter(adapter);
    }
}
