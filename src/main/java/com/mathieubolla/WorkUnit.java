package com.mathieubolla;

import com.amazonaws.services.s3.AmazonS3;

abstract class WorkUnit {
	public abstract void doJob(AmazonS3 s3);
}