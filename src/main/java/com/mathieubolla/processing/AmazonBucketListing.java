package com.mathieubolla.processing;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AmazonBucketListing {
	private final AmazonS3 amazonS3;

	@Inject
	public AmazonBucketListing(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}
	
	public List<S3ObjectSummary> listObjects(String bucket) {
		List<S3ObjectSummary> s3Objects = new ArrayList<S3ObjectSummary>();
		
		String nextMarker = null;
		do {
			ObjectListing listObjects = amazonS3.listObjects(new ListObjectsRequest().withBucketName(bucket).withMarker(nextMarker));
			nextMarker = listObjects.getNextMarker();
			for (S3ObjectSummary summary : listObjects.getObjectSummaries()) {
				s3Objects.add(summary);
			}
		} while (nextMarker != null);
		
		return s3Objects;
	}
}
