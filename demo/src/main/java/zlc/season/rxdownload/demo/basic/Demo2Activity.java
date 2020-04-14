package zlc.season.rxdownload.demo.basic;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import zlc.season.rxdownload.demo.R;
import zlc.season.rxdownload.demo.utils.ProgressButton;
import zlc.season.rxdownload4.Progress;
import zlc.season.rxdownload4.RxDownloadKt;

public class Demo2Activity extends AppCompatActivity implements View.OnClickListener {
    private ProgressButton button0, button, button2, button3;
    private Disposable disposable;
    private Subscription subscription;
    private Map<String, Progress> progressMap;
    private long downloadStartTime;
    private double maxRate;
    private long maxRateCurrentTime;
    private int recount;
    private final static int RECONNECT_COUNT = 3;//重连次数
    private final static int RATE_TIMES = 5;//速度倍率
    private final static int PERIOD = Integer.MAX_VALUE;//下降期间
    private final static int START_TIME = 5000;//开始统计
    private long totalTime;

    private String url = "https://dldir1.qq.com/weixin/android/weixin706android1460.apk";
    //    private String[] urls0 = new String[]{"http://tfs.alipayobjects.com/L1/71/100/and/alipay_2088231010784741_yifan44.apk"};
    private String[] urls0 = new String[]{"http://pkg1.zhimg.com/zhihu/futureve-app-market-release-6.10.0(1512)-bangcle_jytest19.apk"};
    private String[] urls1 = new String[]{"https://dldir1.qq.com/weixin/android/weixin706android1460.apk",
            "http://tfs.alipayobjects.com/L1/71/100/and/alipay_2088231010784741_yifan44.apk",
            "http://imtt.dd.qq.com/16891/apk/94EB2E42A58346E94008B32AA7C7BE78.apk?fsname=com.sina.weibo_9.8.2_4006.apk&csr=db5e",
            "https://pkg1.zhimg.com/zhihu/futureve-app-market-release-6.10.0(1512)-bangcle_jytest19.apk",
            "http://download.alicdn.com/wireless/fleamarket/latest/fleaMarket_1566379054886.apk"};

