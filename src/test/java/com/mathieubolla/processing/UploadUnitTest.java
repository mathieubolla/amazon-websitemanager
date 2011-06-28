package com.mathieubolla.processing;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mathieubolla.io.Md5Summer;
import com.mathieubolla.io.S3KeyCache;


public class UploadUnitTest {
	File mockFile;
	Date mockDate;
	Md5Summer mockMd5Summer;
	S3KeyCache mockCache;
	S3ObjectSummary mockS3ObjectSummary;
	AmazonS3 mockAmazonS3;
	
	@Before
	public void setup() {
		mockFile = mock(File.class);
		mockDate = mock(Date.class);
		mockMd5Summer = mock(Md5Summer.class);
		mockCache = mock(S3KeyCache.class);
		mockS3ObjectSummary = mock(S3ObjectSummary.class);
		mockAmazonS3 = mock(AmazonS3.class);
	}
	
	@Test
	public void shouldUploadFile() {
		UploadUnit uploadUnit = new UploadUnit("bucket", "key", mockFile, mockDate);
		when(mockS3ObjectSummary.getETag()).thenReturn("validMD5Hash");
		when(mockFile.getName()).thenReturn("sample.jpg");
		when(mockCache.get("bucket", "key")).thenReturn(mockS3ObjectSummary);
		when(mockMd5Summer.hash(mockFile)).thenReturn("invalidMD5Hash");
		ArgumentCaptor<PutObjectRequest> objectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
		
		uploadUnit.doJob(mockAmazonS3, mockCache, mockMd5Summer);
		
		verify(mockAmazonS3).putObject(objectRequestCaptor.capture());
		validate(objectRequestCaptor);
	}
	
	@Test
	public void shouldNotUploadSameFileTwice() {
		UploadUnit uploadUnit = new UploadUnit("bucket", "key", mockFile, mockDate);
		when(mockFile.getName()).thenReturn("sample.jpg");
		when(mockCache.get("bucket", "key")).thenReturn(mockS3ObjectSummary);
		when(mockS3ObjectSummary.getETag()).thenReturn("sameMD5Hash");
		when(mockMd5Summer.hash(mockFile)).thenReturn("sameMD5Hash");
		
		uploadUnit.doJob(mockAmazonS3, mockCache, mockMd5Summer);
		
		verifyNoMoreInteractions(mockAmazonS3);
	}

	private void validate(ArgumentCaptor<PutObjectRequest> objectRequestCaptor) {
		PutObjectRequest value = objectRequestCaptor.getValue();
		assertThat(value.getBucketName()).isEqualTo("bucket");
		assertThat(value.getCannedAcl()).isEqualTo(CannedAccessControlList.PublicRead);
		assertThat(value.getFile()).isEqualTo(mockFile);
		assertThat(value.getKey()).isEqualTo("key");
		assertThat(value.getMetadata().getCacheControl()).isEqualTo("public,max-age=600");
		assertThat(value.getMetadata().getContentType()).isEqualTo("image/jpeg");
		assertThat(value.getMetadata().getLastModified()).isEqualTo(mockDate);
	}
}
