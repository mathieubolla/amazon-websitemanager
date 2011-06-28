package com.mathieubolla;

public class UploadConfiguration {
	private final String baseDirectory;
	private final String bucketName;
	private final boolean clearBucketBeforeUpload;

	public UploadConfiguration(String baseDirectory, String bucketName, boolean clearBucketBeforeUpload) {
		this.baseDirectory = baseDirectory;
		this.bucketName = bucketName;
		this.clearBucketBeforeUpload = clearBucketBeforeUpload;
	}
	
	public String getBaseDirectory() {
		return baseDirectory;
	}
	
	public String getBucketName() {
		return bucketName;
	}
	
	public boolean isClearBucketBeforeUpload() {
		return clearBucketBeforeUpload;
	}
}
