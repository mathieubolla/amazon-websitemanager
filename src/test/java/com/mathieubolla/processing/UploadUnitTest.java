package com.mathieubolla.processing;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Date;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;


public class UploadUnitTest {
	@Test
	public void shouldUploadFile() {
		File mockFile = mock(File.class);
		Date mockDate = mock(Date.class);
		AmazonS3 mockAmazonS3 = mock(AmazonS3.class);
		when(mockFile.getName()).thenReturn("sample.jpg");
		UploadUnit uploadUnit = new UploadUnit("bucket", "key", mockFile, mockDate);
		
		uploadUnit.doJob(mockAmazonS3);
		
		ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
		
		verify(mockAmazonS3).putObject(captor.capture());
		PutObjectRequest value = captor.getValue();
		assertThat(value.getBucketName()).isEqualTo("bucket");
		assertThat(value.getCannedAcl()).isEqualTo(CannedAccessControlList.PublicRead);
		assertThat(value.getFile()).isEqualTo(mockFile);
		assertThat(value.getKey()).isEqualTo("key");
		assertThat(value.getMetadata().getCacheControl()).isEqualTo("public,max-age=600");
		assertThat(value.getMetadata().getContentType()).isEqualTo("image/jpeg");
		assertThat(value.getMetadata().getLastModified()).isEqualTo(mockDate);
	}
}
