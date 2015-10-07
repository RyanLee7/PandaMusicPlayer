package pandamusicplayer.the.muisc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pandamusicplayer.the.domain.Music;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service{
	
	//���ſ��������ʶ����
	public static final int COMMAND_UNKNOWN  = -1;
	public static final int COMMAND_PLAY = 0;
	public static final int COMMAND_PAUSE = 1;
	public static final int COMMAND_STOP = 2;
	public static final int COMMAND_RESUME = 3;
	public static final int COMMAND_PREVIOUS = 4;
	public static final int COMMAND_NEXT = 5;
	public static final int COMMAND_CHECK_IS_PLAYING = 6;
	public static final int COMMAND_SEEK_TO = 7;
	public static final int COMMAND_TELL_ME_NUMBER = 8;
	
	//������״̬
	public static final int STATUS_PLAYING = 0;
	public static final int STATUS_PAUSED = 1;
	public static final int STATUS_STOPPED = 2;
	public static final int STATUS_COMPLETED = 3;
	public static final int RETURNBACK = 4;
	
	//�㲥��ʶ
	public static final String BROADCAST_MUSICSERVICE_CONTROL = 
			"MusicService.ACTION_CONTROL";
	public static final String BROADCAST_MUSICSERVICE_UPDATE_STATUS =
			"MusicService.ACTION_UPDATE";
	private MediaPlayer player;
	
	//���Music�����Ա�
	private List<Music> list_Music;
	
	//�㲥������
	private CommandReceiver receiver;
	
	private int number;
	
	SharedPreferences sp;
	
	//�ڲ��࣬���չ㲥�����ִ�в���
	class CommandReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			//��ȡ����
			int command = intent.getIntExtra("command", COMMAND_UNKNOWN);
			//ִ������
			switch (command){
			case COMMAND_SEEK_TO:
				seekTo(intent.getIntExtra("time", 0));
				break;
			case COMMAND_PLAY:
			case COMMAND_PREVIOUS:
			case COMMAND_NEXT:
				number = intent.getIntExtra("number",1);
				Toast.makeText(MusicService.this, "���ڲ��ŵ�" + (number +1) + "��", Toast.LENGTH_SHORT).show();
				play(number);
				break;
			case COMMAND_PAUSE:
				pause();
				break;
			case COMMAND_RESUME:
				resume();
				break;
			case COMMAND_CHECK_IS_PLAYING:
				if(player.isPlaying()){
					sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
				}
				break;
			case COMMAND_TELL_ME_NUMBER:
				sendBroadcastOnStatusChanged(MusicService.RETURNBACK);
				break;
			case COMMAND_UNKNOWN:
			default:
				break;
			}
		}
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		SharedPreferences sp = getSharedPreferences("service", 0);
		sp.edit().putBoolean("isStart", true).commit();
		//�󶨹㲥�����������Խ��չ㲥
		bindCommandReceiver();
		GetMusicCursor();
		//Toast.makeText(this, "MusicService.onCreate()", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onStart(Intent intent,int startId){
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy(){
		SharedPreferences sp = getSharedPreferences("service", 0);
		sp.edit().putBoolean("isStart", false).commit();
		//�ͷŲ�������Դ
		if(player != null){
			player.release();
		}
		super.onDestroy();
	}
	
	//�󶨹㲥������
	private void bindCommandReceiver(){
		receiver = new CommandReceiver();
		IntentFilter filter = new IntentFilter(BROADCAST_MUSICSERVICE_CONTROL);
		registerReceiver(receiver,filter);
	}
	
	//���͹㲥������״̬�ı���
	private void sendBroadcastOnStatusChanged(int status){
		Intent intent = new Intent(BROADCAST_MUSICSERVICE_UPDATE_STATUS);
		intent.putExtra("status", status);
		if(status == STATUS_PLAYING){
			intent.putExtra("time", player.getCurrentPosition());
			intent.putExtra("duration", player.getDuration());
		}
		if(status == RETURNBACK){
			intent.putExtra("number", number);
		}
		sendBroadcast(intent);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		//ע�������
		player.setOnCompletionListener(completionListener);
	}
	
	//���Ž���������
	OnCompletionListener completionListener = new OnCompletionListener(){
		@Override
		public void onCompletion(MediaPlayer arg0) {
			if(player.isLooping()){
				replay();
			} else {
				sendBroadcastOnStatusChanged(MusicService.STATUS_COMPLETED);
			}
		}
	};
	
	//��������
	public void play(int number){
		//ֹͣ��ǰ����
		if(player != null && player.isPlaying()){
			player.stop();
		}
		load(number);
		player.start();
		sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
	}
	
	//��ͣ����
	public void pause(){
		if(player.isPlaying()){
			player.pause();
			sendBroadcastOnStatusChanged(MusicService.STATUS_PAUSED);
		}
	}
	
	//ֹͣ��������
	public void stop(){
		if(player != null){
			player.stop();
			sendBroadcastOnStatusChanged(MusicService.STATUS_STOPPED);
		}
	}
	
	//�ָ����ţ���ͣ��
	public void resume(){
		player.start();
		sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
	}
	
	//���²��ţ�������ɺ�
	public void replay(){
		player.start();
		sendBroadcastOnStatusChanged(MusicService.STATUS_PLAYING);
	}
	
	//��ת������λ��
	private void seekTo(int time){
		if (player != null){
			player.seekTo(time);
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
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
}
