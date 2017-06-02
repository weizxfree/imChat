package com.itutorgroup.tutorchat.phone.utils.voice;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;

import java.io.IOException;

public class MediaManager {
	private static MediaPlayer mMediaPlayer;
	private static boolean isPause = false;
	public static void playSound(String filePath) {
		playSound(filePath, null);
	}

	public static void playSound(String filePath, MediaPlayer.OnCompletionListener onCompletionListener) {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					LogUtil.d("medialog","init onError");
					mMediaPlayer.reset();
					return false;
				}
			});

		} else {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.reset();
		}
		try {
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
			if (onCompletionListener != null) {
				mMediaPlayer.setOnCompletionListener(onCompletionListener);
			}
			mMediaPlayer.setDataSource(filePath);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (IllegalArgumentException e) {
			LogUtil.d("medialog","IllegalArgumentException");
			e.printStackTrace();
		} catch (SecurityException e) {
			LogUtil.d("medialog","SecurityException");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			LogUtil.d("medialog","IllegalStateException");
			e.printStackTrace();
		} catch (IOException e) {
			LogUtil.d("medialog","IOException");
			e.printStackTrace();
		}
	}

	public static void pause() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			isPause = true;
		}
	}
	public static void resume() {
		if (mMediaPlayer != null && isPause) {
			mMediaPlayer.start();
			isPause = false;
		}
	}

	/*
      释放资源
     */
	public static void realese() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
			isPause = true;
		}
	}

	public static boolean isPlaying(){

		if (mMediaPlayer != null) {
			return mMediaPlayer.isPlaying();
		}
		return false;
	}

	public static void reset(){
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		mMediaPlayer.reset();
	}



	public static void destroy(){
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		realese();
	}

	public static void setVolume(int left,int right){
		if(mMediaPlayer != null){
			mMediaPlayer.setVolume(left, right);
		}
	}









}

