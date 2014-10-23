package gntikos.plugin.bluetoothsco;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioFormat;

public class BluetoothSco extends CordovaPlugin {

	private AudioTrack audioTrack = null;
	
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
		
		if (action.equals("start")) {
			this.startScoConnection(callbackContext);
			return true;
		} 
		else if (action.equals("stop")) {
			this.stopScoConnection(callbackContext);
			return true;
		}
		
		return false;
	}
	

	private synchronized void startScoConnection(final CallbackContext callbackContext) {
		
		final CordovaInterface cordova = this.cordova;
				
		AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);

		short[] soundData = new short [8000*20];
		for (int iii = 0; iii < 20*8000; iii++) {
			soundData[iii] = 32767;
			iii++;
			soundData[iii] = -32768;
		}

		this.audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
					8000, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, soundData.length
					* Short.SIZE, AudioTrack.MODE_STATIC);

		this.audioTrack.write(soundData, 0, soundData.length);
		this.audioTrack.play();
		
		audioManager.setMode(AudioManager.MODE_IN_CALL);
		audioManager.setBluetoothScoOn(true);
		audioManager.startBluetoothSco();

	}
	
	private synchronized void stopScoConnection(final CallbackContext callbackContext) {
		
		final CordovaInterface cordova = this.cordova;
				
		AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);

		this.audioTrack.stop();
		this.audioTrack.release();
		audioManager.stopBluetoothSco();
		audioManager.setBluetoothScoOn(false);
		
	}

}
