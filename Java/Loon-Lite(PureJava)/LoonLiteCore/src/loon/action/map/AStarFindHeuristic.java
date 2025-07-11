/**
 * Copyright 2008 - 2012
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
 * @version 0.3.3
 */
package loon.action.map;

public interface AStarFindHeuristic {

	public final static int MANHATTAN = 0;

	public final static int MIXING = 1;

	public final static int DIAGONAL = 2;

	public final static int DIAGONAL_SHORT = 3;

	public final static int EUCLIDEAN = 4;

	public final static int EUCLIDEAN_NOSQR = 5;

	public final static int CLOSEST = 6;

	public final static int CLOSEST_SQUARED = 7;

	public final static int BESTFIRST = 8;

	public final static int OCTILE = 9;

	public final static int DIAGONAL_MIN = 10;

	public final static int DIAGONAL_MAX = 11;

	float getScore(float sx, float sy, float tx, float ty);

	int getTypeCode();

}
