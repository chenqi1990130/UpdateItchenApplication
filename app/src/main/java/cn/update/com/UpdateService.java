package cn.update.com;

import android.app.Service;
import android.content.Intent;
//import android.icu.text.NumberFormat;
import android.os.IBinder;
//import android.support.annotation.IntDef;
import android.text.TextUtils;

import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.NumberFormat;

/**
 * 后台下载apk代码内容
 */
public class UpdateService extends Service {

    //发送广播的意图
    private Intent intent;
    private NumberFormat numberFormat;

    //这个内容是之前建立的下载的http交互内容。
    public static Callback.Cancelable downLoadHandler;

    //http://www.cnblogs.com/mengdd/archive/2013/03/24/2979944.html

    //标志的作用？？？
    private boolean isBegin = false;


    public UpdateService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtil.d("itchen----------onCreate");

        //采用Context.startService()方法启动服务，在服务未被创建时，
        // 系统会先调用服务的onCreate()方法，接着调用onStart()方法。
        // 如果调用startService()方法前服务已经被创建，
        // 多次调用startService()方法并不会导致多次创建服务，
        // 但会导致多次调用onStart()方法。采用startService()方法启动的服务，
        // 只能调用Context.stopService()方法结束服务，服务结束时会调用onDestroy()方法。


        //发送广播的意图 先创建
        intent = new Intent();

        //数字格式化实例对象  getInstance是得到当前语言默认的格式化格式
         numberFormat  =  NumberFormat.getInstance();//NumberFormat.getPercentInstance();
         //numberFormat.setMaximumFractionDigits(3); //格式化输出为2个小数点之后
         numberFormat.setMaximumFractionDigits(2); //格式化输出为2个小数点之后


        //下载存放的路径在
        //10-20 13:49:36.144 22899-22899/com.ycb.www.update I/tag: /storage/emulated/0/Android/data/com.ycb.www.update/cache/xUtils_cache/f708db703f4541f5c445f8a1e431fb61
        //10-20 13:49:36.154 22899-22899/com.ycb.www.update I/tag: onFinished

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String downUrl = intent.getStringExtra("downUrl");//下载地址内容
        LogUtil.d("itchen--onStartCommand+downloadUrl-->"+downUrl);
        //itchen--onStartCommand+downloadUrl-->http://www.ycb.com/m/YcbAndroid2.0.5.apk

        if (TextUtils.isEmpty(downUrl)){

            stopSelf();

            return START_STICKY_COMPATIBILITY;//这个数值等于0
        }

        if (isBegin){
            return START_STICKY_COMPATIBILITY;
        }else{
            isBegin = true;
        }

        downLoad(downUrl);

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtil.d("itchen---服务终止--onDestroy");

    }

    private void downLoad(final String _downUrl) {

        RequestParams requestParams = new RequestParams(_downUrl);//一个普通的get请求。

        //任何东西不携带，只是做纯粹的下载功能
        downLoadHandler = x.http().get(requestParams, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {

                //数据成功下载
                //下载完成立马发送一个数据下载完成的消息
                intent.setAction("com.ycb.www.complete");
                intent.putExtra("filepath", result.getAbsolutePath());

                sendBroadcast(intent);

                stopSelf();//此处代码说明是可以终止服务的。

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

                LogUtil.d("itchen--------onError");

                intent.setAction("com.ycb.www.failed");//下载失败
                intent.putExtra("downUrl", _downUrl);

                sendBroadcast(intent);



            }

            @Override
            public void onCancelled(CancelledException cex) {

                LogUtil.d("itchen--onCancelled");
                stopSelf();

            }

            @Override
            public void onFinished() {
                //Http流程完毕
                LogUtil.d("itchen--onFinished");

                //ased: /storage/emulated/0/Android/data/cn.update.com/cache/xUtils_cache/f708db703f4541f5c445f8a1e431fb61:27746

                stopSelf();//这个也可以终止服务内容

            }

            @Override
            public void onWaiting() {
                LogUtil.d("itchen--onWaiting");
            }

            @Override
            public void onStarted() {

                LogUtil.d("itchen--onStarted");
                //发送一个下载广播 创建一个通知栏
                intent.putExtra("rate",0);
                intent.setAction("com.ycb.www.updating");

                sendBroadcast(intent);

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

                //LogUtil.d("itchen--全部的进度-->"+total);

                Double rate = (double)current/(double)total;
                String format = numberFormat.format(rate);
                //LogUtil.d("itchen---format=="+format);

                int r = (int) (Double.valueOf(format)*100);

                //LogUtil.d("itchen--下载的百分比内容是:"+r);

                intent.putExtra("rate",r);
                intent.setAction("com.ycb.www.updating");

                sendBroadcast(intent);

            }
        });




    }
}
