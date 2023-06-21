package com.example.inclassassessment05;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    ProgressBar progressBar;
    TextView showTerm, upToTerm;
    ListView listViewTerm;
    SeekBar seekBar;

    Handler handler;
    // holds fibonacci numbers
    ArrayList<Integer> numberList = new ArrayList<>();
    // connect arraylist with listView
    ArrayAdapter<Integer> adapter;
    ExecutorService threadPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Fibonacci Calculator");

        //thread pool
        threadPool = Executors.newFixedThreadPool(3);

        //fetch view
        progressBar = findViewById(R.id.progressBar);
        showTerm = findViewById(R.id.showTerm);
        upToTerm = findViewById(R.id.upToTerm);
        listViewTerm = findViewById(R.id.listViewTerm);
        seekBar = findViewById(R.id.seekBar);


        // set ArrayAdapter to the ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, numberList);
        listViewTerm.setAdapter(adapter);

        // OnClickListener to button
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start DoWork task in the thread pool with the current progress of the SeekBar
                threadPool.execute(new DoWork(seekBar.getProgress(), handler));
            }
        });
        // // handler used to handle message from DoWork task
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case DoWork.STATUS_START:
                        // when the task starts, reset progressBar and clear ArrayList
                        Log.d("test", "Starting...");
                        progressBar.setMax(seekBar.getProgress());
                        progressBar.setProgress(0);
                        numberList.clear();
                        adapter.notifyDataSetChanged();
                        break;
                    case DoWork.STATUS_PROGRESS:
                        // when the task reports progress, update progressBar, add new number to the ArrayList
                        Log.d("test", "Progress...");
                        Bundle bundle = msg.getData();
                        int progress = bundle.getInt(DoWork.PROGRESS_KEY);
                        int number = bundle.getInt(DoWork.NUMBER);
                        progressBar.setProgress(progress);
                        numberList.add(number);
                        adapter.notifyDataSetChanged();
                        // update showTerm to display the progress out of the total number of terms
                        showTerm.setText(progress + "/" + seekBar.getProgress());
                        break;
                    case DoWork.STATUS_STOP:
                        // log if task stop
                        Log.d("test", "Stop.");
                        break;
                }
                return false;
            }
        });

        //seekBar
        seekBar.setMax(40);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // When the SeekBar progress changes, update the TextViews
                upToTerm.setText(progress + "Time(s)");
                showTerm.setText("0/" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}

class DoWork implements Runnable {
    static final int STATUS_START = 0x00;
    static final int STATUS_PROGRESS= 0x01;
    static final int STATUS_STOP = 0x02;
    static final String PROGRESS_KEY = "PROGRESS";
    static final String NUMBER = "NUMBER";
    int complexity;
    Handler handler;
    // constructor takes the complexity for fibonacci and handler to send message
    public DoWork(int complexity, Handler handler) {
        this.complexity = complexity;
        this.handler = handler;
    }

    @Override
    public void run() {
        // send a start message
        Message startMessage = new Message();
        startMessage.what = STATUS_START;
        handler.sendMessage(startMessage);

        // generate Fibonacci numbers and send a progress message for each one
        for(int i=0; i<complexity; i++){
            int number = fibonacci(i);
            Message message = new Message();
            message.what = STATUS_PROGRESS;
            Bundle bundle = new Bundle();
            bundle.putInt(PROGRESS_KEY, i+1);
            bundle.putInt(NUMBER, number);
            message.setData(bundle);
            handler.sendMessage(message);
        }
        // send a stop message
        Message endMessage = new Message();
        endMessage.what = STATUS_STOP;
        handler.sendMessage(endMessage);
    }

    // method calculating fibonacci
    public static int fibonacci(int n) {
        if (n <= 1) {
            return n;
        } else {
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
    }
}

