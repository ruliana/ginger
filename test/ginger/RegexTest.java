package ginger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ginger.Regex.r;

import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;


public class RegexTest {
	
	@Test
	public void convenienceConstructor() throws Exception {
		assertEquals(Regex.class, r("anything").getClass());
	}
	
	@Test
	public void simpleFind() throws Exception {
		assertEquals("target", r("target string").find("^\\w+"));
		assertEquals("get", r("target string").find("tar(\\w+)"));
		assertNull(r("target string").find("not there"));
	}
	
	@Test
	public void findwithRegexFlags() throws Exception {
		assertNull(r("target string").find("TAR(\\w+)"));
		assertEquals("get", r("target string").find("TAR(\\w+)", Pattern.CASE_INSENSITIVE));
	}
	
	@Test
	public void charSequenceConstructor() throws Exception {
		assertEquals("char", r(new StringBuilder("testing char sequence")).find("\\s(.*)\\s"));
	}
	
	@Test
	public void findAll() throws Exception {
		Regex regex = new Regex("a text to find everything");
		LinkedList<String> result = null;
		
		result = regex.findAll("not there");
		assertEquals(0, result.size());
		
		result = regex.findAll("f\\w+d");
		assertEquals(1, result.size());
		assertEquals("find", result.getFirst());
		
		result = regex.findAll("f(\\w+)d");
		assertEquals(1, result.size());
		assertEquals("in", result.getFirst());
		
		result = regex.findAll("f(\\w+)d.*(.{3})$");
		assertEquals(2, result.size());
		assertEquals("in", result.get(0));
		assertEquals("ing", result.get(1));
		
		result = regex.findAll("\\w+");
		assertEquals(5, result.size());
		assertEquals("a", result.get(0));
		assertEquals("text", result.get(1));
		assertEquals("everything", result.get(4));
		
		result = r("<em>mark</em> <strong>them</strong>").findAll(">(\\w+)<");
		assertEquals(2, result.size());
		assertEquals("mark", result.get(0));
		assertEquals("them", result.get(1));
	}

	@Test
	public void findAllNamed() throws Exception {
		Regex regex = new Regex("a text to find everything, a find to another thing.");
		
		LinkedList<Map<String, String>> result = regex.findAll("(find).*?(thing)").named("first", "second");
		assertEquals("find", result.get(0).get("first"));
		assertEquals("thing", result.get(0).get("second"));
		assertEquals("find", result.get(1).get("first"));
		assertEquals("thing", result.get(1).get("second"));
	}
}
