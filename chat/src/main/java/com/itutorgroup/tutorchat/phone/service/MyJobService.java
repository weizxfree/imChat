package com.itutorgroup.tutorchat.phone.service;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.utils.kernel.Kernel;

/**
 * Created by joyinzhao on 2016/11/4.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MyJobService extends JobService {
    @Override
    public void onCreate() {
        super.onCreate();
        startJobScheduler();
    }

    public void startJobScheduler() {
        try {
            int id = 1;
            JobInfo.Builder builder = new JobInfo.Builder(id,
                    new ComponentName(getPackageName(), MyJobService.class.getName()));
            builder.setPeriodic(500);  //间隔500毫秒调用onStartJob函数， 500只是为了验证
            JobScheduler jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int ret = jobScheduler.schedule(builder.build());
            // Android24版本才有scheduleAsPackage方法
//            Class clz = Class.forName("android.app.job.JobScheduler");
//            Method[] methods = clz.getMethods();
//            Method method = clz.getMethod("scheduleAsPackage", JobInfo.class, String.class, Integer.class, String.class);
//            Object obj = method.invoke(jobScheduler, builder.build(), "com.brycegao.autostart", "brycegao", "test");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Kernel.getInstance().startTcpService(LPApp.getInstance());
        jobFinished(jobParameters, true);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
