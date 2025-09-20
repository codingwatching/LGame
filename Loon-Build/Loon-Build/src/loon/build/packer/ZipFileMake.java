package loon.build.packer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import loon.build.sys.LSystem;
import loon.build.tools.FileUtils;

public class ZipFileMake {

	public ArrayList<EZipFile> classFileToEZipFile(String classRes) throws Exception {
		final InputStream input = Packer.class.getClassLoader().getResourceAsStream(classRes);
		final byte[] buffer = new byte[4096];
		ZipInputStream it = new ZipInputStream(input);
		ZipEntry zipentry = it.getNextEntry();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ArrayList<EZipFile> zips = new ArrayList<EZipFile>(10);
		while (zipentry != null) {
			String entryName = zipentry.getName();
			int n;
			while ((n = it.read(buffer, 0, 1024)) > -1) {
				out.write(buffer, 0, n);
			}
			out.close();
			it.closeEntry();
			zipentry = it.getNextEntry();
			final byte[] bytes = out.toByteArray();
			EZipFile resource = new EZipFile(entryName, bytes);
			out.reset();
			zips.add(resource);
		}
		it.close();
		it = null;
		return zips;
	}

	public ArrayList<ZipEntry> classResList(String resName) {
		ArrayList<ZipEntry> zips = new ArrayList<ZipEntry>(10);
		ZipInputStream input = new ZipInputStream(Packer.class.getClassLoader().getResourceAsStream(resName));
		ZipInputArchiveIterator it = new ZipInputArchiveIterator(input);
		for (; it.hasNext();) {
			ZipEntry entry = it.nextZip();
			if (entry != null) {
				zips.add(entry);
			}
		}
		try {
			input.close();
			input = null;
		} catch (IOException e) {
		}
		return zips;
	}

	public void zipFolder(String srcFolder, String destZipFile) throws Exception {
		zipFolder(srcFolder, destZipFile, true);
	}

	public void zipFolder(String srcFolder, String destZipFile, boolean remove) throws Exception {
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;
		File file = new File(destZipFile);
		if (remove) {
			if (file.exists()) {
				file.delete();
			}
		}
		FileUtils.makedirs(file);
		fileWriter = new FileOutputStream(file);
		zip = new ZipOutputStream(fileWriter);
		addFolderToZip("", srcFolder, zip);
		zip.flush();
		zip.close();
	}

	static private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {
		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip);
		} else {
			String zipName = path + LSystem.FS + folder.getName();
			int idx = zipName.indexOf(LSystem.FS);
			if (idx != -1) {
				byte[] buf = new byte[4096];
				int len;
				FileInputStream in = new FileInputStream(srcFile);
				String name = zipName.substring(idx + 1, zipName.length());
				zip.putNextEntry(new ZipEntry(name));
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
				in.close();
			}
		}
	}

	static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
		File folder = new File(srcFolder);
		for (String fileName : folder.list()) {
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + LSystem.FS + fileName, zip);
			} else {
				addFileToZip(path + LSystem.FS + folder.getName(), srcFolder + LSystem.FS + fileName, zip);
			}
		}

	}

	public static void unzipToFile(File srcFile, File destFile) throws IOException {
		try {
			if (!srcFile.exists()) {
				return;
			}
			if (!destFile.exists()) {
				FileUtils.makedirs(destFile);
			}
			InputStream inZip = new FileInputStream(srcFile);
			unzipStream(inZip, destFile.getCanonicalPath());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void unzipStream(InputStream inputStream, String descDir) {
		if (!descDir.endsWith(File.separator)) {
			descDir = descDir + File.separator;
		}
		try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
			ZipEntry zipEntry;
			for (; ((zipEntry = zipInputStream.getNextEntry()) != null);) {
				String zipEntryNameAll = zipEntry.getName();
				File outFile = new File((descDir + zipEntryNameAll).trim());
				if (!zipEntry.isDirectory()) {
					File par = outFile.getParentFile();
					if (par != null) {
						par.mkdirs();
					}
					writeFile(outFile.getAbsolutePath(), zipInputStream);
				} else {
					FileUtils.makedirs(outFile);
				}
				zipInputStream.closeEntry();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void writeFile(String filePath, ZipInputStream zipInputStream) {
		try (OutputStream o = new FileOutputStream(filePath)) {
			final byte[] buffer = new byte[4096];
			int len;
			for (; ((len = zipInputStream.read(buffer)) != -1);) {
				o.write(buffer, 0, len);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
