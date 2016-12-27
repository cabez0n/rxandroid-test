package com.chaoticmoon.rxtests;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();
    private ImageView imageView;
    private long ROTATE_ANIMATION_DURATION = 1000;

    private class ExampleTask extends AsyncTask<URL, Integer, Long> {
        private static final int COUNT = 10;
        private String tag;

        public ExampleTask(String tag) {
            this.tag = tag;
        }

        protected Long doInBackground(URL... urls) {
            long totalSize = 0;
            for (int i = 0; i < COUNT; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.v(tag, "Doing work...");

                publishProgress((int) ((i / (float) COUNT) * 100));
                if (isCancelled()) break;
            }
            return totalSize;
        }


        protected void onPostExecute(Long result) {
            Toast.makeText(MainActivity.this, "Finished", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        example1();

    }

    private void example1() {
        ExampleTask task1 = new ExampleTask("task1");
        task1.execute();

        //        ExampleTask task2 = new ExampleTask("task2");
        //        task2.execute();

        runRxAnimation();
    }

    private void example2() {
        ExampleTask task1 = new ExampleTask("task1");
        task1.execute();
        runRxAnimation();
    }

    protected void runRxAnimation() {
        imageView = (ImageView) findViewById(R.id.imageView);
        Log.v(TAG, "runRxAnimation timer");
        Observable
                .interval(ROTATE_ANIMATION_DURATION + 200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .take(4)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "Animation completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.v(TAG, "onNext " + aLong);
                        imageView.animate().rotationBy(360).setDuration(ROTATE_ANIMATION_DURATION).setInterpolator(new AccelerateDecelerateInterpolator());
                    }
                });
    }
}
