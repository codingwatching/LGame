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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import loon.utils.PathUtils;
import loon.utils.StringUtils;
import loon.utils.TArray;

public class JarZipExtractor {

	public static void extractMetaInfoZipFromJar(String zipName, String outputDir) throws IOException {
		extractZipFromJar(JarZipExtractor.class, "META-INF/" + zipName, outputDir, true);
	}

	public static void extractZipFromJar(Class<?> clazz, String zipName, String outputDir, boolean overwrite)
			throws IOException {
		if (StringUtils.isEmpty(PathUtils.getExtension(zipName))) {
			zipName += ".zip";
		}
		File dstJarFile = new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
		if (dstJarFile.isFile() && (dstJarFile.getName().endsWith(".jar") || dstJarFile.getName().endsWith(".tar"))) {
			try (JarFile jarFile = new JarFile(dstJarFile)) {
				JarEntry entry = jarFile.getJarEntry(zipName);
				if (entry == null) {
					throw new FileNotFoundException("The specified zip file was not found :" + zipName);
				}
				try (InputStream is = jarFile.getInputStream(entry); ZipInputStream zis = new ZipInputStream(is)) {
					unzipStream(zis, outputDir, overwrite);
				}
			}
		} else {
			ClassLoader classLoader = clazz.getClassLoader();
			String resourcePath = zipName;
			if (!resourcePath.startsWith("META-INF/")) {
				resourcePath = "META-INF/" + resourcePath;
			}
			URL resourceUrl = classLoader.getResource(resourcePath);
			if (resourceUrl != null) {
				try (InputStream is = resourceUrl.openStream(); ZipInputStream zis = new ZipInputStream(is)) {
					unzipStream(zis, outputDir, overwrite);
				}
			} else {
				File file = new File("src/" + resourcePath);
				if (!file.exists()) {
					file = new File("bin/" + resourcePath);
				}
				if (!file.exists()) {
					throw new FileNotFoundException("The specified zip file was not found :" + zipName);
				}
				try (InputStream is = Files.newInputStream(file.toPath());
						ZipInputStream zis = new ZipInputStream(is)) {
					unzipStream(zis, outputDir, overwrite);
				}
			}
		}
	}

	private static void unzipStream(ZipInputStream zis, String outputDir, boolean overwrite) throws IOException {
		ZipEntry zipEntry;
		while ((zipEntry = zis.getNextEntry()) != null) {
			File outFile = new File(outputDir, zipEntry.getName());
			if (zipEntry.isDirectory()) {
				outFile.mkdirs();
			} else {
				outFile.getParentFile().mkdirs();
				if (!(outFile.exists() && !overwrite)) {
					try (OutputStream os = Files.newOutputStream(outFile.toPath())) {
						byte[] buffer = new byte[8192];
						int len;
						while ((len = zis.read(buffer)) > 0) {
							os.write(buffer, 0, len);
						}
					}
				}
			}
			zis.closeEntry();
		}
	}

	public static Path extractZipFromJarToTemp(Class<?> clazz, String zipName) throws IOException {
		Path tempDir = Files.createTempDirectory("jarZipExtract");
		extractZipFromJar(clazz, zipName, tempDir.toString(), true);
		return tempDir;
	}

	public static TArray<String> listZipsInSelfJar() throws IOException {
		String jarPath = new File(JarZipExtractor.class.getProtectionDomain().getCodeSource().getLocation().getPath())
				.getAbsolutePath();
		TArray<String> result = new TArray<String>();
		try (JarFile jarFile = new JarFile(jarPath)) {
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (!entry.isDirectory() && entry.getName().startsWith("META-INF/")
						&& entry.getName().endsWith(".zip")) {
					result.add(entry.getName());
				}
			}
		}
		return result;
	}

	public static Path findFile(Path rootDir, String fileName) throws IOException {
		Path found = null;
		DirectoryStream<Path> stream = Files.newDirectoryStream(rootDir);
		try {
			found = searchRecursively(stream, fileName);
		} finally {
			stream.close();
		}
		return found;
	}

	private static Path searchRecursively(DirectoryStream<Path> stream, String fileName) throws IOException {
		for (Path path : stream) {
			if (Files.isDirectory(path)) {
				DirectoryStream<Path> subStream = Files.newDirectoryStream(path);
				Path result = searchRecursively(subStream, fileName);
				subStream.close();
				if (result != null) {
					return result;
				}
			} else {
				if (path.getFileName().toString().contains(fileName)) {
					return path;
				}
			}
		}
		return null;
	}

	public static String readFileContent(Path filePath) throws IOException {
		StringBuilder sbr = new StringBuilder();
		BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sbr.append(line).append(System.lineSeparator());
			}
		} finally {
			reader.close();
		}
		return sbr.toString();
	}

}
