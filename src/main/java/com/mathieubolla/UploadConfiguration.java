package com.mathieubolla;

import java.io.File;
import java.util.Date;

import com.mathieubolla.processing.UploadUnit;

public class UploadConfiguration {
	private final File baseDirectory;
	private final String bucketName;
	private final boolean clearBucketBeforeUpload;

	public UploadConfiguration(File baseDirectory, String bucketName, boolean clearBucketBeforeUpload) {
		this.baseDirectory = baseDirectory;
		this.bucketName = bucketName;
		this.clearBucketBeforeUpload = clearBucketBeforeUpload;
	}
	
	public File getBaseDirectory() {
		return baseDirectory;
	}
	
	public String getBucketName() {
		return bucketName;
	}
	
	public boolean isClearBucketBeforeUpload() {
		return clearBucketBeforeUpload;
	}
	
	@Override
	public String toString() {
		return "Will upload " + baseDirectory + " on " + bucketName + ". Will "+(clearBucketBeforeUpload ? "" : "not ")+"clear it before uploading.";
	}
	
	UploadUnit uploadUnitFor(File file, Date date) {
		String key = file.getAbsolutePath().replaceFirst(baseDirectory.getAbsolutePath(), "");
		return new UploadUnit(bucketName, key, file, date);
	}
}
