package com.mathieubolla.processing;

import com.amazonaws.services.s3.AmazonS3;
import com.mathieubolla.io.Md5Summer;
import com.mathieubolla.io.S3KeyCache;

public abstract class WorkUnit {
	public abstract void doJob(AmazonS3 s3, S3KeyCache cache, Md5Summer md5);
}