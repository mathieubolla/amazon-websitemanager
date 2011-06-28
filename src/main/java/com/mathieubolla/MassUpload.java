package com.mathieubolla;

import static com.google.inject.name.Names.named;

import java.io.File;
import java.io.PrintStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Named;
import javax.swing.JOptionPane;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.mathieubolla.ui.SwingUi;

public class MassUpload {
	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new MainModule());
		ModularUpload upload = injector.getInstance(ModularUpload.class);
		SwingUi ui = injector.getInstance(SwingUi.class);
		
		System.setErr(new PrintStream(new File("/dev/null")));
		final Queue<WorkUnit> toDos = new ConcurrentLinkedQueue<WorkUnit>();

		UploadConfiguration configuration = ui.configure();
		if (confirm(message(configuration))) {
			upload.process(toDos, configuration);
		}
	}

	private static String message(UploadConfiguration configuration) {
		return "Will upload " + configuration.getBaseDirectory() + " on " + configuration.getBucketName() + ". Will "+(configuration.isClearBucketBeforeUpload() ? "" : "not ")+"clear it before uploading. Is this what you want?";
	}

	private static boolean confirm(String message) {
		return JOptionPane.showConfirmDialog(null, message) == JOptionPane.YES_OPTION;
	}

	public static class MainModule extends AbstractModule {
		@Override
		protected void configure() {
			bindConstant().annotatedWith(named("ec2credentials")).to(System.getProperty("user.home") + "/.ec2/credentials.properties");
		}
		
		@Provides
		protected AmazonS3 configureS3Client(@Named("ec2credentials") String credentialsPath) {
			try {
				return new AmazonS3Client(new PropertiesCredentials(new File(credentialsPath)), new ClientConfiguration().withProtocol(Protocol.HTTP).withConnectionTimeout(5000).withMaxErrorRetry(5).withMaxConnections(10));
			} catch (Throwable t) {
				throw new IllegalArgumentException("Can't configure Amazon S3 Client. Properties file might be missing.", t);
			}
		}
	}
}
