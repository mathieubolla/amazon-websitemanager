package com.mathieubolla.io;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3ScannerTest {
	AmazonS3 mockAmazonS3;
	S3Scanner scanner;
	
	@Before
	public void setup() {
		mockAmazonS3 = mock(AmazonS3.class);
		
		scanner = spy(new S3Scanner(mockAmazonS3));
	}
	
	@Test
	public void shouldListEmptySummaries() {
		ObjectListing mockObjectListing = mock(ObjectListing.class);
		ListObjectsRequest mockListObjectRequest = mock(ListObjectsRequest.class);
		when(scanner.instanciateRequest("bucket", null)).thenReturn(mockListObjectRequest);

		when(mockAmazonS3.listObjects(mockListObjectRequest)).thenReturn(mockObjectListing);
		when(mockObjectListing.getObjectSummaries()).thenReturn(new ArrayList<S3ObjectSummary>());
		
		List<S3ObjectSummary> listObjects = scanner.listObjects("bucket");
		
		assertThat(listObjects).isEmpty();
	}
	
	@Test
	public void shouldListS3Summaries() {
		ObjectListing mockObjectListing = mock(ObjectListing.class);
		S3ObjectSummary mockObjectA = mock(S3ObjectSummary.class);
		ListObjectsRequest mockListObjectRequest = mock(ListObjectsRequest.class);
		when(scanner.instanciateRequest("bucket", null)).thenReturn(mockListObjectRequest);

		when(mockAmazonS3.listObjects(mockListObjectRequest)).thenReturn(mockObjectListing);
		when(mockObjectListing.getObjectSummaries()).thenReturn(asList(mockObjectA));
		
		List<S3ObjectSummary> listObjects = scanner.listObjects("bucket");
		
		assertThat(listObjects).containsExactly(mockObjectA);
	}
	
	@Test
	public void shouldListS3SummariesSpanningMultiplePages() {
		ObjectListing mockObjectListing = mock(ObjectListing.class);
		ObjectListing mockObjectListingPage2 = mock(ObjectListing.class);
		
		S3ObjectSummary mockObjectA = mock(S3ObjectSummary.class);
		S3ObjectSummary mockObjectB = mock(S3ObjectSummary.class);
		
		ListObjectsRequest mockListObjectRequest = mock(ListObjectsRequest.class);
		ListObjectsRequest mockListObjectRequestPage2 = mock(ListObjectsRequest.class);
		
		when(scanner.instanciateRequest("bucket", null)).thenReturn(mockListObjectRequest);
		when(scanner.instanciateRequest("bucket", "pager")).thenReturn(mockListObjectRequestPage2);

		when(mockAmazonS3.listObjects(mockListObjectRequest)).thenReturn(mockObjectListing);
		when(mockAmazonS3.listObjects(mockListObjectRequestPage2)).thenReturn(mockObjectListingPage2);
		
		when(mockObjectListing.getObjectSummaries()).thenReturn(asList(mockObjectA));
		when(mockObjectListing.getNextMarker()).thenReturn("pager");
		when(mockObjectListingPage2.getObjectSummaries()).thenReturn(asList(mockObjectB));
		
		List<S3ObjectSummary> listObjects = scanner.listObjects("bucket");
		
		assertThat(listObjects).containsExactly(mockObjectA, mockObjectB);
	}
}
