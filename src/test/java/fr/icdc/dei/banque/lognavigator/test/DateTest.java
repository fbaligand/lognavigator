package fr.icdc.dei.banque.lognavigator.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

public class DateTest {

	@Test
	public void testSshDateParse() throws ParseException {
		// .gitconfig - ./.gitconfig - . - false - true - REGULAR - 1362264950 - Fri Jan 16 19:24:24 CET 1970
		// 1376951056883 - now date
		
		System.out.println(new Date().getTime());
		System.out.println(new Date(1362264950000L));
		
	}
		
		@Test
		public void testDateFormat() throws ParseException {
			
//		Date date = new Date(113, 1, 3);
		Date date = new Date();
		
		System.out.println(new SimpleDateFormat("M").format(date));
		System.out.println(new SimpleDateFormat("MM").format(date));
		System.out.println(new SimpleDateFormat("MMM", Locale.US).format(date));
		System.out.println(new SimpleDateFormat("MMMM", Locale.US).format(date));

		System.out.println(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
		System.out.println(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.FRANCE).format(new Date()));
//		Date parsedDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.FRANCE).parse("24 avril 12:09");
		Date parsedDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.FRANCE).parse("13 mars  2012");
//		Date parsedDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.FRANCE).parse("24 avril 12:09");
		System.out.println(new SimpleDateFormat("dd/MM/yyyy").format(parsedDate));
		System.out.println(Locale.getDefault());
	}
}
