package com.mathieubolla;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mathieubolla.io.DirectoryScanner;
import com.mathieubolla.io.S3Scanner;
import com.mathieubolla.processing.DeleteUnit;
import com.mathieubolla.processing.S3Processor;
import com.mathieubolla.processing.UploadUnit;
import com.mathieubolla.ui.Ui;


public class ModularUploadTest {
	S3Scanner mockS3Scanner;
	S3Processor mockS3Processor;
	DirectoryScanner mockDirectoryScanner;
	Ui mockUi;
	File mockSourceDirectory;
	File mockRegularFile;
	Date mockDate;
	
	ModularUpload modularUpload;
	
	@Before
	public void setup() {
		mockS3Scanner = mock(S3Scanner.class);
		mockS3Processor = mock(S3Processor.class);
		mockDirectoryScanner = mock(DirectoryScanner.class);
		mockUi = mock(Ui.class);
		mockSourceDirectory = mock(File.class);
		mockRegularFile = mock(File.class);
		mockDate = mock(Date.class);
		
		modularUpload = new ModularUpload(mockDirectoryScanner, mockS3Processor, mockS3Scanner, mockUi);
	}

	@Test
	public void shouldUploadFilesToBucket() {
		UploadUnit mockTask = mock(UploadUnit.class);
		UploadConfiguration mockUploadConfiguration = mock(UploadConfiguration.class);
		
		when(mockUi.configure()).thenReturn(mockUploadConfiguration);
		when(mockUploadConfiguration.getBaseDirectory()).thenReturn(mockSourceDirectory);
		when(mockDirectoryScanner.scanRegularFiles(mockSourceDirectory)).thenReturn(asList(mockRegularFile));
		when(mockUploadConfiguration.uploadUnitFor(mockRegularFile, mockDate)).thenReturn(mockTask);
		
		modularUpload.start(mockDate);
		
		InOrder inOrder = inOrder(mockS3Processor);
		inOrder.verify(mockS3Processor).queueTask(mockTask);
		inOrder.verify(mockS3Processor).processQueue();
	}
	
	@Test
	public void shouldDeleteExistingFilesInBucketAndUploadFilesToBucket() {
		UploadUnit mockUploadTask = mock(UploadUnit.class);
		UploadConfiguration mockUploadConfiguration = mock(UploadConfiguration.class);
		S3ObjectSummary mockS3ObjectSummary = mock(S3ObjectSummary.class);
		when(mockUi.configure()).thenReturn(mockUploadConfiguration);
		when(mockUploadConfiguration.getBaseDirectory()).thenReturn(mockSourceDirectory);
		when(mockUploadConfiguration.getBucketName()).thenReturn("bucket");
		when(mockDirectoryScanner.scanRegularFiles(mockSourceDirectory)).thenReturn(asList(mockRegularFile));
		when(mockS3Scanner.listObjects("bucket")).thenReturn(asList(mockS3ObjectSummary));
		when(mockUploadConfiguration.uploadUnitFor(mockRegularFile, mockDate)).thenReturn(mockUploadTask);
		
		when(mockUploadConfiguration.isClearBucketBeforeUpload()).thenReturn(true);

		modularUpload.start(mockDate);
		
		InOrder inOrder = inOrder(mockS3Processor);
		inOrder.verify(mockS3Processor).queueTask(new DeleteUnit(mockS3ObjectSummary));
		inOrder.verify(mockS3Processor).queueTask(mockUploadTask);
		inOrder.verify(mockS3Processor).processQueue();
	}
}
