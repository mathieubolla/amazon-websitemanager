package com.mathieubolla;

import java.io.File;
import java.util.Date;
import java.util.Queue;

import javax.inject.Inject;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mathieubolla.io.DirectoryScanner;

public class ModularUpload {
	private Date date = new Date();
	private final DirectoryScanner directoryScanner;
	private final AmazonS3 amazonS3;
	
	@Inject
	public ModularUpload(DirectoryScanner directoryScanner, AmazonS3 amazonS3) {
		this.directoryScanner = directoryScanner;
		this.amazonS3 = amazonS3;
	}
	
	public void process(final Queue<WorkUnit> toDos, final UploadConfiguration configuration) throws InterruptedException {
		if (configuration.isClearBucketBeforeUpload()) {
			System.out.println("Will clear "+configuration.getBucketName());
			clearBucket(configuration.getBucketName(), toDos);
		}
		System.out.println("Will upload "+configuration.getBaseDirectory()+" to "+configuration.getBucketName());
		upload(configuration.getBaseDirectory(), configuration.getBucketName(), toDos);
		
		System.out.println("Last chance to cancel...");
		Thread.sleep(5000);
		System.out.println("Go!");
		
		processQueue(toDos);
	}
	
	private void processQueue(final Queue<WorkUnit> toDos) {
		for (int i = 0; i < 10; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					WorkUnit toDo = null;
					do {
						toDo = toDos.poll();
						if (toDo != null) {
							toDo.doJob(amazonS3);
						}
					} while (toDo != null);
				}
			}).start();
		}
	}
	
	private void clearBucket(String bucket, Queue<WorkUnit> toDos) {
		String nextMarker = null;
		do {
			ObjectListing listObjects = amazonS3.listObjects(new ListObjectsRequest().withBucketName(bucket).withMarker(nextMarker));
			nextMarker = listObjects.getNextMarker();
			for (S3ObjectSummary summary : listObjects.getObjectSummaries()) {
				toDos.add(new DeleteUnit(bucket, summary.getKey()));
			}
		} while (nextMarker != null);
	}

	private void upload(String baseDir, final String bucket, final Queue<WorkUnit> toDos) {
		for (File file : directoryScanner.scanRegularFiles(new File(baseDir))) {
			String key = file.getAbsolutePath().replaceFirst(baseDir, "");
			toDos.add(new UploadUnit(bucket, key, file, date));
		}
	}
}
