/**
 * Copyright 2008 - 2015 The Loon Game Engine Authors
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
package loon;

import java.io.OutputStream;

import loon.action.ActionControl;
import loon.canvas.Image;
import loon.canvas.LColor;
import loon.font.IFont;
import loon.opengl.GLEx;
import loon.utils.ArrayByte;
import loon.utils.ArrayByteOutput;
import loon.utils.GLUtils;
import loon.utils.GifEncoder;
import loon.utils.MathUtils;
import loon.utils.StrBuilder;
import loon.utils.StringUtils;
import loon.utils.processes.RealtimeProcessManager;
import loon.utils.reply.Act;
import loon.utils.reply.Port;
import loon.utils.timer.LTimer;
import loon.utils.timer.LTimerContext;

/**
 * loon的最上级显示渲染与控制用类,本地api通过与此类交互实现游戏功能
 */
public class Display extends BaseIO implements LRelease {

	// 为了方便直接转码到C#和C++，无法使用匿名内部类(也就是在构造内直接构造实现的方式)，只能都写出具体类来……
	// PS:别提delegate，委托那玩意写出来太不优雅了(对于凭空实现某接口或抽象，而非局部重载来说)，而且大多数J2C#的工具也不能直接转换过去……
	private final class PaintPort extends Port<LTimerContext> {

		private final Display _display;

		PaintPort(Display d) {
			this._display = d;
		}

		@Override
		public void onEmit(LTimerContext clock) {
			synchronized (clock) {
				if (!LSystem.PAUSED) {
					RealtimeProcessManager.get().tick(clock);
					_display.draw(clock);
				}
			}
		}

	}

	private final class PaintAllPort extends Port<LTimerContext> {

		private final Display _display;

		PaintAllPort(Display d) {
			this._display = d;
		}

		@Override
		public void onEmit(LTimerContext clock) {
			synchronized (clock) {
				if (!LSystem.PAUSED) {
					RealtimeProcessManager.get().tick(clock);
					ActionControl.get().call(clock.timeSinceLastUpdate);
					_display.draw(clock);
				}
			}
		}

	}

	private final class UpdatePort extends Port<LTimerContext> {

		UpdatePort() {
		}

		@Override
		public void onEmit(LTimerContext clock) {
			synchronized (clock) {
				if (!LSystem.PAUSED) {
					ActionControl.get().call(clock.timeSinceLastUpdate);
				}
			}
		}
	}

	private final class Logo implements LRelease {

		private int centerX = 0, centerY = 0;

		private float alpha = 0f;

		private float curFrame, curTime;

		boolean finish, inToOut;

		LTexture logo;

		public Logo(LTexture texture) {
			this.logo = texture;
			this.curTime = 60;
			this.curFrame = 0;
			this.inToOut = true;
		}

		public void draw(final GLEx gl) {
			if (logo == null || finish) {
				return;
			}
			if (!logo.isLoaded()) {
				this.logo.loadTexture();
			}
			if (centerX == 0 || centerY == 0) {
				this.centerX = (LSystem.viewSize.getWidth()) / 2 - logo.getWidth() / 2;
				this.centerY = (LSystem.viewSize.getHeight()) / 2 - logo.getHeight() / 2;
			}
			if (logo == null || !logo.isLoaded()) {
				return;
			}
			alpha = (curFrame / curTime);
			if (inToOut) {
				curFrame++;
				if (curFrame == curTime) {
					alpha = 1f;
					inToOut = false;
				}
			} else if (!inToOut) {
				curFrame--;
				if (curFrame == 0) {
					alpha = 0f;
					finish = true;
				}
			}
			gl.setAlpha(MathUtils.clamp(alpha, 0f, 0.98f));
			gl.draw(logo, centerX, centerY);
		}

		@Override
		public void close() {
			if (logo != null) {
				logo.close();
				logo = null;
			}
		}
	}

