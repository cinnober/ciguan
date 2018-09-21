package com.cinnober.ciguan.data;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.BeforeClass;	
import org.junit.Test;

import com.cinnober.ciguan.CwfDataIf;
import com.cinnober.ciguan.client.MvcModelAttributesIf;

public class AsCwfDataJsonTest implements MvcModelAttributesIf {

	@BeforeClass
	public static void before() {
		CwfDataFactory.set(new AsCwfDataFactoryImpl());
	}
	
	
	@Test
	public void testProperties() {
		CwfDataIf cwfData = CwfDataFactory.create();
		cwfData.setProperty("string", "Foo");
		assertEquals(cwfData.getProperty("string"), "Foo");
		
		cwfData.setProperty("path.string", "Bar");
		assertEquals(cwfData.getProperty("path.string"), "Bar");
		
		cwfData.setProperty("boolean", true);
		assertEquals(cwfData.getBooleanProperty("boolean"), true);

		cwfData.setProperty("path.boolean", true);
		assertEquals(cwfData.getBooleanProperty("path.boolean"), true);
		
		Long value = new Long(System.currentTimeMillis());
		cwfData.setProperty("long", value);
		assertEquals(cwfData.getLongProperty("long"), value);

		cwfData.setProperty("path.long", value);
		assertEquals(cwfData.getLongProperty("path.long"), value);
		
		System.out.println(cwfData.toString());
	}
	
	@Test
	public void testTwoDimensionalStringArray() {
        String[][] tItems = new String[2][];
        tItems[0] = new String[] { "ABC", "DEF" };
        tItems[1] = new String[] { "GHI", "JKL" };
        
		CwfDataIf cwfData = CwfDataFactory.create();
		cwfData.setProperty("path.two-dimensional-string-array", tItems);
        
        String[][] doubleArray = cwfData.getStringDoubleArray("path.two-dimensional-string-array");
        assertEquals(tItems.length, doubleArray.length);
        
        for (int i = 0; i < tItems.length; i++) {
        	String[] a1 = tItems[i];
        	String[] a2 = doubleArray[i];
        	
            assertEquals(a1.length, a2.length);
        	for (int j = 0; j < a1.length; j++) {
                assertEquals(a1[j], a2[j]);
        	}
        }
		
        System.out.println(cwfData.toString());
	}
	
	@Test
	public void testNamedStringArrayMapValue() {
		String[] array = { "FOO", "BAR", "BAZ" };
		Map<String, String[]> map = new HashMap<>();
		map.put("key", array);
		
		CwfDataIf cwfData = CwfDataFactory.create();
		cwfData.setProperty("path.named-string-array-map", map);
		
		Map<String, String[]> retMap = cwfData.getStringArrayMap("path.named-string-array-map");
		
		assertEquals(map.size(), retMap.size());
		
		for (Entry<String, String[]> entry : map.entrySet()) {
			String[] strings = retMap.get(entry.getKey());
			assertNotNull(strings);
			assertTrue(Arrays.equals(entry.getValue(), strings));
		}
				
        System.out.println(cwfData.toString());
	}
	
	@Test
	public void testArrayOfCwfDataIf() {
        CwfDataIf array = CwfDataFactory.create();

        CwfDataIf obj1 = CwfDataFactory.create();
        obj1.setProperty("obj1", "Foo");
        array.addObject(ATTR_ARRAY_ITEMS, obj1);

        CwfDataIf obj2 = CwfDataFactory.create();
        obj2.setProperty("obj2", "Bar");
        array.addObject(ATTR_ARRAY_ITEMS, obj2);

        List<CwfDataIf> objectList = array.getObjectList(ATTR_ARRAY_ITEMS);
        assertSame(objectList.get(0), obj1);
        assertSame(objectList.get(1), obj2);
        
        System.out.println(array.toString());
	}

	
}
