package pandamusicplayer.the.muisc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pandamusicplayer.the.domain.Music;
import pandamusicplayer.the.muisc.MusicService.CommandReceiver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	//��ʾ���
	private ImageButton imgBtn_Previous;        //��һ��
	private ImageButton imgBtn_PlayOrPause; //��ʼ����ͣ
	private ImageButton imgBtn_Stop;             //ֹͣ
	private ImageButton imgBtn_Next;             //��һ��
	private ListView list;
	private TextView text_Current;
	private TextView text_Duration; 
	private TextView song_Name;
	private Button upDetailBtn;
	//private SeekBar seekBar;
	private ProgressBar progressBar;
	//���½�������Handler
	//private Handler seekBarHandler;
	private Handler progressBarHandler;
	//��ǰ�����ĳ���ʱ��͵�ǰλ�ã������ڽ�����
	private int duration;
	private int time;
	//���������Ƴ���
	private static final int PROGRESS_INCREASE = 0;
	private static final int PROGRESS_PAUSE = 1;
	private static final int PROGRESS_RESET = 2;
	//���Music�����Ա�
	private List<Music> list_Music;
	//��ǰ��������ţ���1��ʼ
	private int number;
	//player
	private MediaPlayer player;
	//����״̬
    private int status;
    //�㲥������
  	private StatusChangedReceiver receiver;
  	//��־λ
  	boolean flag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//�Զ����������ʾ������ʾUI
		//CustomTitleBar. setWelcomePage(this);
		CustomTitleBar. getTitleBar(this,"ȫ������"); 
		setContentView(R.layout.main);
		//��ʼ�����ŵĸ������
		number = 0;
		//��ʼ����Ȼ���ֲ�������״̬
		status = MusicService.STATUS_STOPPED;
		//��ʼ�����ý������Ľ���
		time = 0;
		duration = 0;
		//��ȡ��ʾ���
		findViews();
		//Ϊ��ʾ���ע�������
		registerListeners();
		 //��ʼService
		startService(new Intent(this, MusicService.class));
		//�󶨹㲥������
		bindStatusChangedReceiver();
		//��鲥�����Ƿ����ڲ��ţ�������ڲ��ţ����ϰ󶨵Ľ�������ı�UI
		sendBroadcastOnCommand(MusicService.COMMAND_CHECK_IS_PLAYING);
		//��־λ
		flag = false;
		//��ʼ��������
		//intitSeekBarHandler();
		intitProgressBarHandler();
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
		imgBtn_Previous = (ImageButton)findViewById(R.id.btnPrevious_player);
		imgBtn_PlayOrPause = (ImageButton)findViewById(R.id.btnPlay_player);
		imgBtn_Next = (ImageButton)findViewById(R.id.btnNext_player);
		list = (ListView)findViewById(R.id.listView1);
		text_Current = (TextView)findViewById(R.id.list_song_current);
		text_Duration =  (TextView)findViewById(R.id.list_song_duration);
		//seekBar = (SeekBar)findViewById(R.id.progressBar1);	
		progressBar = (ProgressBar)findViewById(R.id.progressBar1);
		song_Name = (TextView)findViewById(R.id.list_song_name);
		upDetailBtn = (Button) findViewById(R.id.updetail);
	}

	//Ϊ��ʾ���ע�������
	private void registerListeners(){
		imgBtn_Previous.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//������һ���������Service
				sendBroadcastOnCommand(MusicService.COMMAND_PREVIOUS);
				if(flag == false) flag = true;
				//seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
				progressBarHandler.sendEmptyMessage(PROGRESS_RESET);
			}
		});
		imgBtn_PlayOrPause.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//���Ͳ��Ż���ͣ�������Service
				if(isPlaying()){
					sendBroadcastOnCommand(MusicService.COMMAND_PAUSE);
					if(flag == false) flag = true;
				}else if(isPaused()){
					sendBroadcastOnCommand(MusicService.COMMAND_RESUME);
					if(flag == false) flag = true;
				}else if(isStopped()){
					sendBroadcastOnCommand(MusicService.COMMAND_PLAY);
					if(flag == false) flag = true;
					progressBarHandler.sendEmptyMessage(PROGRESS_INCREASE);
				}
			}
		});
		imgBtn_Next.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//������һ���������Service
				sendBroadcastOnCommand(MusicService.COMMAND_NEXT);
				if(flag == false) flag = true;
				//seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
				progressBarHandler.sendEmptyMessage(PROGRESS_RESET);
			}
		});
		list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(flag == false) flag = true;
				number = arg2;
				sendBroadcastOnCommand(MusicService.COMMAND_PLAY);
				//seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
				progressBarHandler.sendEmptyMessage(PROGRESS_RESET);
			}
		});
		upDetailBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this,DetailActivity.class);
				startActivity(intent);
				CustomTitleBar.number = number;
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
	
	//���ò��ű�־
	private void setViewVisibility(int number){
		for(int i=0;i<list.getCount();i++)
		{
			TextView music_line=(TextView)list.getChildAt(i).findViewById(R.id.divider_line);
			if(i != number){
				music_line.setVisibility(View.INVISIBLE);
			} else{	
				music_line.setVisibility(View.VISIBLE);
			}
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		number = CustomTitleBar.number;
		//Toast.makeText(this,""+number, Toast.LENGTH_SHORT).show();
		sendBroadcastOnCommand(MusicService.COMMAND_TELL_ME_NUMBER);
		//���Ի������б�
		initMusicList();
		//����б�û�и������򲥷Ű�ť�����ã��������û�
		if(list.getCount() == 0){
			imgBtn_Previous.setEnabled(false);
			imgBtn_PlayOrPause.setEnabled(false);
			//imgBtn_Stop.setEnabled(false);
			imgBtn_Next.setEnabled(false);
			Toast.makeText(this, this.getString(R.string.tip_no_music_file), Toast.LENGTH_SHORT).show();
		}else{
			imgBtn_Previous.setEnabled(true);
			imgBtn_PlayOrPause.setEnabled(true);
			//imgBtn_Stop.setEnabled(true);
			imgBtn_Next.setEnabled(true);
		}
	}
	
	//��ʼ�������б�������ȡ���ּ��ϸ�����ʾ�б�
	private void initMusicList(){
		GetMusicCursor();
		setListContent();
	}
	
	//�����б�����
	private void setListContent(){
    	MusicItemAdapter adapter=new MusicItemAdapter();
    	list.setAdapter(adapter);
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
			} while (cursor.moveToNext());
		}
	}
	
	//�ƶ�����һ��
	private void moveNumberToNext(){
		//�ж��Ƿ񵽴����б�Ͷ�
		if((number +1 ) >= list.getCount()){
			number = 0;
			Toast.makeText(MainActivity. this,
					MainActivity.this.getString(R.string.tip_reach_bottom) ,
					Toast.LENGTH_SHORT).show();
		} else {
			++number;
		}
	}
	
	//�ƶ�����һ��
	private void moveNumberToPrevious(){
		//�ж��Ƿ񵽴����б���
		if((number ==0) ){
			number = list.getCount()-1;
			Toast.makeText(MainActivity. this,
					MainActivity.this.getString(R.string.tip_reach_top) ,
					Toast.LENGTH_SHORT).show();
		} else {
			--number;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//��ȡ�����ļ�
	public void load(int number){
		//֮ǰ����Դ�����ˣ��ͷŵ�
		if(player != null){
			player.release();
			player = null;
		}
		Music m = list_Music.get(number);
		String url = m.getUrl();
		Uri myUri = Uri.parse(url);

		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		try {
			player.setDataSource(getApplicationContext(), myUri);
			player.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//��������
	public void play(int number){
		//ֹͣ��ǰ����
		if(player.isPlaying()){
			player.stop();
		}
		load(number);
		player.start();
	}

	//��ͣ����
	public void pause(){
		if(player.isPlaying()){
			player.pause();
		}
	}
	
	//ֹͣ��������
	public void stop(){
		if(player != null){
			player.stop();
		}
	}
	
	//�ָ����ţ���ͣ��
	public void resume(){
		player.start();
	}
	
	//���²��ţ�������ɺ�
	public void replay(){
		player.start();
	}
	
	 private class MusicItemAdapter extends BaseAdapter{
			@Override
			public int getCount() {
				return list_Music.size();
			}
			@Override
			public Object getItem(int arg0) {
				return list_Music.get(arg0);
			}
			@Override
			public long getItemId(int position) {
				return position;
			}
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				if(convertView==null){
					convertView=LayoutInflater.from(getApplicationContext()).inflate(R.layout.musiclist, null);
				}
				Music m=list_Music.get(position);
				TextView textName=(TextView) convertView.findViewById(R.id.music_list_singer);
				textName.setText(m.getSinger());
				TextView music_singer=(TextView) convertView.findViewById(R.id.music_list_name);
				music_singer.setText(m.getTitle());
				TextView music_time=(TextView) convertView.findViewById(R.id.music_list_time);
				music_time.setText(getDateToString(m.getTime()));
				TextView music_line=(TextView)convertView.findViewById(R.id.divider_line);
				if(number == position && flag==true)
				{
					music_line.setVisibility(View.VISIBLE);
				}  else {
					music_line.setVisibility(View.INVISIBLE);
				}
				return convertView;
			}
	    }
       //ʱ���ת����ʱ�䣨�֣��룩
	    public static String getDateToString(long time) {
	    	SimpleDateFormat sf;
	         Date d = new Date(time);
	         sf = new SimpleDateFormat("mm:ss");
	         return sf.format(d);
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
					//seekBarHandler.removeMessages(PROGRESS_INCREASE);
					progressBarHandler.removeMessages(PROGRESS_INCREASE);
					//seekBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
					progressBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
					//seekBar.setMax(duration);
					progressBar.setMax(duration);
					//seekBar.setProgress(time);
					progressBar.setMax(duration);
					Music m = list_Music.get(number);
					song_Name.setText(m.getTitle());
					text_Duration.setText(formatTime(duration));
					imgBtn_PlayOrPause.setImageDrawable(getResources().getDrawable(R.drawable.desktop_pausebt));
					break;
				case MusicService.STATUS_PAUSED:
					//seekBarHandler.sendEmptyMessage(PROGRESS_PAUSE);
					progressBarHandler.sendEmptyMessage(PROGRESS_PAUSE);
					imgBtn_PlayOrPause.setImageDrawable(getResources().getDrawable(R.drawable.desktop_playbt));
					break;
				case MusicService.STATUS_COMPLETED:
					//seekBarHandler.sendEmptyMessage(PROGRESS_RESET);
					progressBarHandler.sendEmptyMessage(PROGRESS_RESET);
					sendBroadcastOnCommand(MusicService.COMMAND_NEXT);
					imgBtn_PlayOrPause.setImageDrawable(getResources().getDrawable(R.drawable.desktop_playbt));
					break;
				case MusicService.RETURNBACK:
					number = intent.getIntExtra("number", 0);
					break;
				default:
					break;
				}
				updateUI(status);
				setViewVisibility(number);
			}
			//���ݲ������Ĳ���״̬������UI
		    private void updateUI(int status){
		    	switch(status){
		    	case MusicService.STATUS_PLAYING:
		    		imgBtn_PlayOrPause.setImageDrawable(getResources().getDrawable(R.drawable.desktop_pausebt));
		    		break;
		    	case MusicService.STATUS_PAUSED:
		    	case MusicService.STATUS_STOPPED:
		    	case MusicService.STATUS_COMPLETED:
		    		imgBtn_PlayOrPause.setImageDrawable(getResources().getDrawable(R.drawable.desktop_playbt));
		    		break;
		    	default:
		    		break;
		    	}
		    }
	    }
	    
	    @Override
	    protected void onDestroy(){
	    	if (isStopped()){
	    		stopService(new Intent(this, MusicService.class));
	    	}
	    	super.onDestroy();
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
	    
	    /*
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
	    					text_Current.setText(formatTime(time));
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
	    				text_Current.setText("00:00");
	    				break;
	    			}
	    		}
	    	};
	    }*/
	    
	    //��ʼ��������
	    private void intitProgressBarHandler(){
	    	progressBarHandler = new Handler(){
	    		public void handleMessage(Message msg){
	    			super.handleMessage(msg);
	    			switch (msg.what) {
	    			case PROGRESS_INCREASE:
	    				if (progressBar.getProgress() < duration) {
	    					//������ǰ��1��
	    					progressBar.incrementProgressBy(1000);
	    					progressBarHandler.sendEmptyMessageDelayed(PROGRESS_INCREASE, 1000);
	    					//�޸���ʾ��ǰ���ȵ��ı�
	    					text_Current.setText(formatTime(time));
	    					time += 1000;
	    				}
	    				break;
	    			case PROGRESS_PAUSE:
	    				progressBarHandler.removeMessages(PROGRESS_INCREASE);
	    				break;
	    			case PROGRESS_RESET:
	    				//���ý���������
	    				progressBarHandler.removeMessages(PROGRESS_INCREASE);
	    				progressBar.setProgress(0);
	    				text_Current.setText("00:00");
	    				break;
	    			}
	    		}
	    	};
	    }
}





