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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMacroUpdater {

	static class MacroRule {
		String oldMacro;
		String newMacro;
		String newContent;
		int occurrenceIndex;

		MacroRule(String oldMacro, String newMacro, String newContent, int occurrenceIndex) {
			this.oldMacro = oldMacro;
			this.newMacro = newMacro;
			this.newContent = newContent;
			this.occurrenceIndex = occurrenceIndex;
		}
	}

	public static void replaceInFolder(String rootDir, Map<String, List<MacroRule>> fileRules) throws IOException {
		Path startPath = Paths.get(rootDir);

		Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				String filePath = file.toString();
				if (fileRules.containsKey(filePath)) {
					replaceComplexBlocks(filePath, fileRules.get(filePath));
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static MacroRule replaceComplexBlocks(String filePath, List<MacroRule> rules) throws IOException {
		Path path = Paths.get(filePath);
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		List<String> newLines = new ArrayList<String>();

		Map<String, Integer> macroCount = new HashMap<String, Integer>();
		boolean inTargetBlock = false;
		MacroRule activeRule = null;

		for (String line : lines) {
			String trimmed = line.trim();

			if (!inTargetBlock) {
				if (trimmed.startsWith("#if") || trimmed.startsWith("#elif")) {
					String replacedCondition = trimmed;
					List<MacroRule> matchedRules = new ArrayList<MacroRule>();

					for (MacroRule rule : rules) {
						if (trimmed.contains(rule.oldMacro)) {
							int count = macroCount.getOrDefault(rule.oldMacro, 0);
							macroCount.put(rule.oldMacro, count + 1);

							if (count == rule.occurrenceIndex) {
								replacedCondition = replacedCondition.replaceAll("\\b" + rule.oldMacro + "\\b",
										rule.newMacro);
								matchedRules.add(rule);
							}
						}
					}

					if (!matchedRules.isEmpty()) {
						newLines.add(replacedCondition);
						for (MacroRule rule : matchedRules) {
							for (String newLine : rule.newContent.split("\n")) {
								if (!newLine.isEmpty()) {
									newLines.add(newLine);
								}
							}
						}
						inTargetBlock = true;
						activeRule = matchedRules.get(0);
					} else {
						newLines.add(line);
					}
				} else {
					newLines.add(line);
				}
			} else {
				if (trimmed.startsWith("#elif") || trimmed.startsWith("#else") || trimmed.startsWith("#endif")) {
					newLines.add(line);
					inTargetBlock = false;
					activeRule = null;
				}
			}
		}
		Files.write(path, newLines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
		return activeRule;
	}

}
