package cn.update.com;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import org.xutils.common.util.LogUtil;

import java.io.File;

/**
 * Created by itchenqi on 2017/10/20 0020.
 * description：创建通知栏的广播提示内容
 */

public class UpdateReceiver extends BroadcastReceiver {

    private NotificationManager manager;

    private RemoteViews views;

    private Notification notification;

    @Override
    public void onReceive(Context context, Intent intent) {


        //可能会导致图标闪烁的内容

        LogUtil.d("itchen--收到onReceive==action=="+intent.getAction());

        if (notification == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)

                initNotification(context);//如果系统的版本大于android4.1 那么

            else {
                initNotificationForLowVersion(context);
            }

        }

        String action = intent.getAction();//标识判别

        switch (action) {
            case "com.ycb.www.cancel":

                //关闭内容
                manager.cancel(0);//标识是0???
                UpdateService.downLoadHandler.cancel();//取消网络请求内容
                break;


            case "com.ycb.www.failed"://下载失败

                intent.setAction("com.ycb.www.restart");//在这里重新发起一个请求 触发重新下载

                PendingIntent failedpendingIntent = PendingIntent.getBroadcast(context, 200, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                views.setOnClickPendingIntent(R.id.ll_content, failedpendingIntent);
                views.setTextViewText(R.id.tv_info, "下载失败,点击重试");
                manager.notify(0, notification);// 重新发起一个请求


                break;

            case "com.ycb.www.restart":

                manager.cancel(0);//将原有的取消，重新走启动流程

                intent.setClass(context, UpdateService.class);
                context.startService(intent);

                break;

            case "com.ycb.www.install":

                manager.cancel(0);//将原有的取消之后启用安装程序

                Intent startInstall = new Intent();
                startInstall.setAction(Intent.ACTION_VIEW);
                String filepath = intent.getStringExtra("filepath");
                startInstall.setDataAndType(Uri.fromFile(new File(filepath)), "application/vnd.android.package-archive");
                startInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startInstall);

                break;

            case "com.ycb.www.complete":

                intent.setAction("com.ycb.www.install");

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 200, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                views.setOnClickPendingIntent(R.id.ll_content, pendingIntent);
                views.setTextViewText(R.id.tv_info, "下载完成,点击安装");
                views.setProgressBar(R.id.progressBar, 100, 100, false);

                manager.notify(0, notification);//下载完成的时候 仍然在展示


                break;

            case "com.ycb.www.updating":

                int rate = intent.getIntExtra("rate", 0);
                views.setTextViewText(R.id.tv_info, "正在下载...." + rate + "%");
                LogUtil.d("itchen---正在更新----->" + rate + "%");

                views.setProgressBar(R.id.progressBar, 100, rate, false);
                manager.notify(0, notification);//一直在更新notification

                break;

            default:
                break;
        }


    }

    private void initNotificationForLowVersion(Context context) {

        //两次都要创建一个NotificationManager 管理器
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //设置notifiction布局
        views = new RemoteViews(context.getPackageName(), R.layout.notification_update);
        notification = new Notification();

        notification.when = System.currentTimeMillis();

        notification.tickerText = "低版本的布局版正在下载";
        //设置view
        notification.contentView = views;
        //设置小图标
        notification.icon = R.drawable.ic_launcher;

        //设置布局文件中的textView的内容
        views.setTextViewText(R.id.tv_info, "下载中....0%");

        //设置布局文件中的ProgressBar进度
        views.setProgressBar(R.id.progressBar, 100, 0, false);

        //退出的intent
        Intent intent = new Intent("com.ycb.www.cancel");
        //退出的延迟意图
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 200, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //点击之后退出
        views.setOnClickPendingIntent(R.id.ib_close, mPendingIntent);

    }

    private void initNotification(Context context) {

        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        views = new RemoteViews(context.getPackageName(), R.layout.notification_update);

        Notification.Builder builder = new Notification.Builder(context);

        notification = builder.setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)//这样设置大图标 解决图标闪烁的问题
                //smallicon不添加的话 则手机内有的不展示图标
                //.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_launcher))
                .setContentText("下载中")
                .setContentTitle("下载")
                .setWhen(System.currentTimeMillis())
                .setTicker("4.1以上版本新版本正在下载")// //系统收到通知时，通知栏上面滚动一次弹出显示的文字。
                .setContent(views)
                .build();

        //设置布局内容
        views.setTextViewText(R.id.tv_info, "下载中....0%");
        views.setProgressBar(R.id.progressBar, 100, 0, false);//如果进度条是不确定的为true，反之为false 最后一个参数


        //创建将要执行的意图操作
        Intent intent = new Intent();
        intent.setAction("com.ycb.www.cancel");//点击是取消？？

        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext()
                , 200, intent, PendingIntent.FLAG_UPDATE_CURRENT);//pendingintent的
        //如果PendingIntent已经存在，保留它并且只替换它的extra数据

        //设置views中的按钮的点击事件 这里指的是叉子的功能
        views.setOnClickPendingIntent(R.id.ib_close, mPendingIntent);//这个也是一个延迟意图操作

        /*最经常使用的是FLAG_UPDATE_CURRENT，因为描述的Intent有 更新的时候需要用到这个flag去更新你的描述，否则组件在下次事件发生或时间到达的时候extras永远是第一次Intent的extras。
        上面4个flag中最经常使用的是FLAG_UPDATE_CURRENT，因为描述的Intent有 更新的时候需要用到这个flag去更新你的描述，否则组件在下次事件发生或时间到达的时候extras永远是第一次Intent的extras。
        使用 FLAG_CANCEL_CURRENT也能做到更新extras，只不过是先把前面的extras清除，另外FLAG_CANCEL_CURRENT和 FLAG_UPDATE_CURRENT的区别在于能否新new一个Intent，FLAG_UPDATE_CURRENT能够新new一个 Intent，
        而FLAG_CANCEL_CURRENT则不能，只能使用第一次的Intent。*/


    }
}
