package com.mathieubolla;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mathieubolla.io.DirectoryScanner;
import com.mathieubolla.processing.AmazonS3Processor;
import com.mathieubolla.processing.DeleteUnit;
import com.mathieubolla.processing.UploadUnit;
import com.mathieubolla.ui.SwingUi;

public class ModularUpload {
	private Date date = new Date();
	private final DirectoryScanner directoryScanner;
	private final AmazonS3 amazonS3;
	private final AmazonS3Processor amazonS3Processor;
	private final SwingUi swingUi;
	
	@Inject
	public ModularUpload(DirectoryScanner directoryScanner, AmazonS3 amazonS3, AmazonS3Processor amazonS3Processor, SwingUi swingUi) {
		this.directoryScanner = directoryScanner;
		this.amazonS3 = amazonS3;
		this.amazonS3Processor = amazonS3Processor;
		this.swingUi = swingUi;
	}
	
	public void process(final UploadConfiguration configuration) {
		if (configuration.isClearBucketBeforeUpload()) {
			clearBucket(configuration.getBucketName());
		}
		upload(configuration.getBaseDirectory(), configuration.getBucketName());
		
		amazonS3Processor.processQueue();
	}
	
	private void clearBucket(String bucket) {
		String nextMarker = null;
		do {
			ObjectListing listObjects = amazonS3.listObjects(new ListObjectsRequest().withBucketName(bucket).withMarker(nextMarker));
			nextMarker = listObjects.getNextMarker();
			for (S3ObjectSummary summary : listObjects.getObjectSummaries()) {
				amazonS3Processor.queueTask(new DeleteUnit(bucket, summary.getKey()));
			}
		} while (nextMarker != null);
	}

	private void upload(String baseDir, final String bucket) {
		for (File file : directoryScanner.scanRegularFiles(new File(baseDir))) {
			String key = file.getAbsolutePath().replaceFirst(baseDir, "");
			amazonS3Processor.queueTask(new UploadUnit(bucket, key, file, date));
		}
	}

	public void upload() {
		UploadConfiguration configuration = swingUi.configure();
		process(configuration);
	}
}
