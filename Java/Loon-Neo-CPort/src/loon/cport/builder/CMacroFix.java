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
package loon.cport.builder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class CMacroFix {

	static class MacroReplacement {
		String ifReplacement;
		String elseReplacement;

		MacroReplacement(String ifReplacement, String elseReplacement) {
			this.ifReplacement = ifReplacement;
			this.elseReplacement = elseReplacement;
		}
	}

	static class MacroInjection {
		String injectionText;
		boolean injectOnce;
		// FIRST, LAST, DEFAULT
		String insertionRule;

		MacroInjection(String injectionText, boolean injectOnce, String insertionRule) {
			this.injectionText = injectionText;
			this.injectOnce = injectOnce;
			this.insertionRule = insertionRule;
		}
	}

	public static void processFile(String filePath, Map<String, MacroReplacement> replacements,
			Map<String, MacroInjection> injections) throws IOException {

		if (filePath == null || replacements == null) {
			return;
		}
		if (injections == null) {
			injections = new HashMap<String, MacroInjection>();
		}

		List<String> lines = new ArrayList<String>();
		String lineSeparator = System.getProperty("line.separator");

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		Map<String, List<Integer>> macroEndPositions = new HashMap<>();
		Stack<String> macroStack = new Stack<String>();

		for (int i = 0; i < lines.size(); i++) {
			String trimLine = lines.get(i).trim();
			if (trimLine.startsWith("#if") || trimLine.startsWith("#ifdef")) {
				String matchedMacro = null;
				for (String macro : replacements.keySet()) {
					if (trimLine.matches(".*\\b" + macro + "\\b.*")) {
						matchedMacro = macro;
						break;
					}
				}
				if (matchedMacro == null) {
					for (String macro : injections.keySet()) {
						if (trimLine.matches(".*\\b" + macro + "\\b.*")) {
							matchedMacro = macro;
							break;
						}
					}
				}
				macroStack.push(matchedMacro);
			} else if (trimLine.startsWith("#endif")) {
				if (!macroStack.isEmpty()) {
					String endedMacro = macroStack.pop();
					if (endedMacro != null) {
						macroEndPositions.computeIfAbsent(endedMacro, k -> new ArrayList<>()).add(i);
					}
				}
			}
		}

		Map<Integer, List<String>> insertMap = new HashMap<Integer, List<String>>();
		for (String macro : injections.keySet()) {
			MacroInjection inj = injections.get(macro);
			List<Integer> positions = macroEndPositions.get(macro);
			int insertPos = -1;

			if (positions != null && !positions.isEmpty()) {
				if ("FIRST".equals(inj.insertionRule)) {
					insertPos = positions.get(0);
				} else if ("LAST".equals(inj.insertionRule)) {
					insertPos = positions.get(positions.size() - 1);
				} else {
					insertPos = positions.get(positions.size() - 1);
				}
			}

			if (insertPos >= 0) {
				insertMap.computeIfAbsent(insertPos, k -> new ArrayList<>()).add(macro);
			}
		}

		StringBuilder result = new StringBuilder();
		Set<String> injectedMacros = new HashSet<String>();

		for (int i = 0; i < lines.size(); i++) {
			result.append(lines.get(i)).append(lineSeparator);

			if (insertMap.containsKey(i)) {
				for (String macro : insertMap.get(i)) {
					MacroInjection inj = injections.get(macro);
					if (inj != null) {
						if (inj.injectOnce) {
							if (!injectedMacros.contains(macro)) {
								result.append(inj.injectionText).append(lineSeparator);
								injectedMacros.add(macro);
							}
						} else {
							result.append(inj.injectionText).append(lineSeparator);
						}
					}
				}
			}
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(result.toString());
		}
	}

}
