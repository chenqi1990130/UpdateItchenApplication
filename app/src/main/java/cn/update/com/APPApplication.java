package cn.update.com;

import android.app.Application;

import org.xutils.common.util.LogUtil;
import org.xutils.x;

/**
 * Created by itchenqi on 2017/10/20 0020.
 * description：
 */

public class APPApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        x.Ext.init(this);
        x.Ext.setDebug(true);
         //x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
        LogUtil.customTagPrefix="itchen";// 方便调试时过滤 adb logcat 输出


    }
}
