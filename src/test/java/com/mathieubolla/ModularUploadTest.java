package com.mathieubolla;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.mathieubolla.processing.S3Processor;
import com.mathieubolla.ui.Ui;

public class ModularUploadTest {
	S3Processor mockS3Processor;
	Ui mockUi;
	Date mockDate;
	UploadConfiguration mockUploadConfiguration;
	
	ModularUpload modularUpload;
	
	@Before
	public void setup() {
		mockS3Processor = mock(S3Processor.class);
		mockUi = mock(Ui.class);
		mockDate = mock(Date.class);
		mockUploadConfiguration = mock(UploadConfiguration.class);
		
		modularUpload = new ModularUpload(mockS3Processor, mockUi);
	}

	@Test
	public void shouldAttemptDeleteThenUploadThenProcess() {
		when(mockUi.configure()).thenReturn(mockUploadConfiguration);
		
		modularUpload.start(mockDate);
		
		InOrder inOrder = inOrder(mockS3Processor);
		inOrder.verify(mockS3Processor).clearBucket(mockUploadConfiguration);
		inOrder.verify(mockS3Processor).uploadBucket(mockUploadConfiguration, mockDate);
		inOrder.verify(mockS3Processor).processQueue();
	}
}
