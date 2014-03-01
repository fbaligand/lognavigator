package fr.icdc.dei.banque.lognavigator.test;

import java.io.File;

import org.junit.Test;

public class FileTest {

	@Test
	public void testFile() throws Exception {
		
		System.out.println(new File("./src/main/resources").getPath());
		System.out.println(new File("./src/main/resources/log4j.properties").getPath());
		
		String dir = new File("./src/main/resources").getPath();
		System.out.println(new File("./src/main/resources/log4j.properties").getPath().substring(dir.length() + 1));
		
//		File file = new File(".");
		File file = new File((String)null, ".");
		
		System.out.println(file.getAbsoluteFile());
		System.out.println(file.getCanonicalPath());
		System.out.println(file.getName());
		System.out.println(file.getPath());
		
	}
}
