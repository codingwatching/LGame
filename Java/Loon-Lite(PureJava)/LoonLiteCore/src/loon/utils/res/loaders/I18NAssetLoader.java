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
package loon.utils.res.loaders;

import loon.utils.I18N;

public class I18NAssetLoader extends AssetAbstractLoader<I18N> {

	private I18N _i18n;

	public I18NAssetLoader(String path, String nickname) {
		set(path, nickname);
	}

	@Override
	public I18N get() {
		return _i18n;
	}

	@Override
	public boolean completed() {
		return (_i18n = new I18N(_path)) != null;
	}

	@Override
	public PreloadItem item() {
		return PreloadItem.I18N;
	}

	@Override
	public void close() {
		if (_i18n != null) {
			_i18n.close();
			_i18n = null;
		}

	}

}