	public final Act<LTimerContext> update = Act.create();

	public final Act<LTimerContext> paint = Act.create();

	private final LTimerContext updateClock = new LTimerContext();

	private final LTimerContext paintClock = new LTimerContext();

	private final LGame _game;

	private LColor _debugFontColor = LColor.white;

	private boolean _closed, _autoUpdate, _autoRepaint;

	private final long _updateRate;

	private long _nextUpdate;

	private final static String FPS_STR = "FPS:";

	private final static String MEMORY_STR = "MEMORY:";

	private final static String SPRITE_STR = "SPRITE:";

	private final static String DESKTOP_STR = "DESKTOP:";

	private final static String DRAWCALL_STR = "DRAWCALL:";

	private String displayMemony = MEMORY_STR;

	private String displaySprites = SPRITE_STR;

	private String displayDrawCall = DRAWCALL_STR;

	private StrBuilder displayMessage = new StrBuilder(LSystem.DEFAULT_MAX_CACHE_SIZE);

	private GifEncoder gifEncoder;

	private boolean _videoScreenToGif;

	private boolean _memorySelf;

	private ArrayByteOutput videoCache;

	private final LTimer videoDelay = new LTimer();

	private Runtime _runtime;

	private long _frameCount = 0l;

	private long _frameDelta = 0l;

	private long _sinceRefreshMaxInterval = 0l;

	private int _frameRate = 0;

	private int _debugTextSpace = 0;

	private IFont _displayFont;

	private float cred, cgreen, cblue, calpha;

	private LogDisplay _logDisplay;

	private final GLEx _glEx;

	private final LProcess _process;

	private LSetting _setting;

	private int _displayTop;

	protected boolean showLogo = false, initDrawConfig = false;

	private boolean logDisplayCreated = false;

	private Logo logoTex;

	private PaintAllPort paintAllPort;

	private PaintPort paintPort;

	private UpdatePort updatePort;

	public Display(LGame g, long updateRate) {
		this._updateRate = updateRate;
		this._game = g;
		this._game.checkBaseGame(g);
		this._setting = _game.setting;
		this._process = _game.process();
		this._sinceRefreshMaxInterval = LSystem.SECOND;
		this._debugTextSpace = 5;
		this._memorySelf = _game.isBrowser();
		this._glEx = new GLEx(_game.graphics());
		this._glEx.update();
		this.initGameDisplay(g);
	}

	protected void initGameDisplay(LGame game) {
		this.updateSyncTween(game.setting.isSyncTween);
		this.initDebugString();
		this.autoDisplay();
		game.setupDisplay(this);
	}

	protected void initDebugString() {
		this.displayMemony = MEMORY_STR + "0";
		this.displaySprites = SPRITE_STR + "0 " + DESKTOP_STR + "0";
		this.displayDrawCall = DRAWCALL_STR + "0";
	}

	public Display autoDisplay() {
		this._autoUpdate = this._autoRepaint = true;
		return this;
	}

	public Display stopAutoDisplay() {
		this._autoUpdate = this._autoRepaint = false;
		return this;
	}

	public LColor getDebugFontColor() {
		return this._debugFontColor;
	}

	public Display setDebugFontColor(LColor fc) {
		this._debugFontColor = fc;
		return this;
	}

	public int getDebugTextSpace() {
		return this._debugTextSpace;
	}

	public Display setDebugTextSpace(int s) {
		this._debugTextSpace = s;
		return this;
	}

	public long getSinceRefreshMaxInterval() {
		return this._sinceRefreshMaxInterval;
	}

	public Display setSinceRefreshMaxInterval(long s) {
		this._sinceRefreshMaxInterval = s;
		return this;
	}

