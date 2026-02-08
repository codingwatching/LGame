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
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 工具类,用于清理不必要的宏设置
 */
public class CMacroCleaner {

	class ConditionState {
		boolean skipCurrent;
		boolean relatedToTarget;
		boolean removeDirectives;

		ConditionState(boolean skipCurrent, boolean relatedToTarget, boolean removeDirectives) {
			this.skipCurrent = skipCurrent;
			this.relatedToTarget = relatedToTarget;
			this.removeDirectives = removeDirectives;
		}
	}

	class ConditionParser {

		private final Set<String> targetMacros;
		private final Map<String, Pattern> macroPatterns;

		public ConditionParser(Set<String> targetMacros) {
			this.targetMacros = targetMacros;
			this.macroPatterns = new HashMap<>();
			for (String macro : targetMacros) {
				macroPatterns.put(macro, Pattern.compile("(^|[^A-Za-z0-9_])" + macro + "([^A-Za-z0-9_]|$)"));
			}
		}

		public boolean containsTargetMacro(String expr) {
			for (Pattern p : macroPatterns.values()) {
				if (p.matcher(expr).find()) {
					return true;
				}
			}
			return false;
		}

		public boolean onlyTargetMacros(String expr) {
			boolean hasTarget = false;
			boolean hasNonTarget = false;

			String[] tokens = expr.split("[^A-Za-z0-9_]+");
			for (String token : tokens) {
				if (token.isEmpty())
					continue;
				if (targetMacros.contains(token)) {
					hasTarget = true;
				} else if (Character.isUpperCase(token.charAt(0))) {
					hasNonTarget = true;
				}
			}
			return hasTarget && !hasNonTarget;
		}

		public String rewrite(String expr) {
			String rewritten = expr;
			for (Pattern p : macroPatterns.values()) {
				rewritten = p.matcher(rewritten).replaceAll("0");
			}
			rewritten = rewritten.replaceAll("defined\\s*\\(\\s*0\\s*\\)", "0");
			rewritten = rewritten.replaceAll("defined\\s*\\(\\s*[A-Za-z0-9_]+\\s*\\)", "1");
			rewritten = rewritten.replaceAll("IS_ENABLED\\s*\\(\\s*0\\s*\\)", "0");
			rewritten = rewritten.replaceAll("IS_ENABLED\\s*\\(\\s*1\\s*\\)", "1");
			return simplifyExpression(rewritten);
		}

		private String simplifyExpression(String expr) {
			String simplified = expr.replaceAll("\\s+", " ");
			simplified = simplified.replaceAll("0 && [A-Za-z0-9_]+", "0");
			simplified = simplified.replaceAll("0 \\* [0-9]+", "0");
			simplified = simplified.replaceAll("[0-9]+ \\* 0", "0");
			simplified = simplified.replaceAll("0 \\+ ([0-9]+)", "$1");
			simplified = simplified.replaceAll("([0-9]+) \\+ 0", "$1");
			if (simplified.trim().equals("0")) {
				return "0";
			}
			if (simplified.trim().equals("1")) {
				return "1";
			}
			return simplified.trim();
		}
	}

	private final Set<String> macroNames;
	private final ConditionParser parser;
	private final String targetPath;

	public CMacroCleaner(Set<String> macroNames, String targetPath) {
		this.macroNames = macroNames;
		this.targetPath = targetPath;
		this.parser = new ConditionParser(macroNames);
	}

