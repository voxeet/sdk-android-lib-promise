package com.voxeet.testpromise.errors;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.voxeet.promise.solve.ErrorPromise;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.PromiseExec;
import com.voxeet.promise.solve.PromiseSolver;
import com.voxeet.promise.solve.Solver;
import com.voxeet.testpromise.utils.AndroidMockUtil;

public class PromiseTestNPECatch {

    @Before
    public void setHandler() {
        Promise.setHandler(AndroidMockUtil.mockMainThreadHandler());
    }

    @Test
    public void test() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        final boolean[] catched = {false};
        System.out.println("executing test");
        new Promise<String>(new PromiseSolver<String>() {
            @Override
            public void onCall(@NonNull Solver<String> solver) {
                solver.resolve((String) null);
            }
        })
                .then(new PromiseExec<String, String>() {
                    @Override
                    public void onCall(@Nullable String result, @NonNull Solver<String> solver) {
                        //explode right here
                        solver.resolve(result.toLowerCase());
                    }
                })
                .then(new PromiseExec<String, Void>() {
                    @Override
                    public void onCall(@Nullable String result, @NonNull Solver<Void> solver) {
                        System.out.println("you should not see this");
                        catched[0] = false;
                        latch.countDown();
                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        System.out.println("error catched");
                        catched[0] = true;
                        error.printStackTrace();
                        latch.countDown();
                    }
                });

        //6s are enough
        latch.await(6, TimeUnit.SECONDS);
        if (!catched[0]) {
            throw new IllegalStateException("Expected error...");
        }
    }
}
