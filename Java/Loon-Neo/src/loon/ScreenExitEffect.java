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
package loon;

import loon.action.sprite.ISprite;
import loon.action.sprite.effect.FadeEffect;
import loon.canvas.LColor;

public class ScreenExitEffect {

	public static void gotoFadeEffectExit(final Screen src, final Screen dst, final LColor color) {
		final FadeEffect e = FadeEffect.create(ISprite.TYPE_FADE_OUT, color);
		e.setCompletedAfterBlack(true);
		e.completedDispose(new LRelease() {

			@Override
			public void close() {
				dst.setTransition(LTransition.newFade(ISprite.TYPE_FADE_IN, color));
				src.setScreen(dst);
			}
		});
		src.setLock(true);
		src.lastSpriteDraw();
		src.add(e);
	}
}
