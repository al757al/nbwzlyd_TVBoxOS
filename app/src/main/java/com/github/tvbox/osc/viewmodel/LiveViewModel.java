package com.github.tvbox.osc.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.tvbox.osc.bean.LiveChannelItem;
import com.github.tvbox.osc.util.Force;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LiveViewModel extends ViewModel {

    public MutableLiveData<Object> result = new MutableLiveData<>();
    ;
    public ExecutorService executor;

    public void getUrl(LiveChannelItem item) {
        execute(() -> {
            String url = item.getUrl();
            if (!item.isForceTv()) {
                return url;
            }
            url = Force.get().fetch(url);
            return url;
        });
    }

    public void getUrl(String url) {
        execute(() -> {
            String tmpUrl = url;
            if (tmpUrl.startsWith("P") || tmpUrl.equals("mitv")) {
                tmpUrl = Force.get().fetch(tmpUrl);
            }
            return tmpUrl;
        });
    }

    private void execute(Callable<?> callable) {
        if (executor != null) executor.shutdownNow();
        executor = Executors.newFixedThreadPool(2);
        executor.execute(() -> {
            try {
                if (!Thread.interrupted())
                    result.postValue(executor.submit(callable).get(10, TimeUnit.SECONDS));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onCleared() {
        if (executor != null) executor.shutdownNow();
    }
}
