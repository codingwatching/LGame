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
package loon.android;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class AndroidHandlerCompat {

	private AndroidHandlerCompat() {
	}

	private static volatile Handler mainHandler;

	private static final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));

	public static Handler getMainHandler() {
		if (mainHandler == null) {
			synchronized (AndroidHandlerCompat.class) {
				if (mainHandler == null) {
					mainHandler = createAsyncHandlerSafely(Looper.getMainLooper());
				}
			}
		}
		return mainHandler;
	}

	private static Handler createAsyncHandlerSafely(Looper looper) {
		try {
			if (Build.VERSION.SDK_INT >= 28) {
				Method m = Handler.class.getMethod("createAsync", Looper.class);
				Object obj = m.invoke(null, looper);
				if (obj instanceof Handler) {
					return (Handler) obj;
				}
			}
		} catch (Throwable ignored) {
		}
		return new Handler(looper);
	}

	public static boolean post(Runnable r) {
		try {
			return getMainHandler().post(wrapRunnable(r));
		} catch (Throwable t) {
			return false;
		}
	}

	public static boolean postDelayed(Runnable r, long delayMillis) {
		try {
			return getMainHandler().postDelayed(wrapRunnable(r), delayMillis);
		} catch (Throwable t) {
			return false;
		}
	}

	public static void removeCallbacks(Runnable r) {
		try {
			getMainHandler().removeCallbacks(r);
		} catch (Throwable ignored) {
		}
	}

	public static void removeCallbacksAndMessages(Object token) {
		try {
			getMainHandler().removeCallbacksAndMessages(token);
		} catch (Throwable ignored) {
		}
	}

	public static void postOnBackground(Runnable r) {
		try {
			scheduler.execute(r);
		} catch (Throwable ignored) {
		}
	}

	public static ScheduledFuture<?> schedule(Runnable r, long delayMillis) {
		try {
			return scheduler.schedule(r, Math.max(0, delayMillis), TimeUnit.MILLISECONDS);
		} catch (Throwable t) {
			return scheduler.schedule(() -> {
			}, 0, TimeUnit.MILLISECONDS);
		}
	}

	public static void cancelScheduled(ScheduledFuture<?> future) {
		if (future != null) {
			try {
				future.cancel(false);
			} catch (Throwable ignored) {
			}
		}
	}

	private static Runnable wrapRunnable(Runnable r) {
		return r;
	}

	public static abstract class WeakRunnable<T> implements Runnable {
		private final WeakReference<T> ref;

		public WeakRunnable(T owner) {
			this.ref = new WeakReference<>(owner);
		}

		@Override
		public final void run() {
			T owner = ref.get();
			if (owner != null) {
				runWithOwner(owner);
			}
		}

		protected abstract void runWithOwner(T owner);
	}

	public static void shutdownForLifecycle() {
		try {
			getMainHandler().removeCallbacksAndMessages(null);
		} catch (Throwable ignored) {
		}
		try {
			scheduler.shutdownNow();
		} catch (Throwable ignored) {
		}
	}
}