    private String[] urls2 = new String[]{"http://static.sandboxol.com/sandbox/blockmango/engine2_engineres/20020/Sound.zip",
            "http://static.sandboxol.com/sandbox/blockmango/engine2_engineres/20020/Media01.zip",
            "http://static.sandboxol.com/sandbox/blockmango/engine2_engineres/20020/lua.zip",
            "http://static.sandboxol.com/sandbox/blockmango/engine2_engineres/20020/effects.zip",
            "http://static.sandboxol.com/sandbox/blockmango/engine2_engineres/20020/Skins04.zip",
            "http://static.sandboxol.com/sandbox/blockmango/engine2_engineres/20020/Skins01.zip",
            "http://static.sandboxol.com/sandbox/blockmango/engine2_engineres/20020/GUI.zip",
            "http://static.sandboxol.com/sandbox/blockmango/engine2_engineres/20020/Skins03.zip",
            "http://static.sandboxol.com/sandbox/blockmango/engine2_engineres/20020/Skins02.zip"
    };
    private String[] urls3 = new String[]{"https://ks3-cn-shanghai.ksyun.com/sandbox-region/engine2_engineres/80000/Sound.zip",
            "https://ks3-cn-shanghai.ksyun.com/sandbox-region/engine2_engineres/80000/Media01.zip",
            "https://ks3-cn-shanghai.ksyun.com/sandbox-region/engine2_engineres/80000/lua.zip",
            "https://ks3-cn-shanghai.ksyun.com/sandbox-region/engine2_engineres/80000/effects.zip",
            "https://ks3-cn-shanghai.ksyun.com/sandbox-region/engine2_engineres/80000/Skins04.zip",
            "https://ks3-cn-shanghai.ksyun.com/sandbox-region/engine2_engineres/80000/Skins01.zip",
            "https://ks3-cn-shanghai.ksyun.com/sandbox-region/engine2_engineres/80000/GUI.zip",
            "https://ks3-cn-shanghai.ksyun.com/sandbox-region/engine2_engineres/80000/Skins03.zip",
            "https://ks3-cn-shanghai.ksyun.com/sandbox-region/engine2_engineres/80000/Skins02.zip"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_download);
        button0 = findViewById(R.id.button0);
        button0.setOnClickListener(this);
        button = findViewById(R.id.button);
        button.setOnClickListener(this);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button3 = findViewById(R.id.button3);
        button3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        recount = 0;
        totalTime = System.currentTimeMillis();
        switch (v.getId()) {
            case R.id.button0:
                download(urls0);
                break;
            case R.id.button:
                download(urls1);
                break;
            case R.id.button2:
                download(urls2);
                break;
            case R.id.button3:
                download(urls3);
                break;
            default:
                break;
        }
    }

    private void download(String... urls) {
        new RxPermissions(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        maxRate = 0;
                        downloadStartTime = 0;
                        maxRateCurrentTime = 0;
                        progressMap = new HashMap<>();
                        List<Flowable<Progress>> list = new ArrayList<>();
                        for (String url : urls) {
                            list.add(RxDownloadKt.download(url));
                        }
                        Flowable.mergeDelayError(list).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Progress>() {
                            @Override
                            public void onSubscribe(Subscription s) {
                                subscription = s;
                                s.request(Long.MAX_VALUE);
                                downloadStartTime = System.currentTimeMillis();
                                Log.d("testDownload", "onSubscribe");
                            }

                            @Override
                            public void onNext(Progress progress1) {
                                long totalSize = 0;
                                long downloadSize = 0;
                                progressMap.put(progress1.getDownloadUrl() + "", progress1);
                                for (Progress progress : progressMap.values()) {
                                    totalSize += progress.getTotalSize();
                                    downloadSize += progress.getDownloadSize();
                                }
                                long currentTime = System.currentTimeMillis();
                                long downtime = currentTime - downloadStartTime;
                                double rate = (double) downloadSize / ((double) downtime == 0 ? 1 : (double) downtime);
                                BigDecimal b = new BigDecimal(rate);
                                rate = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                if (downtime > START_TIME && rate > maxRate) {
                                    maxRate = rate;
                                    maxRateCurrentTime = currentTime;
                                }
                                if (maxRate / rate > RATE_TIMES && currentTime - maxRateCurrentTime <= PERIOD && recount < RECONNECT_COUNT) {
                                    recount++;
                                    subscription.cancel();
                                    Log.d("testDownload recount", recount + "");
                                    download(urls);
                                }
                                Log.d("testDownload Progress", percent(downloadSize, totalSize) + "%" + " , size : " + downloadSize + " , time : " + downtime + " , rate : " + rate);
                            }

                            @Override
                            public void onError(Throwable t) {
                                subscription.cancel();
                                Log.d("testDownload", t.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                subscription.cancel();
                                Log.d("testDownload", "onComplete : " + (System.currentTimeMillis() - totalTime));
                            }
                        });
                    }
                });
    }

    public void stop(View view) {
//        if (disposable != null && !disposable.isDisposed()) {
//            disposable.dispose();
//        }
        if (subscription != null) {
            subscription.cancel();
        }
    }

    public void delete(View view) {
//        RxDownloadKt.delete(url);//RxDownloadKt.delete(new Task(),);
        deleteAll();
    }

    /**
     * 删除所有的记录，文件
     */
    public void deleteAll() {
        SharedPreferences sp = getSharedPreferences("rxdownload_simple_storage", Context.MODE_PRIVATE);
        Map<String, ?> all = sp.getAll();
        if (all != null) {
            for (Object o : all.values()) {
                String[] value = o.toString().split("\n");
                if (value.length >= 2) {
                    String name = value[1];
                    String dir = value[2];
                    if (!deleteFiles(dir + "/" + name)) {
                        deleteFiles(dir + "/" + name + ".download");
                        deleteFiles(dir + "/" + name + ".tmp");
                    }
                }
            }
            sp.edit().clear().apply();
        }
    }

    public boolean deleteFiles(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    private void startAnimation(View view) {
        //因为CircularReveal动画是api21之后才有的,所以加个判断语句,免得崩溃
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cicular_R = view.getHeight() / 2 > view.getWidth() / 2 ? view.getHeight() / 2 : view.getWidth() / 2;
            Animator animator = ViewAnimationUtils.createCircularReveal(view, (int) view.getWidth() / 2, (int) view.getHeight() / 2, 0, cicular_R);

            animator.setDuration(1000);
            animator.start();
        } else {
            Toast.makeText(this, "SDK版本太低,请升级", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Return percent number.
     */
    public Double percent(Long downloadSize, Long totalSize) {
        if (totalSize <= 0) {
            return 0.0;
        } else {
            BigDecimal result = new BigDecimal(downloadSize * 100.0).divide(new BigDecimal(totalSize * 1.0), 2, BigDecimal.ROUND_HALF_UP);
            return result.doubleValue();
        }
    }
}
