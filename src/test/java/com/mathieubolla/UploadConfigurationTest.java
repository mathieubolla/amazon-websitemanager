package com.mathieubolla;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Date;

import org.junit.Test;

import com.mathieubolla.processing.UploadUnit;

public class UploadConfigurationTest {
	@Test
	public void shouldConfigureUploadCommandsCorrectly() {
		Date date = new Date();
		UploadConfiguration configuration = new UploadConfiguration(new File("/some/where"), "bucket", false);
		
		UploadUnit uploadUnit = configuration.uploadUnitFor(new File("/some/where/in/the/sky"), date);
		
		assertThat(uploadUnit.getKey()).isEqualTo("in/the/sky");
	}
	
	@Test
	public void shouldConfigureUploadCommandsCorrectlyWithTrailingSlasg() {
		Date date = new Date();
		UploadConfiguration configuration = new UploadConfiguration(new File("/some/where/"), "bucket", false);
		
		UploadUnit uploadUnit = configuration.uploadUnitFor(new File("/some/where/in/the/sky"), date);
		
		assertThat(uploadUnit.getKey()).isEqualTo("in/the/sky");
	}
}