	protected void newDefView(boolean show) {
		if (show && _displayFont == null) {
			this._displayFont = LSystem.getSystemLogFont();
		}
		if (show && _setting.isDisplayLog) {
			if (_displayFont != null) {
				_logDisplay = new LogDisplay(_displayFont);
			} else {
				_logDisplay = new LogDisplay();
			}
			logDisplayCreated = true;
		}
		showLogo = _setting.isLogo;
		if (showLogo && !StringUtils.isEmpty(_setting.logoPath)) {
			logoTex = new Logo(newTexture(_setting.logoPath));
		}
	}

	public boolean isLogDisplay() {
		return logDisplayCreated;
	}

	public void clearLog() {
		if (logDisplayCreated) {
			_logDisplay.clear();
		}
	}

	public void addLog(String mes, LColor col) {
		if (!logDisplayCreated) {
			return;
		}
		_logDisplay.addText(mes, col);
	}

	public void addLog(String mes) {
		if (!logDisplayCreated) {
			return;
		}
		_logDisplay.addText(mes);
	}

	public LogDisplay getLogDisplay() {
		return _logDisplay;
	}

	protected void paintLog(final GLEx g, int x, int y) {
		if (!logDisplayCreated) {
			return;
		}
		_logDisplay.paint(g, x, y);
	}

	public void updateSyncTween(boolean sync) {
		if (paintAllPort != null) {
			paint.disconnect(paintAllPort);
		}
		if (paintPort != null) {
			paint.disconnect(paintPort);
		}
		if (update != null) {
			update.disconnect(updatePort);
		}
		if (sync) {
			paint.connect(paintAllPort = new PaintAllPort(this));
		} else {
			paint.connect(paintPort = new PaintPort(this));
			update.connect(updatePort = new UpdatePort());
		}
	}

	/**
	 * 清空当前游戏窗体内容为指定色彩
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public void clearColor(float red, float green, float blue, float alpha) {
		cred = red;
		cgreen = green;
		cblue = blue;
		calpha = alpha;
	}

	/**
	 * 清空当前游戏窗体内容为指定色彩
	 * 
	 * @param color
	 */
	public void clearColor(LColor color) {
		this.clearColor(color.r, color.g, color.b, color.a);
	}

	/**
	 * 清空当前游戏窗体内容为纯黑色
	 */
	public void clearColor() {
		this.clearColor(0, 0, 0, 0);
	}

	public void update(LTimerContext clock) {
		update.emit(clock);
	}

	public void paint(LTimerContext clock) {
		paint.emit(clock);
	}

	protected void draw(LTimerContext clock) {
		if (_closed) {
			return;
		}
		// fix渲染时机，避免调用渲染在纹理构造前
		if (!initDrawConfig) {
			newDefView(
					_setting.isFPS || _setting.isLogo || _setting.isMemory || _setting.isSprites || _setting.isDebug);
			initDrawConfig = true;
		}

		if (showLogo) {
			try {
				_glEx.save();
				_glEx.begin();
				_glEx.clear(cred, cgreen, cblue, calpha);
				if (logoTex == null || logoTex.finish || logoTex.logo.disposed()) {
					showLogo = false;
					return;
				}
				logoTex.draw(_glEx);
				if (logoTex.finish) {
					showLogo = false;
					logoTex.close();
					logoTex = null;
				}
			} finally {
				_glEx.end();
				_glEx.restore();
				if (!showLogo) {
					_process.start();
				}
			}
			return;
		}

		if (!_process.next()) {
			return;
		}
		try {
			_glEx.saveTx();

			// 在某些情况下,比如存在全局背景时，因为旧有画面已被遮挡，不必全局刷新Screen画面,应禁止全局刷新画布内容
			if (_setting.allScreenRefresh) {
				_glEx.reset(cred, cgreen, cblue, calpha);
			} else {
				_glEx.resetConfig();
			}

			_glEx.begin();

			// 最初渲染的内容
			_process.drawFrist(_glEx);
			_process.load();
			_process.runTimer(clock);

			_process.draw(_glEx);

			// 渲染debug信息
			drawDebug(_glEx, _setting, clock.unscaledTimeSinceLastUpdate);

			_process.drawEmulator(_glEx);
			// 最后渲染的内容
			_process.drawLast(_glEx);

			_process.unload();

			// 如果存在屏幕录像设置
			if (_videoScreenToGif && !LSystem.PAUSED && gifEncoder != null) {
				if (videoDelay.action(clock)) {
					Image tmp = GLUtils.getScreenshot();
					Image image = null;
					if (LSystem.isDesktop()) {
						image = tmp;
					} else {
						// 因为内存和速度关系,考虑到全平台录制,因此默认只录屏幕大小的一半(否则在手机上绝对抗不了5分钟以上……)
						image = Image.getResize(tmp, MathUtils.iceil(_process.getWidth() * 0.5f),
								MathUtils.iceil(_process.getHeight() * 0.5f));
					}
					gifEncoder.addFrame(image);
					if (tmp != null) {
						tmp.close();
						tmp = null;
					}
					if (image != null) {
						image.close();
						image = null;
					}
				}
			}

		} finally {
			_glEx.end();
			_glEx.restoreTx();
			_process.resetTouch();
			GraphicsDrawCall.clear();
		}
	}

