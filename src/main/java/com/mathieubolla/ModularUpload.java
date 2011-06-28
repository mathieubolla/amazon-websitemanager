package com.mathieubolla;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mathieubolla.io.DirectoryScanner;
import com.mathieubolla.io.S3Scanner;
import com.mathieubolla.processing.AmazonBucketListing;
import com.mathieubolla.processing.DeleteUnit;
import com.mathieubolla.ui.Ui;

public class ModularUpload {
	private Date date = new Date();
	private final DirectoryScanner directoryScanner;
	private final S3Scanner s3Scanner;
	private final Ui ui;
	private final AmazonBucketListing bucketListing;
	
	@Inject
	public ModularUpload(DirectoryScanner directoryScanner, S3Scanner s3Scanner, AmazonBucketListing bucketListing, Ui ui) {
		this.directoryScanner = directoryScanner;
		this.s3Scanner = s3Scanner;
		this.bucketListing = bucketListing;
		this.ui = ui;
	}
	
	private void process(final UploadConfiguration configuration) {
		clearBucket(configuration);
		uploadBucket(configuration);
		
		s3Scanner.processQueue();
	}
	
	private void clearBucket(UploadConfiguration configuration) {
		if (configuration.isClearBucketBeforeUpload()) {
			for (S3ObjectSummary summary : bucketListing.listObjects(configuration.getBucketName())) {
				s3Scanner.queueTask(new DeleteUnit(summary));
			}
		}
	}

	private void uploadBucket(UploadConfiguration configuration) {
		for (File file : directoryScanner.scanRegularFiles(new File(configuration.getBaseDirectory()))) {
			s3Scanner.queueTask(configuration.uploadUnitFor(file, date));
		}
	}

	public void start() {
		process(ui.configure());
	}
}
