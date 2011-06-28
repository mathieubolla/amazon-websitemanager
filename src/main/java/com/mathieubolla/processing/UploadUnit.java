package com.mathieubolla.processing;

import java.io.File;
import java.util.Date;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.mathieubolla.io.Md5Summer;
import com.mathieubolla.io.S3KeyCache;

public class UploadUnit extends WorkUnit {
	String bucket;
	String key;
	File file;
	Date lastChangeDate;
	
	public UploadUnit(String bucket, String key, File file, Date lastChangeDate) {
		this.bucket = bucket;
		this.file = file;
		this.key = key;
		this.lastChangeDate = lastChangeDate;
	}
	
	@Override
	public void doJob(AmazonS3 s3, S3KeyCache cache, Md5Summer md5) {
		String mime = Mimetypes.getInstance().getMimetype(file);
		
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
		
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setCacheControl("public,max-age=600");
		objectMetadata.setContentType(mime);
		objectMetadata.setLastModified(lastChangeDate);
		
		if (cache.get(bucket, key).getETag().equals(md5.hash(file))) {
			System.out.println("Cache hit: Not uploading "+key);
			return;
		}
		s3.putObject(putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead).withMetadata(objectMetadata));
		
		System.out.println("Successfully sent "+key+".");
	}
}