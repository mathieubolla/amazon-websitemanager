package com.mathieubolla.processing;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Date;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mathieubolla.UploadConfiguration;
import com.mathieubolla.io.DirectoryScanner;
import com.mathieubolla.io.S3Scanner;


public class S3ProcessorTest {
	S3Processor s3Processor;
	S3Scanner mockS3Scanner;
	DirectoryScanner mockDirectoryScanner;
	AmazonS3 mockAmazonS3;
	Queue<WorkUnit> mockQueue;
	UploadConfiguration mockUploadConfiguration;
	
	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		mockS3Scanner = mock(S3Scanner.class);
		mockDirectoryScanner = mock(DirectoryScanner.class);
		mockAmazonS3 = mock(AmazonS3.class);
		mockQueue = mock(Queue.class);
		mockUploadConfiguration = mock(UploadConfiguration.class);
		s3Processor = new S3Processor(mockAmazonS3, mockS3Scanner, mockDirectoryScanner, mockQueue);
	}
	
	@Test
	public void shouldAcceptDeleteCommands() {
		UploadConfiguration mockUploadConfiguration = mock(UploadConfiguration.class);
		S3ObjectSummary mockS3ObjectSummary = mock(S3ObjectSummary.class);
		
		when(mockUploadConfiguration.getBucketName()).thenReturn("bucket");
		when(mockS3Scanner.listObjects("bucket")).thenReturn(asList(mockS3ObjectSummary));
		
		when(mockUploadConfiguration.isClearBucketBeforeUpload()).thenReturn(true);

		s3Processor.clearBucket(mockUploadConfiguration);
		
		verify(mockQueue).add(new DeleteUnit(mockS3ObjectSummary));
	}
	
	@Test
	public void shouldRefuseDeleteCommands() {
		
		S3ObjectSummary mockS3ObjectSummary = mock(S3ObjectSummary.class);
		
		when(mockUploadConfiguration.getBucketName()).thenReturn("bucket");
		when(mockS3Scanner.listObjects("bucket")).thenReturn(asList(mockS3ObjectSummary));

		when(mockUploadConfiguration.isClearBucketBeforeUpload()).thenReturn(false);
		
		s3Processor.clearBucket(mockUploadConfiguration);
		
		verifyNoMoreInteractions(mockQueue);
	}
	
	@Test
	public void shouldAcceptUploadCommands() {
		Date mockDate = mock(Date.class);
		UploadUnit mockUploadUnit = mock(UploadUnit.class);
		File mockFile = mock(File.class);
		File mockDirectory = mock(File.class);
		when(mockDirectoryScanner.scanRegularFiles(mockDirectory)).thenReturn(asList(mockFile));
		when(mockUploadConfiguration.getBaseDirectory()).thenReturn(mockDirectory);
		when(mockUploadConfiguration.uploadUnitFor(mockFile, mockDate)).thenReturn(mockUploadUnit);
		
		s3Processor.uploadBucket(mockUploadConfiguration, mockDate);
		
		verify(mockQueue).add(mockUploadUnit);
	}
}