	protected void onFrame() {
		if (_closed) {
			return;
		}
		final LSetting setting = _game.setting;
		final float fpsScale = setting.getScaleFPS();
		if (_autoUpdate) {
			final int updateTick = _game.tick();
			final long updateLoop = setting.fixedUpdateLoopTime;
			long nextUpdate = this._nextUpdate;
			if (updateTick >= nextUpdate) {
				final long updateRate = this._updateRate;
				long updates = 0;
				while (updateTick >= nextUpdate) {
					nextUpdate += updateRate;
					updates++;
				}
				this._nextUpdate = nextUpdate;
				final long updateDt = updates * updateRate;
				if (updateLoop == -1) {
					updateClock.timeSinceLastUpdate = (long) (updateDt * fpsScale);
					updateClock.unscaledTimeSinceLastUpdate = updateDt;
				} else {
					updateClock.timeSinceLastUpdate = updateLoop;
					updateClock.unscaledTimeSinceLastUpdate = updateLoop;
				}
				if (updateClock.timeSinceLastUpdate > _sinceRefreshMaxInterval) {
					updateClock.timeSinceLastUpdate = 0;
					updateClock.unscaledTimeSinceLastUpdate = 0;
				}
				updateClock.tick += updateClock.timeSinceLastUpdate;
				update(updateClock);
			}
		}
		if (_autoRepaint) {
			final long paintLoop = setting.fixedPaintLoopTime;
			final long paintTick = _game.tick();
			if (paintLoop == -1) {
				final long clock = paintTick - paintClock.tick;
				paintClock.timeSinceLastUpdate = (long) (clock * fpsScale);
				paintClock.unscaledTimeSinceLastUpdate = clock;
			} else {
				paintClock.timeSinceLastUpdate = paintLoop;
				paintClock.unscaledTimeSinceLastUpdate = paintLoop;
			}
			if (paintClock.timeSinceLastUpdate > _sinceRefreshMaxInterval) {
				paintClock.timeSinceLastUpdate = 0;
				paintClock.unscaledTimeSinceLastUpdate = 0;
			}
			paintClock.tick = paintTick;
			paintClock.alpha = 1f - (_nextUpdate - paintTick) / (float) _updateRate;
			paint(paintClock);
		}
	}

