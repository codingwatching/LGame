package loon.an;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Looper;
import loon.Asyn;
import loon.LSystem;
import loon.Log;
import loon.utils.reply.Act;

public class JavaANAsyn extends Asyn.Default {

	private static final int POOL_SIZE = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
	private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);

	private static final ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE, new ThreadFactory() {
		private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

		@Override
		public Thread newThread(Runnable r) {
			Thread t = defaultFactory.newThread(r);
			t.setDaemon(true);
			t.setName("AsyncInvoker-" + THREAD_COUNTER.incrementAndGet());
			return t;
		}
	});

	public static Future<?> executeAsync(final Runnable action) {
		if (action == null) {
			return null;
		}
		return executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					action.run();
				} catch (Throwable t) {
					try {
						System.err.println("AsyncInvoker task failure [task=" + action + "]");
						t.printStackTrace();
					} catch (Throwable ignored) {
					}
				}
			}
		});
	}

	public static Future<?> executeAsyncDelayed(final Runnable action, long delayMillis) {
		if (action == null) {
			return null;
		}
		return executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					if (delayMillis > 0)
						Thread.sleep(delayMillis);
					action.run();
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				} catch (Throwable t) {
					try {
						System.err.println("AsyncInvoker delayed task failure [task=" + action + "]");
						t.printStackTrace();
					} catch (Throwable ignored) {
					}
				}
			}
		});
	}

	public static Future<?> executeAsyncWithUiCallback(final Runnable action, final Runnable uiCallback) {
		if (action == null) {
			return null;
		}
		return executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					action.run();
				} catch (Throwable t) {
					try {
						System.err.println("AsyncInvoker task failure [task=" + action + "]");
						t.printStackTrace();
					} catch (Throwable ignored) {
					}
				} finally {
					if (uiCallback != null) {
						try {
							JavaANHandlerCompat.post(new Runnable() {
								@Override
								public void run() {
									try {
										uiCallback.run();
									} catch (Throwable t) {
										try {
											System.err.println(
													"AsyncInvoker uiCallback failure [callback=" + uiCallback + "]");
											t.printStackTrace();
										} catch (Throwable ignored) {
										}
									}
								}
							});
						} catch (Throwable ignored) {
							try {
								if (Looper.myLooper() == Looper.getMainLooper()) {
									uiCallback.run();
								} else {
									new android.os.Handler(Looper.getMainLooper()).post(uiCallback);
								}
							} catch (Throwable ignored2) {
							}
						}
					}
				}
			}
		});
	}

	public static Future<?> callAsync(final Runnable action) {
		return executeAsync(action);
	}

	public static void shutdownNow() {
		try {
			executor.shutdownNow();
			executor.awaitTermination(200, TimeUnit.MILLISECONDS);
		} catch (Throwable ignored) {
		}
	}

	private final JavaANGame game;

	public JavaANAsyn(Log log, Act<? extends Object> frame, JavaANGame game) {
		super(log, frame);
		this.game = game;
	}

	protected boolean isPaused() {
		return LSystem.PAUSED;
	}

	@Override
	public void invokeLater(Runnable action) {
		if (isPaused()) {
			game.mainPlatform.runOnUI(action);
		} else {
			super.invokeLater(action);
		}
	}

	@Override
	public boolean isAsyncSupported() {
		return true;
	}

	@Override
	public void invokeAsync(final Runnable action) {
		callAsync(action);
	}
}
