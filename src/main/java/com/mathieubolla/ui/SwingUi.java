package com.mathieubolla.ui;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.mathieubolla.UploadConfiguration;

public class SwingUi implements Ui {
	private final AmazonS3 amazonS3;

	@Inject
	public SwingUi(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}
	
	public UploadConfiguration configure() {
		final String baseDir = chooseBaseDir() + "/";
		if (baseDir.equals("null/")) {
			throw new IllegalArgumentException("User choosed to abort");
		}
		
		final String bucket = chooseBucket(baseDir);
		if (bucket == null) {
			throw new IllegalArgumentException("User choosed to abort");
		}
		
		final boolean shouldClearBucketFirst = chooseClearBucket(baseDir, bucket);
		
		UploadConfiguration configuration = new UploadConfiguration(baseDir, bucket, shouldClearBucketFirst);
		if (confirm(configuration.toString())) {
			return configuration;
		}
		
		throw new IllegalArgumentException("User choosed to abort");
	}
	
	private String chooseBucket(String forDirectory) {
		List<String> options = new ArrayList<String>();
		for (Bucket bucket : amazonS3.listBuckets()) {
			options.add(bucket.getName());
		}
		return options.get(JOptionPane.showOptionDialog(null, "Choose your Amazon S3 bucket to upload "+forDirectory, "Bucket Choice", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.toArray()[0]));
	}
	
	private boolean chooseClearBucket(String baseDir, String bucket) {
		return JOptionPane.showOptionDialog(null, "Do you want to clear "+bucket+" prior to uploading "+baseDir+"?", "Clear Choice", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION) == JOptionPane.YES_OPTION;
	}

	private String chooseBaseDir() {
		String baseDir = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int showOpenDialogAnswer = fileChooser.showOpenDialog(null);
		
		if (showOpenDialogAnswer == JFileChooser.APPROVE_OPTION) {
			baseDir = fileChooser.getSelectedFile().getAbsolutePath();
		}
		return baseDir;
	}

	private boolean confirm(String message) {
		return JOptionPane.showConfirmDialog(null, message) == JOptionPane.YES_OPTION;
	}
}
