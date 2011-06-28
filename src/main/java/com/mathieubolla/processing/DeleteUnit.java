package com.mathieubolla.processing;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;

public class DeleteUnit extends WorkUnit {
	String bucket;
	String key;
	
	public DeleteUnit(String bucket, String key) {
		super();
		this.bucket = bucket;
		this.key = key;
	}

	@Override
	public void doJob(AmazonS3 s3) {
		s3.deleteObject(new DeleteObjectRequest(bucket, key));
		System.out.println("Successfully deleted "+bucket+"/"+key);
	}
}