/**
 * Copyright 2008 - 2019 The Loon Game Engine Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.5
 */
package loon.se;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.MP3Player;
import loon.LGame;
import loon.SoundImpl;
import loon.utils.MathUtils;

public class JavaSEMP3Sound extends SoundImpl<Object> {

	private LGame _game;

	private int mode = 0;

	MP3Player mp3Player;

	public JavaSEMP3Sound(LGame g) {
		_game = g;
	}

	public LGame getGame() {
		return _game;
	}

	synchronized void loadMP3(InputStream ins) throws IOException {
		try {
			mp3Player = new MP3Player(ins);
		} catch (JavaLayerException e) {
			throw new IOException(e.getMessage());
		}
		mode = 1;
	}

	@Override
	protected synchronized boolean playingImpl() {
		switch (mode) {
		case 0:
			return (((Clip) impl)).isActive();
		case 1:
			return !mp3Player.isComplete();
		}
		return false;
	}

	@Override
	protected synchronized boolean playImpl() {
		switch (mode) {
		case 0:
			((Clip) impl).setFramePosition(0);
			if (looping) {
				((Clip) impl).loop(Clip.LOOP_CONTINUOUSLY);
			} else {
				((Clip) impl).start();
			}
			break;
		case 1:
			_game.invokeAsync(new AsyncSound(this));
			break;
		}
		return true;
	}

	@Override
	protected synchronized void stopImpl() {
		switch (mode) {
		case 0:
			((Clip) impl).stop();
			((Clip) impl).flush();
			break;
		case 1:
			mp3Player.stop();
			break;
		}
	}

	@Override
	protected synchronized void setLoopingImpl(boolean looping) {
		this.looping = looping;
	}

	@Override
	protected synchronized void setVolumeImpl(float volume) {
		switch (mode) {
		case 0:
			if (((Clip) impl).isControlSupported(FloatControl.Type.MASTER_GAIN)) {
				FloatControl volctrl = (FloatControl) ((Clip) impl).getControl(FloatControl.Type.MASTER_GAIN);
				volctrl.setValue(toGain(volume, volctrl.getMinimum(), volctrl.getMaximum()));
			}
			break;
		case 1:
			mp3Player.setGain(volume);
			this.volume = volume;
			break;
		}
	}

	@Override
	protected synchronized void releaseImpl() {
		switch (mode) {
		case 0:
			((Clip) impl).close();
			break;
		case 1:
			mp3Player.free();
			break;
		}
	}

	@Override
	public boolean pause() {
		stopImpl();
		return true;
	}

	protected static float toGain(float volume, float min, float max) {
		return MathUtils.clamp((float) (20 * Math.log10(volume)), min, max);
	}

	private static final class AsyncSound implements Runnable {

		final JavaSEMP3Sound _sound;

		AsyncSound(final JavaSEMP3Sound sound) {
			_sound = sound;
		}

		@Override
		public void run() {
			try {
				if (_sound.looping) {
					for (; !_sound.mp3Player.isClosed();) {
						_sound.mp3Player.play();
					}
				} else {
					_sound.mp3Player.play();
				}
			} catch (Exception e) {
			}
		}
	}

}
