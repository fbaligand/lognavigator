package fr.icdc.dei.banque.lognavigator.test;

import org.junit.Test;

public class EnumTest {
	
	public enum MyEnum {
		TATA, TITI;
		
		public static MyEnum fromValue(String value) {
			return TITI;
		}
		public String toValue() {
			return "TITI";
		}
	}
	
	@Test
	public void testEnum() {
		System.out.println(MyEnum.valueOf("TATA"));
	}

}
