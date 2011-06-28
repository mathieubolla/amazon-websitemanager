package com.mathieubolla.io;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class DirectoryScannerTest {
	DirectoryScanner scanner;
	File mockRegularFileA;
	File mockRegularFileB;
	File mockDirectoryA;
	File mockDirectoryB;
	
	@Before
	public void setup() {
		scanner = new DirectoryScanner();
		mockRegularFileA = mock(File.class);
		mockRegularFileB = mock(File.class);
		mockDirectoryA = mock(File.class);
		mockDirectoryB = mock(File.class);
		when(mockRegularFileA.isFile()).thenReturn(true);
		when(mockRegularFileB.isFile()).thenReturn(true);
		when(mockDirectoryA.isDirectory()).thenReturn(true);
		when(mockDirectoryB.isDirectory()).thenReturn(true);
		when(mockRegularFileA.getName()).thenReturn("A");
		when(mockRegularFileB.getName()).thenReturn("B");
		when(mockDirectoryA.getName()).thenReturn("Adir");
		when(mockDirectoryB.getName()).thenReturn("Bdir");
	}
	
	@Test
	public void shouldListFilesFromDirectory() {
		File mockSourceDirectory = mock(File.class);

		when(mockSourceDirectory.listFiles()).thenReturn(new File[] {mockRegularFileA, mockRegularFileB});
		
		assertThat(scanner.scanRegularFiles(mockSourceDirectory)).containsExactly(mockRegularFileA, mockRegularFileB);
	}
	
	@Test
	public void shouldRecursivelyListFiles() {
		File mockSourceDirectory = mock(File.class);

		when(mockSourceDirectory.listFiles()).thenReturn(new File[] {mockDirectoryA, mockDirectoryB});
		when(mockDirectoryA.listFiles()).thenReturn(new File[] {mockRegularFileA});
		when(mockDirectoryB.listFiles()).thenReturn(new File[] {mockRegularFileB});
		
		assertThat(scanner.scanRegularFiles(mockSourceDirectory)).containsExactly(mockRegularFileA, mockRegularFileB);
	}
	
	@Test
	public void shouldIgnoreDotFiles() {
		File mockSourceDirectory = mock(File.class);

		when(mockSourceDirectory.listFiles()).thenReturn(new File[] {mockDirectoryA, mockDirectoryB});
		when(mockDirectoryA.listFiles()).thenReturn(new File[] {mockRegularFileA});
		when(mockDirectoryB.listFiles()).thenReturn(new File[] {mockRegularFileB});
		
		when(mockRegularFileB.getName()).thenReturn(".dotName");
		
		assertThat(scanner.scanRegularFiles(mockSourceDirectory)).containsExactly(mockRegularFileA);
	}
	
	@Test
	public void shouldIgnoreDotDirectories() {
		File mockSourceDirectory = mock(File.class);

		when(mockSourceDirectory.listFiles()).thenReturn(new File[] {mockDirectoryA, mockDirectoryB});
		when(mockDirectoryA.listFiles()).thenReturn(new File[] {mockRegularFileA});
		when(mockDirectoryB.listFiles()).thenReturn(new File[] {mockRegularFileB});
		
		when(mockDirectoryB.getName()).thenReturn(".dotName");
		
		assertThat(scanner.scanRegularFiles(mockSourceDirectory)).containsExactly(mockRegularFileA);
	}
}
