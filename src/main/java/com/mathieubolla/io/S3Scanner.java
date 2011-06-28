package com.mathieubolla.io;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.fest.util.VisibleForTesting;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3Scanner {
	private final AmazonS3 amazonS3;

	@Inject
	public S3Scanner(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}
	
	public List<S3ObjectSummary> listObjects(String bucket) {
		List<S3ObjectSummary> s3Objects = new ArrayList<S3ObjectSummary>();
		
		String nextMarker = null;
		do {
			ObjectListing listObjects = amazonS3.listObjects(instanciateRequest(bucket, nextMarker));
			nextMarker = listObjects.getNextMarker();
			for (S3ObjectSummary summary : listObjects.getObjectSummaries()) {
				s3Objects.add(summary);
			}
		} while (nextMarker != null);
		
		return s3Objects;
	}

	@VisibleForTesting
	protected ListObjectsRequest instanciateRequest(String bucket, String nextMarker) {
		return new ListObjectsRequest().withBucketName(bucket).withMarker(nextMarker);
	}
}
