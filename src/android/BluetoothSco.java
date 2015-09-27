package gntikos.plugin.bluetoothsco;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
    private BluetoothDevice btDevice = null;
    private static final int RECORDER_SAMPLERATE = 8000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private static final String TAG = "JerrysBluetooth";
	private AudioRecord audioRecorder = null;
	private AudioTrack audioTrack = null;
	private Thread recordingThread = null;
	private boolean isRunning = false;
	private	int BufferElements2Rec = 80;
	private	int BytesPerElement = 2;
	private int bufferSize = BufferElements2Rec * BytesPerElement;
	private byte buffer[] = new byte[bufferSize];

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {

        if(this.cordova.getActivity().isFinishing()) return true;

        if (action.equals("start")) {
        
            boolean sendAudio = false;

            try {
                JSONObject parameters = args.getJSONObject(0);
                sendAudio = parameters.getBoolean("sendAudio");
            } catch (JSONException e) {
                // just catch the damn thing.
            }

            this.startScoConnection(callbackContext, sendAudio);
            return true;
        
        } else if (action.equals("stop")) {
        
        	boolean sendAudio = false;

            try {
                JSONObject parameters = args.getJSONObject(0);
                sendAudio = parameters.getBoolean("sendAudio");
            } catch (JSONException e) {
                // just catch the damn thing.
            }

            this.stopScoConnection(callbackContext, sendAudio);
            return true;
        
        } else if (action.equals("switchToSend")) {
        
            this.disableRecord();
            this.enableRecord(callbackContext, true);
            return true;
        
        } else if (action.equals("switchToReceive")) {

            this.disableRecord();
            this.enableRecord(callbackContext, false);
            return true;
        }

        return false;
    }

    private synchronized void enableRecord(final CallbackContext callbackContext, final boolean sendAudio) {
        final CordovaInterface cordova = this.cordova;

        Log.v(TAG, "enableRecord");
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);

                try {
                    int minBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    int streamType = AudioManager.STREAM_MUSIC;
                    int micType = MediaRecorder.AudioSource.VOICE_RECOGNITION;

                    Log.v(TAG, "sendAudio: " + sendAudio);

                    if(sendAudio) {
                        //audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        streamType = AudioManager.STREAM_VOICE_CALL;
                        micType = MediaRecorder.AudioSource.CAMCORDER;
                    }

                    Log.v(TAG, "micType: " + micType);
                    Log.v(TAG, "streamType: " + streamType);

                    audioTrack = new AudioTrack(streamType, RECORDER_SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO, RECORDER_AUDIO_ENCODING, minBufferSize, AudioTrack.MODE_STREAM);
                    audioRecorder = new AudioRecord(micType, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, minBufferSize);

                    audioManager.setMicrophoneMute(false);

                    try {
                        audioRecorder.startRecording();
                        Log.v(TAG, "audioRecorder started");
                    } catch (IllegalStateException e) {
                        Log.v(TAG, e.getMessage());
                        Log.v(TAG, "audioRecorder init FAILED");
                    }

                    try {
                        audioTrack.play();
                        Log.v(TAG, "audioTrack started");
                    } catch (IllegalStateException e) {
                        Log.v(TAG, e.getMessage());
                        Log.v(TAG, "audioTrack init FAILED");
                    }

                    isRunning = true;
                    recordingThread = new Thread(new Runnable() {
                        public void run() {
                            while (isRunning == true) {
                                audioRecorder.read(buffer, 0, bufferSize);
                                audioTrack.write(buffer, 0, bufferSize);
                            }
                        }
                    }, "Stream Thread");

                    recordingThread.start();

                    Log.v(TAG, "enableRecord SUCCESS");
                    callbackContext.success();
                } catch (Exception e) {
                    Log.v(TAG, "enableRecord FAILED");
                    callbackContext.error(e.getMessage());
                }
            }
        };

        this.cordova.getActivity().runOnUiThread(runnable);
    }

    private synchronized void disableRecord(){
        final CordovaInterface cordova = this.cordova;

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);

                try {
                    isRunning = false;

                    audioRecorder.stop();
                    audioRecorder.release();
                    audioRecorder = null;

                    audioTrack.stop();
                    audioTrack.flush();
                    audioTrack.release();
                    audioTrack = null;

                    recordingThread = null;

                    Log.v(TAG, "disableRecord SUCCESS");
                } catch (Exception e) {
                    Log.v(TAG, "disableRecord FAILED");
                }
            }
        };

        this.cordova.getActivity().runOnUiThread(runnable);
    }

    private synchronized void switchToReceive(){
        final CordovaInterface cordova = this.cordova;

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);

                try {
                    isRunning = false;

                    audioRecorder.stop();
                    audioRecorder.release();
                    audioRecorder = null;

                    audioTrack.stop();
                    audioTrack.flush();
                    audioTrack.release();
                    audioTrack = null;

                    recordingThread = null;

                    Log.v(TAG, "disableRecord SUCCESS");
                } catch (Exception e) {
                    Log.v(TAG, "disableRecord FAILED");
                }
            }
        };

        this.cordova.getActivity().runOnUiThread(runnable);
    }

    private synchronized void startScoConnection(final CallbackContext callbackContext, final boolean sendAudio) {
        final CordovaInterface cordova = this.cordova;

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                if (btAdapter == null) {
                    callbackContext.error("This device does not support Bluetooth.");
                    return;
                }
                else if (! btAdapter.isEnabled()) {
                    callbackContext.error("Bluetooth is not enabled.");
                    return;
                }

                btDevice = btAdapter.getRemoteDevice(btAdapter.getAddress());

                Log.v(TAG, "btAdapter state: " + btAdapter.getState());
                Log.v(TAG, "btAdapter address: " + btAdapter.getAddress());
                Log.v(TAG, "btDevice bluetooth class: "+ btDevice.getBluetoothClass());
                Log.v(TAG, "btDevice name: " + btDevice.getName());
                Log.v(TAG, "btDevice type: " + btDevice.getType());
                Log.v(TAG, "btDevice UUIDs: " + btDevice.getUuids());

                cordova.getActivity().registerReceiver(new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

                        if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                            AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);

                            int maxVolume = audioManager.getStreamMaxVolume(6);
                            audioManager.setStreamVolume(6, maxVolume, 0);

                            //callbackContext.success(btAdapter.getAddress());
                            enableRecord(callbackContext, sendAudio);
                            context.unregisterReceiver(this);
                        }

                    }

                }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));

                AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
                if (!audioManager.isBluetoothScoAvailableOffCall()) {
                    callbackContext.error("Off-call Bluetooth audio not supported on this device.");
                    return;
                }

                audioManager.setBluetoothScoOn(true);
                audioManager.startBluetoothSco();
            }
        };

        this.cordova.getActivity().runOnUiThread(runnable);
    }

    private synchronized void stopScoConnection(final CallbackContext callbackContext, final boolean sendAudio) {

        final CordovaInterface cordova = this.cordova;

        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
                Log.v(TAG, "sendAudio: "+sendAudio);

                try {
                    disableRecord();

					audioManager.stopBluetoothSco();
					audioManager.setBluetoothScoOn(false);
					audioManager.setMode(AudioManager.MODE_NORMAL);
                    callbackContext.success();
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        };

        this.cordova.getActivity().runOnUiThread(runnable);
    }

}
