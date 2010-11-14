package ginger;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a simplified way to use regular expressions.
 * <p>
 * Examples:
 * </p>
 * 
 * <pre>
 * // Get the last word, returns "string"
 * new Regex("your string").find("\\w+$")
 * 
 * // Or we can use a capture group to find the
 * // word after "your"
 * new Regex("your string").find("your (\\w+)")
 * 
 * // Find all capture groups is also easy. Here we
 * // get a list with "your" and "string"
 * new Regex("your string").find("(\\w+) (\\w+)")

 * </pre>
 * <p>
 * Or, if you like onliners, like me, you can use the convenience static constructor "r".
 * Just use an static import and:
 * </p>
 * <pre>
 * if (r("your string").find("\\d+") == "0") return "We have numbers";
 * </pre>
 * <p>
 * Why this is better than the Java way?
 * </p>
 * <ul>
 * <li>There is only one object to deal with;</li>
 * <li>The results are just Strings and Lists;</li>
 * <li>We can do oneliners!!!</li>
 * </ul>
 * @author Ronie Uliana
 * @since 2010-11
 */
public class Regex {

	private final CharSequence targetString;

	/**
	 * Convenience constructor to make inliners easier. Use static import with
	 * it.
	 * <pre>
	 * import static ginger.Regex.r;
	 * ...
	 * String firstWord = r("some string").find("^\\w+");
	 * </pre>
	 * @see #Regex(CharSequence)
	 */
	public static Regex r(CharSequence targetString) {
		
		// Guard clause
		if (targetString == null) return new Regex("");

		return new Regex(targetString);
	}
	
	/**
	 * The parameter "targetString" is the {@link String} (or
	 * {@link StringBuilder}, as it accepts {@link CharSequence}) where all
	 * regular expressions will execute on.
	 * 
	 * @see #r
	 */
	public Regex(CharSequence targetString) {
		this.targetString = targetString;
	}

	/**
	 * Returns true if the regular expression matches any part of the target string.
	 * <p>It's case insensitive.</p>
	 */
	public boolean contains(String regex) {
		return find(regex, Pattern.CASE_INSENSITIVE) != null;
	}
	
	/**
	 * Alias for {@link #find(String, int)} 
	 */
	public String find(String regex) {
		return find(regex, 0);
	}
	
	/**
	 * Returns the first capture group in the regular expression,<br />
	 * Returns the match if there is no capture group,<br />
	 * Returns null if there is no match.
	 * <pre>
	 * r("target string").find("^\\w+");     // Returns "target"
	 * r("target string").find("tar(\\w+)"); // Returns "get" (the capture group)
	 * r("target string").find("not there"); // Returns null (no match)
	 * </pre>
	 * @param flags Exactly the same flags you pass in {@link Pattern#compile(String, int)}.
	 * @see #r
	 * @see #find(String)
	 * @see #findAll(String, int)
	 */
	public String find(String regex, int flags) {
		LinkedList<String> matches = findAll(regex, flags);
		if (matches.isEmpty()) {
			return null;
		} else {
			return matches.getFirst();
		}
	}

	/**
	 * Alias for {@link #findAll(String, int)} 
	 */
	public LinkedList<String> findAll(String regex) {
		return findAll(regex, 0);
	}
	
	/**
	 * Returns a list with all the capture groups in the regular expression,<br />
	 * Returns a list with the match if there is no capture group,<br />
	 * Returns an empty list if there is no match.<br />
	 * <p>
	 * It <em>never</em> returns null.
	 * </p>
	 * <pre>
	 * r("target string").findAll("(\\w+) (\\w+)"); // Returns ["target", "string"]
	 * r("target string").findAll("^\\w+");         // Returns ["target"]
	 * r("target string").findAll("not there");     // Returns [] (empty)
	 * </pre>
	 * @param flags Exactly the same flags you pass in {@link Pattern#compile(String, int)}.
	 * @see #r
	 * @see #find(String)
	 * @see #findAll(String)
	 */
	public LinkedList<String> findAll(String regex, int flags) {
		Matcher matcher = matcherFor(regex, flags);
		
		if (!matcher.find()) return newList();
		if (matcher.groupCount() == 0) {
			return newList(matcher.group());
		} else {
			LinkedList<String> result = newList();
			for (int i = 1; i <= matcher.groupCount(); i++) {
				result.add(matcher.group(i));
			}
			return result;
		}
	}
	
	private Matcher matcherFor(String regex, int flags) {
		return Pattern.compile(regex, flags).matcher(targetString);
	}
	
	private static <T> LinkedList<T> newList(T...elements) {
		LinkedList<T> result = new LinkedList<T>();
		for (T element : elements) {
			result.add(element);
		}
		return result;
	}
}
