package pandamusicplayer.the.muisc;

import pandamusicplayer.the.muisc.MusicService.CommandReceiver;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;



public class FashionMusicActivity extends Activity {
	
	 /**
     *������Ļ�����¼���
     */
    private Thread mSplashThread;    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = getSharedPreferences("service", 0);
		if (sp.getBoolean("isStart", false)) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		} else {
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.welcome);
			// ��������������߳�
	        mSplashThread =  new Thread(){
	            @Override
	            public void run(){
	                try {
	                    synchronized(this){
	                        // ������ͣ��ʱ��
	                        wait(2800);
	                    }
	                }
	                catch(InterruptedException ex){                    
	                }

	                finish();
	                
	                //������һ��Activity
	                startActivity(new Intent(FashionMusicActivity.this,
							MainActivity.class));
					finish();
	                try{
	                	stop();   
	                }catch(Exception e){
	                	System.out.println(e);
	                }
	            }
	        };
	        
	        mSplashThread.start();

		}
	}
	   /**
	    *������Ļ�����¼�
	    *�����û�����һ��APP��ͼ��ʱ���㻽�������߳�
	    */
	   @Override
	   public boolean onTouchEvent(MotionEvent evt)
	   {
	       if(evt.getAction() == MotionEvent.ACTION_DOWN)
	       {
	           synchronized(mSplashThread){
	               mSplashThread.notifyAll();
	           }
	       }
	       return true;
	   }
}
