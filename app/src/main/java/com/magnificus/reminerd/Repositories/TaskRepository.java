package com.magnificus.reminerd.Repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.magnificus.reminerd.Entities.CategoryEntity;
import com.magnificus.reminerd.Entities.ColorEntity;
import com.magnificus.reminerd.Entities.TaskEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by allysson on 31/05/17.
 */

public class TaskRepository extends SQLiteOpenHelper {

    private final Context context;

    public TaskRepository(Context context) {
        super(context, "Tasks", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE Tasks (" +
                "ID CHAR(36) PRIMARY KEY, " +
                "Title TEXT NOT NULL, " +
                "Description TEXT NOT NULL, " +
                "Date TEXT, " +
                "Time TEXT, " +
                "IDCategoryEntity CHAR(36) NOT NULL);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*String sql = "";
        switch (oldVersion) {
            case 1:
                sql = "QUERY AQUI";
                db.execSQL(sql);
        }*/
    }

    public List<TaskEntity> getTasks() {
        String sql = "SELECT * FROM Tasks";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);

        return populateTasks(c);
    }

    public List<TaskEntity> getTasksByCategory(String id) {
        String sql = "SELECT * FROM Tasks WHERE IDCategoryEntity = ?";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, new String[]{id});

        return populateTasks(c);
    }

    public TaskEntity getTask(String id) {
        String sql = "SELECT * FROM tasks WHERE ID = ?";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, new String[]{id});
        TaskEntity task = new TaskEntity();
        if (c != null) {
            if(c.moveToFirst()) {
                task.setID(c.getString(c.getColumnIndex("ID")));
                task.setTitle(c.getString(c.getColumnIndex("Title")));
                task.setDescription(c.getString(c.getColumnIndex("Description")));
                task.setDate(c.getString(c.getColumnIndex("Date")));
                task.setTime(c.getString(c.getColumnIndex("Time")));
                task.setIDCategoryEntity(c.getString(c.getColumnIndex("IDCategoryEntity")));
                task.setCategoryEntity(this.setCategoryObject(task.getIDCategoryEntity()));

                return task;
            }
        }

        return null;
    }

    @NonNull
    public List<TaskEntity> populateTasks(Cursor c) {
        List<TaskEntity> tasks = new ArrayList<TaskEntity>();

        while (c.moveToNext()) {
            TaskEntity task = new TaskEntity();
            task.setID(c.getString(c.getColumnIndex("ID")));
            task.setTitle(c.getString(c.getColumnIndex("Title")));
            task.setDescription(c.getString(c.getColumnIndex("Description")));
            task.setDate(c.getString(c.getColumnIndex("Date")));
            task.setTime(c.getString(c.getColumnIndex("Time")));
            task.setIDCategoryEntity(c.getString(c.getColumnIndex("IDCategoryEntity")));
            task.setCategoryEntity(this.setCategoryObject(task.getIDCategoryEntity()));

            tasks.add(task);
        }

        return tasks;
    }

    //TODO: This another gambex needs to be resolved later
    private CategoryEntity setCategoryObject(String id) {
        CategoryRepository categoryRepository = new CategoryRepository(this.context);
        CategoryEntity categoryEntity = categoryRepository.getCategory(id);

        if (categoryEntity == null) {
//            categoryEntity.setID(generateUUID());
//            categoryEntity.setName("Default category");
//            categoryEntity.setIDColorEntity((long) 1);
//            categoryEntity.setColorEntity(categoryRepository.setColorObject());
            return null;
        }

        categoryRepository.close();

        return categoryEntity;
    }

    public void insert(TaskEntity taskEntity) {
        SQLiteDatabase db = getWritableDatabase();
        setIdIfNecessary(taskEntity);
        ContentValues data = buildTaskObject(taskEntity);
        db.insert("Tasks", null, data);
    }

    private void setIdIfNecessary(TaskEntity taskEntity) {
        if (taskEntity.getID() == null){
            taskEntity.setID(generateUUID());
        }
    }

    public void update(TaskEntity taskEntity) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues data = buildTaskObject(taskEntity);

        String[] params = {taskEntity.getID()};
        db.update("Tasks", data, "ID = ?", params);
    }

    public void delete(TaskEntity taskEntity) {
        SQLiteDatabase db = getWritableDatabase();

        String[] params = {taskEntity.getID()};
        db.delete("Tasks", "id = ?", params);
    }

    @NonNull
    private ContentValues buildTaskObject(TaskEntity taskEntity) {
        ContentValues data = new ContentValues();
        data.put("ID", taskEntity.getID());
        data.put("Title", taskEntity.getTitle());
        data.put("Description", taskEntity.getDescription());
        data.put("Date", taskEntity.getDate());
        data.put("Time", taskEntity.getTime());
        data.put("IDCategoryEntity", taskEntity.getIDCategoryEntity());

        return data;
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public void syncTasks(List<TaskEntity> taskEntities) {
        for(TaskEntity taskEntity : taskEntities) {
            if(taskExists(taskEntity)) {
                update(taskEntity);
            } else {
                insert(taskEntity);
            }
        }
    }

    private boolean taskExists(TaskEntity taskEntity) {
        SQLiteDatabase db = getReadableDatabase();
        String exists = "SELECT ID FROM Tasks WHERE ID = ? LIMIT 1";
        Cursor cursor = db.rawQuery(exists, new String[]{taskEntity.getID()});
        return cursor.getCount() > 0;
    }
}
