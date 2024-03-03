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
package loon.action;

import loon.LSystem;
import loon.canvas.LColor;
import loon.utils.Easing;
import loon.utils.MathUtils;
import loon.utils.StringUtils;

/*
 * 一个非常简单的动作脚本字符串设置模式，以"move()->delay()->move()-..."这类方式,
 * 设置一个 具体ActionTween的行为.只支持常用命令.
 * 
 * 用例:移动到127,127位置,每次移动16个像素(true为八方走法，false为四方走法),旋转360度,延迟2秒后,淡入(60帧完成),淡出(90帧完成)
 * 写法：move(127,127,true,16)->rotate(360)->delay(2)->fadein(60)->fadeout(90)
 * 
 * 在Screen可以写作如下格式:
 * 
 * 	@Override
 public void onLoad() {
 Sprite sprite = new Sprite("assets/ball.png");
 ActionScript script = act(sprite, "move(127,127,true,16)->rotate(360)->delay(2f)->fadein(60)->fadeout(90)");
 add(sprite);
 script.start();
 }
 */
public class ActionScript {

	private ActionTween _tween;

	private String _src;

	public ActionScript(ActionTween tween, String script) {
		_tween = tween;
		_src = script;
	}

	protected final float convertToFloat(String num) {
		if (MathUtils.isNan(num)) {
			return Float.parseFloat(num);
		}
		return -1f;
	}

