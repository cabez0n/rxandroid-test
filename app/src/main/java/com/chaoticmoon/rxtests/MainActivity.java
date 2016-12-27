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
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
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

        imageView = (ImageView) findViewById(R.id.imageView);

//        example1();

//        example2();

        example3();

    }

    /**
     * Concurrent execution by default
     */
    private void example1() {
        ExampleTask task1 = new ExampleTask("task1");
        task1.execute();
        runAnimationWithRx();
    }


    /**
     * Also concurrent
     */
    private void example2() {
        ExampleTask task1 = new ExampleTask("task1");
        task1.execute();
        imageView.animate().rotationBy(360).setDuration(ROTATE_ANIMATION_DURATION * 5).setInterpolator(new AccelerateDecelerateInterpolator());
    }

    /**
     * Concurrency with zip.
     *
     * This is commonly used for 2 api calls, or long operations for which we don't know how long they are going to take
     * and we want to take some action after the 2 are done like combining the results
     *
     * The animation is a fixed timed process. And there's also no result from the animation so the combine method is trivial.
     */
    private void example3() {
        ExampleTask task1 = new ExampleTask("task1");
        task1.execute();
        runAnimationAndAsyncTaskWithZip();
    }


    protected void runAnimationWithRx() {
        Observable<Long> animationObservable = Observable
                .interval(ROTATE_ANIMATION_DURATION + 200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

        animationObservable.take(2)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "Animation completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.w(TAG, "onError " + e.getMessage());
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.v(TAG, "Got element " + aLong);
                        imageView.animate().rotationBy(360).setDuration(ROTATE_ANIMATION_DURATION).setInterpolator(new AccelerateDecelerateInterpolator());
                    }
                });
    }

    protected void runAnimationAndAsyncTaskWithZip() {
        Log.v(TAG, "runAnimationAndAsyncTaskWithZip timer");
        Observable.OnSubscribe<? extends Object> registerAppSubscriber = new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                Log.i(TAG, "Finished");
            }
        };
        Observable<? extends Object> registerAppObservable = Observable.create(registerAppSubscriber);

        Observable<Long> animationObservable = Observable
                .interval(ROTATE_ANIMATION_DURATION + 200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

        animationObservable.take(2)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "Animation completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.w(TAG, "onError " + e.getMessage());
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.v(TAG, "Animation observable onNext: " + aLong);
                        imageView.animate().rotationBy(360).setDuration(ROTATE_ANIMATION_DURATION).setInterpolator(new AccelerateDecelerateInterpolator());
                    }
                });

        Observable<Object> combinedObservable = Observable.zip(animationObservable, registerAppObservable, new Func2<Long, Object, Object>() {
            @Override
            public Object call(Long aLong, Object o) {
                Log.v(TAG, "Func2 finished");
                return o;
            }
        });

        combinedObservable.subscribe(new Subscriber<Object>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "Combined observable onCompleted");
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Object o) {
                Log.v(TAG, "Combined observable onNext");
            }
        });


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
