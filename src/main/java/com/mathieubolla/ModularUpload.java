package com.mathieubolla;

import java.util.Date;

import javax.inject.Inject;

import com.mathieubolla.processing.S3Processor;
import com.mathieubolla.ui.Ui;

public class ModularUpload {
	private final S3Processor s3processor;
	private final Ui ui;
	
	@Inject
	public ModularUpload(S3Processor s3processor, Ui ui) {
		this.s3processor = s3processor;
		this.ui = ui;
	}

	public void start(Date date) {
		UploadConfiguration configuration = ui.configure();
		
		System.out.println(configuration);
		
		s3processor.clearBucket(configuration);
		s3processor.uploadBucket(configuration, date);
		s3processor.processQueue();
	}
}