package cn.dhl.ipcserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import cn.dhl.aidl.ITestAidlInterface;

/**
 * Author: duanhl
 * Create: 2019-11-27 16:44
 * Description:
 */
public class AidlDemoService extends Service {

    private ITestAidlInterface.Stub mIBinder = new ITestAidlInterface.Stub() {
        @Override
        public String basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
            return "String from server";
        }

        @Override
        public City changeCityName(City city) throws RemoteException {
            City ci = new City();
            ci.name = city.name+"_addByServer";
            return ci;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("dhl.AidlDemoService", "onCreate");
        Toast.makeText(getApplicationContext(), "AidlDemoService onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i("dhl.AidlDemoService", "onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("dhl.AidlDemoService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("dhl.AidlDemoService", "onBind:"+mIBinder);
        return mIBinder;
    }
}
