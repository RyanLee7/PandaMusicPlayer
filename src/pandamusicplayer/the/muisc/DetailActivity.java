package pandamusicplayer.the.muisc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pandamusicplayer.the.domain.Music;
import pandamusicplayer.the.muisc.MusicService.CommandReceiver;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity {
	
	//��ʾ���
	private Button Btn_back;   
	private SeekBar seekBar;
	private TextView tv_Text_Current;
	private TextView tv_Text_Duration; 
	private TextView tv_Song_Name;
	private TextView tv_Singer;
	private ImageButton tv_Play;
	private ImageButton tv_Previous;
	private ImageButton tv_Next;
	
	//��ǰ��������ţ���1��ʼ
	private int number;
	
	//����״̬
    private int status;
    
	//���Music�����Ա�
	private List<Music> list_Music;
   
	//���½�������Handler
	private Handler seekBarHandler;
	//��ǰ�����ĳ���ʱ��͵�ǰλ�ã������ڽ�����
	private int duration;
	private int time;
	
	//�����������Ŀ
	private int sumMuisc;
	
	//�㲥������
  	private StatusChangedReceiver receiver;
	
	//���������Ƴ���
	private static final int PROGRESS_INCREASE = 0;
	private static final int PROGRESS_PAUSE = 1;
	private static final int PROGRESS_RESET = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//�Զ����������ʾ������ʾUI
		DetailTitleBar. getTitleBar(this,""); 
		setContentView(R.layout.playlayout);
		//��ȡ��ʾ���
		findViews();
		//Ϊ��ʾ���ע�������
		registerListeners();
		//��鲥�����Ƿ����ڲ��ţ�������ڲ��ţ����ϰ󶨵Ľ�������ı�UI
		sendBroadcastOnCommand(MusicService.COMMAND_CHECK_IS_PLAYING);
		//�󶨹㲥������
		bindStatusChangedReceiver();
		//��ʼ��������
		intitSeekBarHandler();
	}
	
	//�󶨹㲥������
		private void bindStatusChangedReceiver(){
			receiver = new StatusChangedReceiver();
			IntentFilter filter = new IntentFilter(
					MusicService.BROADCAST_MUSICSERVICE_UPDATE_STATUS);
			registerReceiver(receiver,filter);
		}
	
	//��ȡ��ʾ���
		private void findViews(){
			Btn_back = (Button)findViewById(R.id.btnback);
			tv_Text_Current = (TextView)findViewById(R.id.tv_Text_Current);
			tv_Text_Duration =  (TextView)findViewById(R.id.tv_Text_Duration);
			tv_Song_Name =  (TextView)findViewById(R.id.tv_songname);
			tv_Singer =  (TextView)findViewById(R.id.tv_singer);
			tv_Play = (ImageButton)findViewById(R.id.tv_play);
			tv_Previous = (ImageButton)findViewById(R.id.tv_previous);
			tv_Next = (ImageButton)findViewById(R.id.tv_next);
			seekBar = (SeekBar)findViewById(R.id.my_seekbar);
		}

		//Ϊ��ʾ���ע�������
		private void registerListeners(){
			Btn_back.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.setClass(DetailActivity.this,MainActivity.class);
					startActivity(intent);
				   CustomTitleBar.number  = number;
				}
			});
			tv_Previous.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					//������һ���������Service
					sendBroadcastOnCommand(MusicService.COMMAND_PREVIOUS);
					seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
				}
			});
			tv_Play.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					//���Ͳ��Ż���ͣ�������Service
					if(isPlaying()){
						sendBroadcastOnCommand(MusicService.COMMAND_PAUSE);
					}else if(isPaused()){
						sendBroadcastOnCommand(MusicService.COMMAND_RESUME);
					}else if(isStopped()){
						sendBroadcastOnCommand(MusicService.COMMAND_PLAY);
						seekBarHandler.sendEmptyMessage(PROGRESS_INCREASE);
					}
				}
			});
			tv_Next.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					//������һ���������Service
					sendBroadcastOnCommand(MusicService.COMMAND_NEXT);
					seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
				}
			});
		}
		
		//�Ƿ����ڲ�������
		private boolean isPlaying(){
			return status == MusicService.STATUS_PLAYING;
		}
		
		//�Ƿ���ͣ��������
		private boolean isPaused(){
			return status == MusicService.STATUS_PAUSED;
		}
		
		//�Ƿ�ֹͣ״̬
		private boolean isStopped(){
			return status == MusicService.STATUS_STOPPED;
		}
		
		//��������������ֲ��š�����������MusicService����
	    private void sendBroadcastOnCommand(int command){
	    	Intent intent = new Intent(MusicService.BROADCAST_MUSICSERVICE_CONTROL);
	    	intent.putExtra("command", command);
	    	//���ݲ�ͬ�����װ��ͬ����
	    	switch (command) {
	    	case MusicService.COMMAND_PLAY:
	    		intent.putExtra("number", number);
	    		break;
	    	case MusicService.COMMAND_PREVIOUS:
	    		moveNumberToPrevious();
	    		intent.putExtra("number", number);
	    		break;
	    	case MusicService.COMMAND_NEXT:
	    		moveNumberToNext();
	    		intent.putExtra("number", number);
	    		break;
	    	case MusicService.COMMAND_SEEK_TO:
	    		intent.putExtra("time", time);
	    		break;
	    	case MusicService.COMMAND_PAUSE:
	    	case MusicService.COMMAND_STOP:
	    	case MusicService.COMMAND_RESUME:
	    	default:
	    			break;
	    	}
	    	sendBroadcast(intent);
	    }
	    
	    @Override
		protected void onResume(){
			super.onResume();	 
			number = CustomTitleBar.number ;
			//Toast.makeText(this, ""+number, Toast.LENGTH_SHORT).show();
			//��ʼ������Ϊ0
			sumMuisc = 0;
			//ȡ�������б�
			GetMusicCursor();
			sendBroadcastOnCommand(MusicService.COMMAND_TELL_ME_NUMBER);
		}
	    
	  //��ȡϵͳɨ��õ�������ý�弯
		private void GetMusicCursor(){
			 list_Music = new ArrayList<Music>();
			//��ȡ����ѡ����
			ContentResolver resolver = getApplication().getContentResolver();
			//ѡ������ý�弯
			Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			if (cursor.moveToFirst()) {
				do {
					Music m = new Music();
					String title = cursor.getString(cursor
							.getColumnIndex(MediaStore.Audio.Media.TITLE));
					String singer = cursor.getString(cursor
							.getColumnIndex(MediaStore.Audio.Media.ARTIST));
					if ("<unknown>".equals(singer)) {
						singer = "δ֪������";
					}
					String album = cursor.getString(cursor
							.getColumnIndex(MediaStore.Audio.Media.ALBUM));
					long size = cursor.getLong(cursor
							.getColumnIndex(MediaStore.Audio.Media.SIZE));
					long time = cursor.getLong(cursor
							.getColumnIndex(MediaStore.Audio.Media.DURATION));
					String url = cursor.getString(cursor
							.getColumnIndex(MediaStore.Audio.Media.DATA));
					m.setTitle(title);
					m.setSinger(singer);
					m.setAlbum(album);
					m.setSize(size);
					m.setTime(time);
					m.setUrl(url);
					list_Music.add(m);
					sumMuisc++;
				} while (cursor.moveToNext());
			}
		}
		
		//�ƶ�����һ��
		private void moveNumberToNext(){
			//�ж��Ƿ񵽴����б�Ͷ�
			if((number +1 ) >= sumMuisc){
				number = 0;
			} else {
				++number;
			}
		}
		
		//�ƶ�����һ��
		private void moveNumberToPrevious(){
			//�ж��Ƿ񵽴����б���
			if((number ==0) ){
				number = sumMuisc-1;
			} else {
				--number;
			}
		}
		
		//��ʽ�������� --> "mm:ss"
	    private String formatTime(int msec){
	    	int minute = (msec / 1000) / 60;
	    	int second = (msec / 1000) % 60;
	    	String minuteString;
	    	String secondString;
	    	if (minute < 10) {
	    		minuteString = "0" + minute;
	    	} else {
	    		minuteString = "" + minute;
	    	}
	    	if (second < 10) {
	    		secondString = "0" + second;
	    	} else {
	    		secondString = "" + second;
	    	}
	    	return minuteString + ":" + secondString;
	    }
	    
	  //��ʼ��������
	    private void intitSeekBarHandler(){
	    	seekBarHandler = new Handler(){
	    		public void handleMessage(Message msg){
	    			super.handleMessage(msg);
	    			switch (msg.what) {
	    			case PROGRESS_INCREASE:
	    				if (seekBar.getProgress() < duration) {
	    					//������ǰ��1��
	    					seekBar.incrementProgressBy(1000);
	    					seekBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
	    					//�޸���ʾ��ǰ���ȵ��ı�
	    					tv_Text_Current.setText(formatTime(time));
	    					time += 1000;
	    				}
	    				break;
	    			case PROGRESS_PAUSE:
	    				seekBarHandler.removeMessages(PROGRESS_INCREASE);
	    				break;
	    			case PROGRESS_RESET:
	    				//���ý���������
	    				seekBarHandler.removeMessages(PROGRESS_INCREASE);
	    				seekBar.setProgress(0);
	    				tv_Text_Current.setText("00:00");
	    				break;
	    			}
	    		}
	    	};
	    }
	    
	  //�ڲ��࣬���ڲ�����״̬���µĽ��չ㲥
	    class StatusChangedReceiver extends BroadcastReceiver{
			@Override
			public void onReceive(Context context, Intent intent) {
				//��ȡ������״̬
				status = intent.getIntExtra("status", -1);
				//������Ž����ˣ�������һ��
				switch (status){
				case MusicService.STATUS_PLAYING:
					time = intent.getIntExtra("time", 0);
					duration = intent.getIntExtra("duration", 0);
					seekBarHandler.removeMessages(PROGRESS_INCREASE);
					seekBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
					seekBar.setMax(duration);
					seekBar.setProgress(time);
					Music m = list_Music.get(number);
					tv_Song_Name.setText(m.getTitle());
					tv_Text_Duration.setText(formatTime(duration));
					tv_Play.setImageDrawable(getResources().getDrawable(R.drawable.desktop_pausebt_b));
					break;
				case MusicService.STATUS_PAUSED:
					seekBarHandler.sendEmptyMessage(PROGRESS_PAUSE);
					tv_Play.setImageDrawable(getResources().getDrawable(R.drawable.desktop_playbt_b));
					break;
				case MusicService.STATUS_COMPLETED:
					seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
					sendBroadcastOnCommand(MusicService.COMMAND_NEXT);
					tv_Play.setImageDrawable(getResources().getDrawable(R.drawable.desktop_playbt_b));
					break;
				case MusicService.RETURNBACK:
					number = intent.getIntExtra("number", 0);
					Music m1 = list_Music.get(number);
				    tv_Song_Name.setText(m1.getTitle());
				    tv_Singer.setText(m1.getSinger());
				    break;
				default:
					break;
				}
				updateUI(status);
			}
			//���ݲ������Ĳ���״̬������UI
		    private void updateUI(int status){
		    	switch(status){
		    	case MusicService.STATUS_PLAYING:
		    		tv_Play.setImageDrawable(getResources().getDrawable(R.drawable.desktop_pausebt_b));
		    		break;
		    	case MusicService.STATUS_PAUSED:
		    	case MusicService.STATUS_STOPPED:
		    	case MusicService.STATUS_COMPLETED:
		    		tv_Play.setImageDrawable(getResources().getDrawable(R.drawable.desktop_playbt_b));
		    		break;
		    	default:
		    		break;
		    	}
		    }
	    }
	
}
