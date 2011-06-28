package com.mathieubolla.processing;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.fest.assertions.Fail;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mathieubolla.UploadConfiguration;
import com.mathieubolla.io.DirectoryScanner;
import com.mathieubolla.io.Md5Summer;
import com.mathieubolla.io.S3KeyCache;
import com.mathieubolla.io.S3Scanner;

public class S3ProcessorTest {
	S3Processor s3Processor;
	S3Scanner mockS3Scanner;
	DirectoryScanner mockDirectoryScanner;
	S3KeyCache mockS3KeyCache;
	AmazonS3 mockAmazonS3;
	Md5Summer mockMd5Summer;
	WorkQueue mockQueue;
	UploadConfiguration mockUploadConfiguration;

	@Before
	public void setup() {
		mockS3Scanner = mock(S3Scanner.class);
		mockDirectoryScanner = mock(DirectoryScanner.class);
		mockAmazonS3 = mock(AmazonS3.class);
		mockQueue = mock(WorkQueue.class);
		mockMd5Summer = mock(Md5Summer.class);
		mockUploadConfiguration = mock(UploadConfiguration.class);
		mockS3KeyCache = mock(S3KeyCache.class);
		s3Processor = new S3Processor(mockAmazonS3, mockS3Scanner, mockS3KeyCache, mockMd5Summer, mockDirectoryScanner, mockQueue);
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

	@Test(timeout = 1000)
	public void shouldProcessQueueWith10Threads() {
		s3Processor = new S3Processor(mockAmazonS3, mockS3Scanner, mockS3KeyCache, mockMd5Summer, mockDirectoryScanner, buildNDependentTasks(10));

		s3Processor.processQueue();
	}

	private WorkQueue buildNDependentTasks(int n) {
		final AtomicInteger sharedLock = new AtomicInteger(0);
		WorkQueue queue = new WorkQueue();
		for (int i = 0; i < n; i++) {
			queue.add(lockNThreadsTask(sharedLock, n));
		}
		return queue;
	}

	private WorkUnit lockNThreadsTask(final AtomicInteger lock, final int n) {
		return new WorkUnit() {
			@Override
			public void doJob(AmazonS3 s3, S3KeyCache cache, Md5Summer md5) {
				synchronized (lock) {
					if(lock.incrementAndGet() < n) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							Fail.fail("Should not get interrupted");
						}
					} else {
						lock.notifyAll();
					}
				}
			}
		};
	}
}
