/**
 * Copyright 2008 - 2020 The Loon Game Engine Authors
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
package loon.action.behaviors;

public class Sequence<T> extends Composite<T> {

	public Sequence() {
		this(AbortTypes.None);
	}

	public Sequence(AbortTypes abortType) {
		this.abortType = abortType;
	}

	@Override
	public TaskStatus update(T context) {
		if (_currentChildIndex != 0) {
			handleAborts(context, TaskStatus.Success);
		}

		Behavior<T> current = _children.get(_currentChildIndex);
		TaskStatus status = current.tick(context);

		if (status != TaskStatus.Success) {
			return status;
		}

		_currentChildIndex++;

		if (_currentChildIndex == _children.size) {
			_currentChildIndex = 0;
			return TaskStatus.Success;
		}

		return TaskStatus.Running;
	}

}