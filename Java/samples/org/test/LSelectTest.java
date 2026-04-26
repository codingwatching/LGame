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
package org.test;

import loon.Stage;
import loon.canvas.LColor;
import loon.component.LSelect;

public class LSelectTest extends Stage {

	@Override
	public void create() {
		setBackground(LColor.red);
		LSelect select = new LSelect(0, 0, 200, 250);

		select.setMessage(new String[] { "选项1", "选项2", "选项3", "选项4", "选项5", "选项6" });
		// 设置回调
		select.setOnSelectListener(new LSelect.OnSelectListener() {
			public void onSelectionChanged(LSelect select, int index, String item) {
				// 选中变化
				System.out.println("update : " + item);
			}

			public void onItemConfirmed(LSelect select, int index, String item) {
				// 确认选择
				System.out.println("selected : " + item);
			}
		});
		select.up((x, y) -> {

		});
		add(select);
	}

}