	public void cleanMacros(Path rootDir) throws IOException {
		Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.toString().contains(targetPath) && Files.isRegularFile(file) && isSourceFile(file)) {
					processFile(file);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private boolean isSourceFile(Path file) {
		String name = file.getFileName().toString().toLowerCase();
		return name.endsWith(".c") || name.endsWith(".h") || name.endsWith(".cpp");
	}

	private void processFile(Path file) throws IOException {
		List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
		if (lines.size() == 0) {
			return;
		}
		List<String> cleaned = new ArrayList<String>();

		Deque<ConditionState> stack = new ArrayDeque<ConditionState>();
		ConditionState current = new ConditionState(false, false, false);
		boolean promotedElif = false;

		boolean[] inBlockComment = { false };

		for (int i = 0; i < lines.size(); i++) {
			String rawLine = lines.get(i);
			String line = stripComments(rawLine, inBlockComment);

			if (isMacroDefineOrUndef(line)) {
				continue;
			}

			if (line.startsWith("#if") || line.startsWith("#ifdef") || line.startsWith("#ifndef")) {
				int endIndex = findMatchingEnd(lines, i);
				if (parser.onlyTargetMacros(line)) {
					int elseIndex = -1;
					for (int j = i + 1; j <= endIndex; j++) {
						String innerLine = stripComments(lines.get(j).trim(), inBlockComment);
						if (innerLine.startsWith("#else")) {
							elseIndex = j;
							break;
						}
					}
					if (elseIndex != -1) {
						for (int j = elseIndex + 1; j < endIndex; j++) {
							String elseLine = lines.get(j);
							String stripped = stripComments(elseLine, inBlockComment);
							if (!stripped.startsWith("#endif")) {
								cleaned.add(elseLine);
							}
						}
					}
					i = endIndex;
					continue;
				}

				boolean related = parser.containsTargetMacro(line);
				stack.push(current);

				if (related) {
					String rewritten = parser.rewrite(line);
					cleaned.add(rewritten);
					current = new ConditionState(false, true, false);
				} else {
					cleaned.add(rawLine);
					current = new ConditionState(false, false, false);
				}
				continue;
			}

			if (line.startsWith("#else")) {
				if (current.relatedToTarget && current.skipCurrent) {
					continue;
				}
				cleaned.add(rawLine);
				continue;
			}

			if (line.startsWith("#elif")) {
				boolean related = parser.containsTargetMacro(line);
				if (related) {
					if (parser.onlyTargetMacros(line)) {
						current = new ConditionState(true, true, true);
					} else {
						String rewritten = parser.rewrite(line);
						if (current.removeDirectives && !promotedElif) {
							rewritten = rewritten.replaceFirst("#elif", "#if");
							promotedElif = true;
							current.removeDirectives = false;
						}
						cleaned.add(rewritten);
						current = new ConditionState(false, true, false);
					}
				} else {
					String newLine = rawLine;
					if (current.removeDirectives && !promotedElif) {
						newLine = rawLine.replaceFirst("#elif", "#if");
						promotedElif = true;
						current.removeDirectives = false;
					}
					cleaned.add(newLine);
					current = new ConditionState(false, false, false);
				}
				continue;
			}

			if (line.startsWith("#endif")) {
				if (!stack.isEmpty()) {
					current = stack.pop();
				}
				if (current.relatedToTarget && current.skipCurrent) {
					continue;
				}
				cleaned.add(rawLine);
				continue;
			}

			if (!current.skipCurrent) {
				cleaned.add(rawLine);
			}
		}

		if (!cleaned.equals(lines)) {
			Files.write(file, cleaned, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
		}
	}

	private boolean isMacroDefineOrUndef(String line) {
		for (String macro : macroNames) {
			if (line.startsWith("#define " + macro) || line.startsWith("#undef " + macro)) {
				return true;
			}
		}
		return false;
	}

	private int findMatchingEnd(List<String> lines, int startIndex) {
		int depth = 0;
		boolean[] inBlockComment = { false };
		for (int i = startIndex; i < lines.size(); i++) {
			String line = stripComments(lines.get(i).trim(), inBlockComment);
			if (line.startsWith("#if") || line.startsWith("#ifdef") || line.startsWith("#ifndef")) {
				depth++;
			} else if (line.startsWith("#endif")) {
				depth--;
				if (depth == 0) {
					return i;
				}
			}
		}
		return lines.size() - 1;
	}

	private String stripComments(String line, boolean[] inBlockComment) {
		StringBuilder sbr = new StringBuilder();
		int i = 0;
		while (i < line.length()) {
			if (inBlockComment[0]) {
				int end = line.indexOf("*/", i);
				if (end == -1) {
					return "";
				} else {
					inBlockComment[0] = false;
					i = end + 2;
				}
			} else {
				if (i + 1 < line.length() && line.charAt(i) == '/' && line.charAt(i + 1) == '*') {
					inBlockComment[0] = true;
					i += 2;
				} else if (i + 1 < line.length() && line.charAt(i) == '/' && line.charAt(i + 1) == '/') {
					break;
				} else {
					sbr.append(line.charAt(i));
					i++;
				}
			}
		}
		return sbr.toString().trim();
	}
}
