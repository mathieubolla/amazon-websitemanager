package com.mathieubolla;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mathieubolla.io.DirectoryScanner;

public class MassUpload {
	static final Date UPLOAD_START_DATE = new Date();
	
	static final AtomicLong TOTAL_SIZE_SENT = new AtomicLong(0);
	static final AtomicLong TOTAL_SIZE_TOSEND = new AtomicLong(0);
	private static DirectoryScanner directoryScanner = new DirectoryScanner();
	
	public static void main(String[] args) throws Exception {
		System.setErr(new PrintStream(new File("/dev/null")));
		final AmazonS3 s3 = new AmazonS3Client(new PropertiesCredentials(new File("/home/mathieu/.ec2/credentials.properties")), new ClientConfiguration().withProtocol(Protocol.HTTP).withConnectionTimeout(5000).withMaxErrorRetry(5).withMaxConnections(10));
		final Queue<WorkUnit> toDos = new ConcurrentLinkedQueue<WorkUnit>();

		final String baseDir = chooseBaseDir() + "/";
		if (baseDir.equals("null/")) {
			return;
		}
		
		final String bucket = chooseBucket(s3, baseDir);
		if (bucket == null) {
			return;
		}
		
		final boolean shouldClearBucketFirst = chooseClearBucket(baseDir, bucket);
		
		if (confirm(message(baseDir, bucket, shouldClearBucketFirst))) {
			if (shouldClearBucketFirst) {
				System.out.println("Will clear "+bucket);
				clearBucket(s3, bucket, toDos);
			}
			System.out.println("Will upload "+baseDir+" to "+bucket);
			upload(baseDir, s3, bucket, toDos);
			
			System.out.println("Last chance to cancel...");
			Thread.sleep(5000);
			System.out.println("Go!");
			
			processQueue(s3, toDos);
		}
	}
	
	private static void processQueue(final AmazonS3 s3, final Queue<WorkUnit> toDos) {
		for (int i = 0; i < 10; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					WorkUnit toDo = null;
					do {
						toDo = toDos.poll();
						if (toDo != null) {
							toDo.doJob(s3);
						}
					} while (toDo != null);
				}
			}).start();
		}
	}
	
	private static void clearBucket(AmazonS3 s3, String bucket, Queue<WorkUnit> toDos) {
		String nextMarker = null;
		do {
			ObjectListing listObjects = s3.listObjects(new ListObjectsRequest().withBucketName(bucket).withMarker(nextMarker));
			nextMarker = listObjects.getNextMarker();
			for (S3ObjectSummary summary : listObjects.getObjectSummaries()) {
				toDos.add(new DeleteUnit(bucket, summary.getKey()));
			}
		} while (nextMarker != null);
	}

	private static String message(final String baseDir, final String bucket, final boolean shouldClearBucketFirst) {
		return "Will upload " + baseDir + " on " + bucket + ". Will "+(shouldClearBucketFirst ? "" : "not ")+"clear it before uploading. Is this what you want?";
	}
	
	private static boolean confirm(String message) {
		return JOptionPane.showConfirmDialog(null, message) == JOptionPane.YES_OPTION;
	}
	
	private static String chooseBucket(AmazonS3 s3, String forDirectory) {
		List<String> options = new ArrayList<String>();
		for (Bucket bucket : s3.listBuckets()) {
			options.add(bucket.getName());
		}
		return options.get(JOptionPane.showOptionDialog(null, "Choose your Amazon S3 bucket to upload "+forDirectory, "Bucket Choice", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.toArray()[0]));
	}
	
	private static boolean chooseClearBucket(String baseDir, String bucket) {
		return JOptionPane.showOptionDialog(null, "Do you want to clear "+bucket+" prior to uploading "+baseDir+"?", "Clear Choice", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION) == JOptionPane.YES_OPTION;
	}

	private static String chooseBaseDir() {
		String baseDir = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int showOpenDialogAnswer = fileChooser.showOpenDialog(null);
		
		if (showOpenDialogAnswer == JFileChooser.APPROVE_OPTION) {
			baseDir = fileChooser.getSelectedFile().getAbsolutePath();
		}
		return baseDir;
	}

	private static void upload(String baseDir, final AmazonS3 s3, final String bucket, final Queue<WorkUnit> toDos) {
		for (File file : directoryScanner.scanRegularFiles(new File(baseDir))) {
			String key = file.getAbsolutePath().replaceFirst(baseDir, "");
			toDos.add(new UploadUnit(bucket, key, file));
			TOTAL_SIZE_TOSEND.addAndGet(file.length());
		}
	}
}
