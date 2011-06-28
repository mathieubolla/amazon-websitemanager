package com.mathieubolla.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;

import javax.inject.Inject;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mathieubolla.UploadConfiguration;
import com.mathieubolla.io.DirectoryScanner;
import com.mathieubolla.io.S3Scanner;

public class S3Processor {
	private final Queue<WorkUnit> queue;
	private final AmazonS3 amazonS3;
	private final S3Scanner s3Scanner;
	private final DirectoryScanner directoryScanner;
	
	@Inject
	public S3Processor(AmazonS3 amazonS3, S3Scanner s3Scanner, DirectoryScanner directoryScanner, Queue<WorkUnit> queue) {
		this.amazonS3 = amazonS3;
		this.s3Scanner = s3Scanner;
		this.directoryScanner = directoryScanner;
		this.queue = queue;
	}
	
	public void processQueue() {
		List<Thread> threads = new ArrayList<Thread>();
		
		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					WorkUnit toDo = null;
					do {
						toDo = queue.poll();
						if (toDo != null) {
							toDo.doJob(amazonS3);
						}
					} while (toDo != null);
				}
			});
			threads.add(thread);
			thread.start();
		}
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException("Error: Was interrupted while waiting for job to get done");
			}
		}
	}
	
	public void clearBucket(UploadConfiguration configuration) {
		if (configuration.isClearBucketBeforeUpload()) {
			for (S3ObjectSummary summary : s3Scanner.listObjects(configuration.getBucketName())) {
				queue.add(new DeleteUnit(summary));
			}
		}
	}

	public void uploadBucket(UploadConfiguration configuration, Date date) {
		for (File file : directoryScanner.scanRegularFiles(configuration.getBaseDirectory())) {
			queue.add(configuration.uploadUnitFor(file, date));
		}
	}
}
