package com.mathieubolla;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mathieubolla.io.S3Scanner;
import com.mathieubolla.io.DirectoryScanner;
import com.mathieubolla.processing.DeleteUnit;
import com.mathieubolla.processing.S3Processor;
import com.mathieubolla.ui.Ui;

public class ModularUpload {
	private final DirectoryScanner directoryScanner;
	private final S3Processor s3processor;
	private final Ui ui;
	private final S3Scanner s3scanner;
	
	@Inject
	public ModularUpload(DirectoryScanner directoryScanner, S3Processor s3processor, S3Scanner s3scanner, Ui ui) {
		this.directoryScanner = directoryScanner;
		this.s3processor = s3processor;
		this.s3scanner = s3scanner;
		this.ui = ui;
	}
	
	private void clearBucket(UploadConfiguration configuration) {
		if (configuration.isClearBucketBeforeUpload()) {
			for (S3ObjectSummary summary : s3scanner.listObjects(configuration.getBucketName())) {
				s3processor.queueTask(new DeleteUnit(summary));
			}
		}
	}

	private void uploadBucket(UploadConfiguration configuration, Date date) {
		for (File file : directoryScanner.scanRegularFiles(configuration.getBaseDirectory())) {
			s3processor.queueTask(configuration.uploadUnitFor(file, date));
		}
	}

	public void start(Date date) {
		UploadConfiguration configuration = ui.configure();
		
		clearBucket(configuration);
		uploadBucket(configuration, date);
		s3processor.processQueue();
	}
}
