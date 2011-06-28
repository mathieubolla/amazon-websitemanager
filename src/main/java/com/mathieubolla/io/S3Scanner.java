package com.mathieubolla.io;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

import com.amazonaws.services.s3.AmazonS3;
import com.mathieubolla.processing.WorkUnit;

public class S3Scanner {
	private final Queue<WorkUnit> toDos = new ConcurrentLinkedQueue<WorkUnit>();
	private final AmazonS3 amazonS3;
	
	@Inject
	public S3Scanner(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}
	
	public void queueTask(WorkUnit task) {
		toDos.add(task);
	}
	
	public void processQueue() {
		for (int i = 0; i < 10; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					WorkUnit toDo = null;
					do {
						toDo = toDos.poll();
						if (toDo != null) {
							toDo.doJob(amazonS3);
						}
					} while (toDo != null);
				}
			}).start();
		}
	}
}
