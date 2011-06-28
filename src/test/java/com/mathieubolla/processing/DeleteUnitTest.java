package com.mathieubolla.processing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class DeleteUnitTest {
	@Test
	public void shouldDeleteSummaryOnS3() {
		S3ObjectSummary mockS3ObjectSummary = mock(S3ObjectSummary.class);
		AmazonS3 mockAmazonS3 = mock(AmazonS3.class);
		when(mockS3ObjectSummary.getBucketName()).thenReturn("bucket");
		when(mockS3ObjectSummary.getKey()).thenReturn("key");
		
		DeleteUnit deleteUnit = new DeleteUnit(mockS3ObjectSummary);
		
		deleteUnit.doJob(mockAmazonS3, null, null);
		
		verify(mockAmazonS3).deleteObject("bucket", "key");
	}
}