	/**
	 * 渲染debug信息到游戏画面
	 * 
	 * @param gl
	 * @param setting
	 * @param delta
	 */
	private final void drawDebug(final GLEx gl, final LSetting setting, final long delta) {
		if (_closed) {
			return;
		}
		final boolean debug = setting.isDebug;

		if (debug || setting.isFPS || setting.isMemory || setting.isSprites || setting.isDrawCall) {

			this._frameCount++;
			this._frameDelta += delta;

			if (_frameCount % 60 == 0 && _frameDelta != 0) {
				final int dstFPS = setting.fps;
				final int newFps = MathUtils
						.round((_sinceRefreshMaxInterval * _frameCount * setting.getScaleFPS()) / _frameDelta) + 1;
				this._frameRate = MathUtils.clamp(newFps, 0, dstFPS);
				if (_frameRate == dstFPS - 1) {
					_frameRate = MathUtils.max(dstFPS, _frameRate);
				}
				this._frameDelta = this._frameCount = 0;

				if (this._memorySelf) {
					displayMessage.setLength(0);
					displayMessage.append(MEMORY_STR);
					displayMessage.append(MathUtils.abs(((LTextures.getMemSize() * 100) >> 20) / 10f));
					displayMessage.append(" of ");
					displayMessage.append('?');
					displayMessage.append(" MB");
				} else {
					if (_runtime == null) {
						_runtime = Runtime.getRuntime();
					}
					long totalMemory = _runtime.totalMemory();
					long currentMemory = totalMemory - _runtime.freeMemory();
					displayMessage.setLength(0);
					displayMessage.append(MEMORY_STR);
					displayMessage.append(MathUtils.abs((currentMemory * 10) >> 20) / 10f);
					displayMessage.append(" of ");
					displayMessage.append(MathUtils.abs((_runtime.maxMemory() * 10) >> 20) / 10f);
					displayMessage.append(" MB");
				}
				displayMemony = displayMessage.toString();

				LGame game = getGame();

				displayMessage.setLength(0);
				displayMessage.append(SPRITE_STR);
				displayMessage.append(game.allSpritesCount());
				displayMessage.append(" ");
				displayMessage.append(DESKTOP_STR);
				displayMessage.append(game.allDesktopCount());

				displaySprites = displayMessage.toString();

				displayMessage.setLength(0);
				displayMessage.append(DRAWCALL_STR);
				displayMessage.append(GraphicsDrawCall.getCount() + gl.getDrawCallCount());

				displayDrawCall = displayMessage.toString();

			}
			if (_displayFont != null) {

				final int maxHeight = MathUtils.max(10, _displayFont.getSize()) + 2;

				// 显示fps速度
				if (debug || setting.isFPS) {
					_displayFont.drawString(gl, FPS_STR + _frameRate, _debugTextSpace, _displayTop += _debugTextSpace,
							0, _debugFontColor);
				}
				// 显示内存占用
				if (debug || setting.isMemory) {
					_displayFont.drawString(gl, displayMemony, _debugTextSpace, _displayTop += maxHeight, 0,
							_debugFontColor);
				}
				// 显示精灵与组件数量
				if (debug || setting.isSprites) {
					_displayFont.drawString(gl, displaySprites, _debugTextSpace, _displayTop += maxHeight, 0,
							_debugFontColor);
				}
				// 显示渲染次数
				if (debug || setting.isDrawCall) {
					_displayFont.drawString(gl, displayDrawCall, _debugTextSpace, _displayTop += maxHeight, 0,
							_debugFontColor);
				}
				// 若打印日志到界面,很可能挡住游戏界面内容,所以isDisplayLog为true并且debug才显示
				if (debug && setting.isDisplayLog) {
					paintLog(gl, _debugTextSpace, _displayTop += maxHeight);
				}
				_displayTop = 0;
			}
		}

	}

	public boolean isRunning() {
		return initDrawConfig;
	}

	public boolean isAutoRepaint() {
		return _autoRepaint;
	}

	public Display setAutoRepaint(boolean r) {
		this._autoRepaint = r;
		return this;
	}

	public Display stopRepaint() {
		this._autoRepaint = false;
		return this;
	}

	public Display startRepaint() {
		this._autoRepaint = true;
		return this;
	}

