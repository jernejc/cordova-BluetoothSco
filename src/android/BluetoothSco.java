package gntikos.plugin.bluetoothsco;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;

import android.util.Log;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class BluetoothSco extends CordovaPlugin {
	
	private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	private AudioRecord audioRecorder = null;
	private AudioManager audioManager = null;
	private static final String TAG = "JerrysBluetooth";
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
		
		if(this.cordova.getActivity().isFinishing()) return true;
		
		if (action.equals("receive")) {
			this.startRecieveScoConnection(callbackContext);
			return true;
		} 
		else if (action.equals("stop")) {
			this.stopScoConnection(callbackContext);
			return true;
		} 
		else if (action.equals("send")) {
			this.startSendScoConnection(callbackContext);
			return true;
		}
		
		return false;
	}

	private synchronized void startSendScoConnection(final CallbackContext callbackContext) {
		final CordovaInterface cordova = this.cordova;

		Log.v(TAG, "startSendScoConnection");
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				Log.v(TAG, "startSendScoConnection RUN");

				if (btAdapter == null) {
					callbackContext.error("This device does not support Bluetooth");
					Log.v(TAG, "startSendScoConnection no bluetooth support");
					return;
				}
				else if (! btAdapter.isEnabled()) {
					callbackContext.error("Bluetooth is not enabled");
					Log.v(TAG, "startSendScoConnection bluetooth not enabled");
					return;
				} 

				cordova.getActivity().registerReceiver(new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {
						Log.v(TAG, "startSendScoConnection onReceive");
						int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

						if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
							Log.v(TAG, "startSendScoConnection SCO_AUDIO_STATE_CONNECTED");

							int samppersec = 8000;
							int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
							int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
							int buffersizebytes = AudioRecord.getMinBufferSize(samppersec,channelConfiguration,audioEncoding); 
							
							Log.v(TAG, "recorder init");
							audioRecorder = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC,samppersec,channelConfiguration,audioEncoding,buffersizebytes); // constructor
							audioRecorder.startRecording();

							callbackContext.success();
							context.unregisterReceiver(this);

						}
							else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
								Log.v(TAG, "startSendScoConnection SCO_AUDIO_STATE_DISCONNECTED");
								
								callbackContext.error("Could not start Bluetooth SCO connection");
								context.unregisterReceiver(this);
						}
					}
					
				}, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
				
				audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
				if (! audioManager.isBluetoothScoAvailableOffCall()) {
					callbackContext.error("Off-call Bluetooth audio not supported on this device.");
					return;
				}
				
				audioManager.setMode(AudioManager.MODE_IN_CALL);
				audioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
				audioManager.setBluetoothScoOn(true);
				audioManager.startBluetoothSco();
			}
		};
		
		this.cordova.getActivity().runOnUiThread(runnable);
	}	

	private synchronized void startRecieveScoConnection(final CallbackContext callbackContext) {
		final CordovaInterface cordova = this.cordova;
		
		Log.v(TAG, "startRecieveScoConnection");

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				if (btAdapter == null) {
					callbackContext.error("This device does not support Bluetooth");
					return;
				}
				else if (! btAdapter.isEnabled()) {
					callbackContext.error("Bluetooth is not enabled");
					return;
				} 
				
				cordova.getActivity().registerReceiver(new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {
						int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
						
						if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
							callbackContext.success();
							context.unregisterReceiver(this);
						}
							else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
								callbackContext.error("Could not start Bluetooth SCO connection");
								context.unregisterReceiver(this);
						}
					}
					
				}, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
				
				audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
				if (! audioManager.isBluetoothScoAvailableOffCall()) {
					callbackContext.error("Off-call Bluetooth audio not supported on this device.");
					return;
				}
				
				audioManager.setBluetoothScoOn(true);
				audioManager.startBluetoothSco();
			}
		};
		
		this.cordova.getActivity().runOnUiThread(runnable);
	}
	
	private synchronized void stopScoConnection(final CallbackContext callbackContext) {
		
		final CordovaInterface cordova = this.cordova;

		Log.v(TAG, "stopScoConnection");
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
				
				try {
					audioManager.setMode(AudioManager.MODE_NORMAL);
					audioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
					audioRecorder.stop();
					audioRecorder.release();
					audioManager.stopBluetoothSco();
					audioManager.setBluetoothScoOn(false);
					callbackContext.success();
				} catch (Exception e) {
					callbackContext.error(e.getMessage());
				}
			}
		};
		
		this.cordova.getActivity().runOnUiThread(runnable);
	}

}