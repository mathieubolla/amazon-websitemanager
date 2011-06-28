package com.mathieubolla.ui;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.inject.Inject;

import com.google.inject.name.Named;
import com.mathieubolla.UploadConfiguration;

public class BatchUi implements Ui {
	private final String path;

	@Inject
	public BatchUi(@Named("batch.path") String path) {
		this.path = path;
	}
	
	@Override
	public UploadConfiguration configure() {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(new File(path)));
			
			String baseDirectory = properties.getProperty("baseDirectory");
			if (baseDirectory == null || baseDirectory.equals("")) {
				throw new IllegalArgumentException("baseDirectory property not found");
			}
			
			String bucketName = properties.getProperty("bucketName");
			if (bucketName == null || bucketName.equals("")) {
				throw new IllegalArgumentException("bucketName property not found");
			}
			
			boolean clearBucket = Boolean.parseBoolean(properties.getProperty("clearBucket", "false"));
			
			return new UploadConfiguration(new File(baseDirectory), bucketName, clearBucket);
		} catch (Throwable t) {
			throw new IllegalArgumentException("Can't load property file. Aborting.", t);
		}
	}
}
