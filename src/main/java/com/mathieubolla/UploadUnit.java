package com.mathieubolla;

import java.io.File;
import java.util.Date;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

class UploadUnit extends WorkUnit {
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
	public void doJob(AmazonS3 s3) {
		String mime = Mimetypes.getInstance().getMimetype(file);
		
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
		
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setCacheControl("public,max-age=600");
		objectMetadata.setContentType(mime);
		objectMetadata.setLastModified(lastChangeDate);
		
		s3.putObject(putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead).withMetadata(objectMetadata));
		
		System.out.println("Successfully sent "+key+".");
	}
}