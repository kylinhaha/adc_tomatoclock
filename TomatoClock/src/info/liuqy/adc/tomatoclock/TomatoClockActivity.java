package info.liuqy.adc.tomatoclock;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TomatoClockActivity extends Activity {

	private boolean mBindFlg = false;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	static final int VIBRATE_ACTION = 0;
    public static final long TWENTYFIVE_MIN = 10*1000; //only for class demo. real: 25*60*1000;

	private TimerUpdateForegroundService srv = null;
	private ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			srv = ((TimerUpdateForegroundService.MyBinder)binder).getService();
		}
		public void onServiceDisconnected(ComponentName className) {
			srv = null;
		}
	};
   
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    private Handler updateTimerHandler = new Handler() {

		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Button clockBtn = (Button)TomatoClockActivity.this.findViewById(R.id.start_timer);
			//int secs = (Integer)msg.obj;
			long secs = srv.getTimeElapsed();
			int min = (int) secs/60;
			int sec = (int) secs - min*60;
			clockBtn.setText(String.format("%1$02d:%2$02d", min, sec));
		}
    	
    };

    public void startTimer(View v) {
    	    	
        Intent i = new Intent(this, TomatoVibrator.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                this.getApplicationContext(), VIBRATE_ACTION, i, 0);
        AlarmManager am = (AlarmManager)this.getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TWENTYFIVE_MIN, pi);
        Toast.makeText(this, "TomatoTimer started!", Toast.LENGTH_SHORT).show();      

        Intent service = new Intent(this, TimerUpdateForegroundService.class);
        service.putExtra(TimerUpdateForegroundService.UPDATE_MSGR, new Messenger(updateTimerHandler));
        if (mBindFlg) {
        	unbindService(conn);
        	stopService(service);
        }
    	bindService(service, conn, Context.BIND_AUTO_CREATE);
    	startService(service);
    	mBindFlg =true;
    }
}