package com.mathieubolla.processing;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class DeleteUnit extends WorkUnit {
	S3ObjectSummary summary;
	
	public DeleteUnit(S3ObjectSummary summary) {
		this.summary = summary;
	}

	@Override
	public void doJob(AmazonS3 s3) {
		s3.deleteObject(summary.getBucketName(), summary.getKey());
		System.out.println("Successfully deleted "+summary);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DeleteUnit) {
			return summary.equals(((DeleteUnit)obj).summary);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return summary.hashCode();
	}
}