	public TweenTo<ActionTween> start() {
		if (_tween != null) {
			if (_src != null) {
				String result = StringUtils.replace(_src, LSystem.LS, LSystem.EMPTY);
				String[] list = StringUtils.split(result, "->");
				for (String cmd : list) {
					if (cmd.length() > 0) {

						int start = cmd.indexOf(LSystem.PAREN_START);
						int end = cmd.lastIndexOf(LSystem.PAREN_END);

						String name = null;
						String[] parameters = null;

						if (start != -1 && end != -1 && end > start) {
							name = cmd.substring(0, start).trim().toLowerCase();
							parameters = StringUtils.split(cmd.substring(start + 1, end), LSystem.COMMA);
						} else {
							name = cmd.trim().toLowerCase();
						}

						if ("resume".equals(name) || "resumeto".equals(name)) {
							_tween.resume();
						} else if ("delay".equals(name) || "delayto".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.delay(convertToFloat(parameters[0]));
								}
							}
						} else if ("move".equals(name) || "moveto".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 2) {
									_tween.moveTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]));
								} else if (parameters.length == 3) {
									if (StringUtils.isBoolean(parameters[2])) {
										_tween.moveTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]),
												StringUtils.toBoolean(parameters[2]));
									} else {
										_tween.moveTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]),
												MathUtils.ifloor(convertToFloat(parameters[2])));
									}
								} else if (parameters.length == 4) {
									_tween.moveTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]),
											StringUtils.toBoolean(parameters[2]),
											MathUtils.ifloor(convertToFloat(parameters[3])));
								}
							}
						} else if ("moveby".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 2) {
									_tween.moveBy(convertToFloat(parameters[0]), convertToFloat(parameters[1]));
								} else if (parameters.length == 3) {
									_tween.moveBy(convertToFloat(parameters[0]), convertToFloat(parameters[1]),
											MathUtils.ifloor(convertToFloat(parameters[2])));
								}
							}
						} else if ("rotate".equals(name) || "rotateto".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.rotateTo(convertToFloat(parameters[0]));
								} else if (parameters.length == 2) {
									_tween.rotateTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]));
								}
							}
						} else if ("rotate".equals(name) || "rotateto".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.rotateTo(convertToFloat(parameters[0]));
								} else if (parameters.length == 2) {
									_tween.rotateTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]));
								}
							}
						} else if ("fadein".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.fadeIn(convertToFloat(parameters[0]));
								}
							} else {
								_tween.fadeIn(60);
							}
						} else if ("fadeout".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.fadeOut(convertToFloat(parameters[0]));
								}
							} else {
								_tween.fadeOut(60);
							}
						} else if ("color".equals(name) || "colorto".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.colorTo(new LColor(parameters[0]));
								} else if (parameters.length == 2) {
									_tween.colorTo(new LColor(parameters[0]), new LColor(parameters[1]));
								} else if (parameters.length == 3) {
									_tween.colorTo(new LColor(parameters[0]), new LColor(parameters[1]),
											convertToFloat(parameters[2]));
								} else if (parameters.length == 4) {
									_tween.colorTo(new LColor(parameters[0]), new LColor(parameters[1]),
											convertToFloat(parameters[2]), convertToFloat(parameters[3]));
								}
							} else {
								_tween.colorTo(LColor.white.cpy());
							}
						} else if ("transformpos".equals(name) || "transpos".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 2) {
									_tween.transformPos(convertToFloat(parameters[0]), convertToFloat(parameters[1]));
								}
							}
						} else if ("transformscale".equals(name) || "transscale".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 2) {
									_tween.transformScale(convertToFloat(parameters[0]), convertToFloat(parameters[1]));
								}
							}
						} else if ("transformalpha".equals(name) || "transalpha".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.transformAlpha(convertToFloat(parameters[0]));
								}
							}
						} else if ("transformrotation".equals(name) || "transrotation".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.transformRotation(convertToFloat(parameters[0]));
								}
							}
						} else if ("transformcolor".equals(name) || "transcolor".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.transformColor(LColor.decode(parameters[0]));
								} else if (parameters.length == 3) {
									_tween.transformColor(new LColor(convertToFloat(parameters[0]),
											convertToFloat(parameters[1]), convertToFloat(parameters[2])));
								} else if (parameters.length == 4) {
									_tween.transformColor(new LColor(convertToFloat(parameters[0]),
											convertToFloat(parameters[1]), convertToFloat(parameters[2])));
								}
							}
						} else if ("shake".equals(name) || "shaketo".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 2) {
									_tween.shakeTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]));
								} else if (parameters.length == 3) {
									_tween.shakeTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]),
											convertToFloat(parameters[2]));
								} else if (parameters.length == 4) {
									_tween.shakeTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]),
											convertToFloat(parameters[2]), convertToFloat(parameters[3]));
								}
							}
						} else if ("transfer".equals(name) || "transferto".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 4) {
									_tween.transferTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]),
											convertToFloat(parameters[2]), Easing.toEasingMode(parameters[3]));
								} else if (parameters.length == 6) {
									_tween.transferTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]),
											convertToFloat(parameters[2]), Easing.toEasingMode(parameters[3]),
											StringUtils.toBoolean(parameters[4]), StringUtils.toBoolean(parameters[5]));
								} else if (parameters.length == 7) {
									_tween.transferTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]),
											convertToFloat(parameters[2]), convertToFloat(parameters[3]),
											Easing.toEasingMode(parameters[4]), StringUtils.toBoolean(parameters[5]),
											StringUtils.toBoolean(parameters[6]));
								}
							}
						} else if ("scale".equals(name) || "scaleto".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									float scale = convertToFloat(parameters[0]);
									_tween.scaleTo(scale, scale);
								} else if (parameters.length == 2) {
									_tween.scaleTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]));
								} else if (parameters.length == 3) {
									_tween.scaleTo(convertToFloat(parameters[0]), convertToFloat(parameters[1]),
											convertToFloat(parameters[2]));
								}
							}
						} else if ("show".equals(name) || "showto".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.showTo(StringUtils.toBoolean(parameters[0]));
								}
							}
						} else if ("repeat".equals(name) || "repeatto".equals(name)) {
							if (parameters != null) {
								if (parameters.length == 1) {
									_tween.repeat(convertToFloat(parameters[0]));
								} else if (parameters.length == 2) {
									_tween.repeat(MathUtils.ifloor(convertToFloat(parameters[0])),
											convertToFloat(parameters[1]));
								}
							} else {
								_tween.repeat(1f);
							}
						}
					}
				}
			}
			return _tween.start();
		}
		return null;
	}

	public ActionTween getTween() {
		return _tween;
	}

	public void setTween(ActionTween tween) {
		this._tween = tween;
	}

	public String getScript() {
		return _src;
	}

	public void setScript(String script) {
		this._src = script;
	}

}
