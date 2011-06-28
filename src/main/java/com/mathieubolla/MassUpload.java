package com.mathieubolla;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class MassUpload {
	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new MainModule());
		ModularUpload upload = injector.getInstance(ModularUpload.class);
		AmazonS3 s3 = injector.getInstance(AmazonS3.class);
		
		System.setErr(new PrintStream(new File("/dev/null")));
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
			upload.process(toDos, new UploadConfiguration(baseDir, bucket, shouldClearBucketFirst));
		}
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
	
	private static class MainModule extends AbstractModule {
		@Override
		protected void configure() {
			try {
				bind(AmazonS3.class).toInstance(new AmazonS3Client(new PropertiesCredentials(new File("/home/mathieu/.ec2/credentials.properties")), new ClientConfiguration().withProtocol(Protocol.HTTP).withConnectionTimeout(5000).withMaxErrorRetry(5).withMaxConnections(10)));
			} catch (Throwable t) {
				throw new IllegalArgumentException("Can't configure Amazon S3 Client. Properties file might be missing.", t);
			}
		}
	}
}
