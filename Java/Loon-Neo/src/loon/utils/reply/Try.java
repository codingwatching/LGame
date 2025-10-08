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
package loon.utils.reply;

import loon.LSysException;

public abstract class Try<T> {

	public static <T> Try<T> createSuccess(final T value) {
		return new Success<T>(value);
	}

	public static <T> Try<T> createFailure(final Throwable cause) {
		return new Failure<T>(cause);
	}

	public static final class Success<T> extends Try<T> {
		public final T value;

		public Success(final T value) {
			this.value = value;
		}

		@Override
		public T get() {
			return value;
		}

		@Override
		public Throwable getFailure() {
			throw new LSysException("Failure");
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public <R> Try<R> map(final Function<? super T, R> func) {
			try {
				return createSuccess(func.apply(value));
			} catch (Throwable t) {
				return createFailure(t);
			}
		}

		@Override
		public String toString() {
			return "Success(" + value + ")";
		}
	}

	public static final class Failure<T> extends Try<T> {

		public final Throwable cause;

		public Failure(final Throwable cause) {
			this.cause = cause;
		}

		@Override
		public T get() {
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				throw (RuntimeException) new RuntimeException().initCause(cause);
			}
		}

		@Override
		public Throwable getFailure() {
			return cause;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public <R> Try<R> map(final Function<? super T, R> func) {
			return this.<R>casted();
		}

		@Override
		public String toString() {
			return "Failure(" + cause + ")";
		}

		@SuppressWarnings("unchecked")
		private <R> Try<R> casted() {
			return (Try<R>) this;
		}
	}

	public abstract T get();

	public abstract Throwable getFailure();

	public abstract boolean isSuccess();

	public boolean isFailure() {
		return !isSuccess();
	}

	public abstract <R> Try<R> map(final Function<? super T, R> func);

	private Try() {
	}
}
