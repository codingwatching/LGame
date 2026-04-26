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
import loon.action.sprite.ArrowPath;
import loon.geom.Vector2f;
import loon.utils.TArray;

public class ArrowPathTest extends Stage{

	@Override
	public void create() {
		
		 // 构建一个基于瓦片大小32像素的华丽方向箭头
	     ArrowPath arrowPath = new ArrowPath(32);
         // 加粗8px 
	     arrowPath.setThickness(8f);
	     // 箭头大小32px 
	     arrowPath.setArrowSize(32f);
	     // 设置虚线模式，虚线长18px，宽10px
	     arrowPath.setDashed(true, 18f, 10f);
	     // 渐变色设置
	     // arrowPath.setGradient(new LColor(1f, 0.6f, 0.2f, 1f), new LColor(1f, 0.1f, 0.1f, 1f));
	     // 不自动适配，直接设置瓦片大小为32x32
	     // arrowPath.setGridSize(32,false);
	     // 路线在瓦片自动居中 
	     // arrowPath.setCenterAlign(true);
	     // 直接传入瓦片坐标(1,3) → (4,9) 的箭头指向
	     TArray<Vector2f> tilePath = new TArray<Vector2f>();
	     tilePath.add(new Vector2f(1, 3));
	     tilePath.add(new Vector2f(4, 9));

	     // 自动转换瓦片路径为像素路径，使用8方向模式
	     arrowPath.setPathFromTiles(tilePath, ArrowPath.Mode.EIGHT);
	  // 创建移动路径(战棋移动染格模式)
	  /* 
	     TArray<Vector2f> movePath = new TArray<Vector2f>();
	     movePath.add(new Vector2f(2,2)); // 起点
	     movePath.add(new Vector2f(2,5)); // 拐点
	     movePath.add(new Vector2f(6,5)); // 终点

	     // 设置瓦片大小（默认状态会自动适配样式）
	     arrowPath.setGridSize(48);
	     // 切换为移动路线模式
	     arrowPath.setPathFromTiles(movePath, ArrowPath.Mode.PATH);
         // 蓝色发光
	     arrowPath.setGlow(true, LColor.blue, 1f); 
	      // 实线/虚线切换
	     arrowPath.setDashed(false, 0,0);
         */
	     up((x,y)->{
	    	 // 让箭头指向鼠标点击位置
	    	 arrowPath.pathToPixelPos(new Vector2f(x,y));
	     });
	     
	        add(arrowPath);
	}

}