	public boolean isAutoUpdate() {
		return _autoUpdate;
	}

	public Display setAutoUpdate(boolean u) {
		this._autoUpdate = u;
		return this;
	}

	public Display stopUpdate() {
		this._autoUpdate = false;
		return this;
	}

	public Display startUpdate() {
		this._autoUpdate = true;
		return this;
	}

	public int getFPS() {
		return _frameRate;
	}

	public float getAlpha() {
		return calpha;
	}

	public float getRed() {
		return cred;
	}

	public float getGreen() {
		return cgreen;
	}

	public float getBlue() {
		return cblue;
	}

	public GLEx GL() {
		return _glEx;
	}

	public float width() {
		return LSystem.viewSize.width();
	}

	public float height() {
		return LSystem.viewSize.height;
	}

	/**
	 * 返回video的缓存结果(不设置out对象时才会有效)
	 * 
	 * @return
	 */
	public ArrayByte getVideoCache() {
		return videoCache.getArrayByte();
	}

	/**
	 * 开始录像(默认使用ArrayByte缓存录像结果到内存中)
	 * 
	 * @return
	 */
	public GifEncoder startVideo() {
		return startVideo(videoCache = new ArrayByteOutput());
	}

	/**
	 * 开始录像(指定一个OutputStream对象,比如FileOutputStream 输出录像结果到指定硬盘位置)
	 * 
	 * @param output
	 * @return
	 */
	public GifEncoder startVideo(OutputStream output) {
		return startVideo(output, LSystem.isDesktop() ? _sinceRefreshMaxInterval
				: _sinceRefreshMaxInterval + _sinceRefreshMaxInterval / 2);
	}

	/**
	 * 开始录像(指定一个OutputStream对象,比如FileOutputStream 输出录像结果到指定硬盘位置)
	 * 
	 * @param output
	 * @param delay
	 * @return
	 */
	public GifEncoder startVideo(OutputStream output, long delay) {
		stopVideo();
		videoDelay.setDelay(delay);
		gifEncoder = new GifEncoder();
		gifEncoder.start(output);
		gifEncoder.setDelay((int) delay);
		_videoScreenToGif = true;
		return gifEncoder;
	}

	/**
	 * 结束录像
	 * 
	 * @return
	 */
	public GifEncoder stopVideo() {
		if (gifEncoder != null) {
			gifEncoder.finish();
		}
		_videoScreenToGif = false;
		return gifEncoder;
	}

	public final LTimerContext getUpdate() {
		return updateClock;
	}

	public final LTimerContext getPaint() {
		return paintClock;
	}

	public Display resize(int viewWidth, int viewHeight) {
		if (_closed) {
			return this;
		}
		_process.resize(viewWidth, viewHeight);
		if (_glEx != null) {
			_glEx.resize();
		}
		return this;
	}

	public Display setScreen(Screen screen) {
		if (_closed) {
			return this;
		}
		_process.setScreen(screen);
		return this;
	}

	public Display resume() {
		if (_closed) {
			return this;
		}
		_process.resume();
		return this;
	}

	public Display pause() {
		if (_closed) {
			return this;
		}
		_process.pause();
		return this;
	}

	public IFont getDisplayFont() {
		return _displayFont;
	}

	public Display setDisplayFont(IFont displayFont) {
		this._displayFont = displayFont;
		return this;
	}

	public LProcess getProcess() {
		return _process;
	}

	public LGame getGame() {
		return _game;
	}

	public boolean isClosed() {
		return this._closed;
	}

	@Override
	public void close() {
		this._closed = true;
		this.stopAutoDisplay();
		if (this._displayFont != null) {
			this._displayFont.close();
			this._displayFont = null;
		}
		if (this.logoTex != null) {
			this.logoTex.close();
			this.logoTex = null;
		}
		if (this._process != null) {
			_process.close();
		}
		this.initDrawConfig = logDisplayCreated = false;
	}

}
