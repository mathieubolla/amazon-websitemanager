package com.mathieubolla;

import java.io.File;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

class UploadUnit extends WorkUnit {
	String bucket;
	String key;
	File file;
	
	public UploadUnit(String bucket, String key, File file) {
		this.bucket = bucket;
		this.file = file;
		this.key = key;
	}
	
	@Override
	public void doJob(AmazonS3 s3) {
		String mime = Mimetypes.getInstance().getMimetype(file);
		
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
		
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setCacheControl("public,max-age=600");
		objectMetadata.setContentType(mime);
		objectMetadata.setLastModified(MassUpload.UPLOAD_START_DATE);
		
		s3.putObject(putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead).withMetadata(objectMetadata));
		
		long size = file.length();
		long kbSent = size / 1024;
		long totalKbSent = MassUpload.TOTAL_SIZE_SENT.addAndGet(size) / 1024;
		double percentage = (double)Math.round((double)MassUpload.TOTAL_SIZE_SENT.get() / MassUpload.TOTAL_SIZE_TOSEND.get() * 10000) / 100;
		System.out.println("Successfully sent "+kbSent+"kb to "+key+". Total "+totalKbSent+"kb out of "+(MassUpload.TOTAL_SIZE_TOSEND.get()/1024)+"kb ("+percentage+"%)");
	}
}