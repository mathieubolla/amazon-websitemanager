package com.mathieubolla.processing;

import com.amazonaws.services.s3.AmazonS3;

public abstract class WorkUnit {
	public abstract void doJob(AmazonS3 s3);
}