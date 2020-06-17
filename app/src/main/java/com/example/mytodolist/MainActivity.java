package com.example.mytodolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.mytodolist.db.TaskContract;
import com.example.mytodolist.db.TaskDbHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView lvItems;

    private ArrayAdapter<String> itemsAdapter;
    private TaskDbHelper taskDbHelper;
    private TextView tvTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskDbHelper = new TaskDbHelper(this);
        lvItems = findViewById(R.id.list_todo);
        updateUI();
        tvTimer = findViewById(R.id.timer);
        new SecondTimer().execute(tvTimer);

    }

    private void updateUI(){
        ArrayList<String> items = new ArrayList<>();
        SQLiteDatabase db = taskDbHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()){
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            items.add(cursor.getString(idx));
        }
        if (itemsAdapter == null) {
            itemsAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo,
                    R.id.task_title,
                    items);
            lvItems.setAdapter(itemsAdapter);
        } else {
            itemsAdapter.clear();
            itemsAdapter.addAll(items);
            itemsAdapter.notifyDataSetChanged();
        }
        cursor.close();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                addNewTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewTask(){
        final EditText taskEditText = new EditText(this);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Добавить новую задачу")
                .setMessage("Что бы вы хотели добавить?")
                .setView(taskEditText)
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String task = String.valueOf(taskEditText.getText());
                        SQLiteDatabase db = taskDbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                        db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                null,
                                values,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        db.close();
                        updateUI();
                    }
                })
                .setNegativeButton("Отмена", null)
                .create();
        alertDialog.show();
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = taskDbHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
    }

    class SecondTimer extends AsyncTask<View, Integer, Void> {
        int currentVal;
        @Override
        protected Void doInBackground(View... views) {
            TextView tvTimer = (TextView) views[0];
            while (true) {
                publishProgress(++currentVal);
                SystemClock.sleep(1000);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            tvTimer.setText(String.valueOf(values[0]));
        }
    }

}
