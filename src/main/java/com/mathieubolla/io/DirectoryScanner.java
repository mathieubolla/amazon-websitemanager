package com.mathieubolla.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryScanner {
	public List<File> scanRegularFiles(File sourceDirectory) {
		List<File> files = new ArrayList<File>();
		for (File f : sourceDirectory.listFiles()) {
			if (f.getName().startsWith(".")) {
				continue;
			}
			
			if (f.isFile()) {
				files.add(f);
			}
			if (f.isDirectory()) {
				files.addAll(scanRegularFiles(f));
			}
		}
		return files;
	}
}