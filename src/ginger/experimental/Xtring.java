package ginger.experimental;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Xtring implements CharSequence {
	
    private final String string;
    private final String regex;

    //===========================
    // High level interface
    //===========================

    public Xtring(CharSequence string) {
        this(string, null);
    }

    public Xtring(CharSequence string, String regex) {
        this.string = string == null ? "" : string.toString();
        this.regex = regex;
    }
    
	public Xtring find(String regex) {
		LinkedList<Xtring> result = findAll(regex);
		return result.isEmpty() ? null : result.getFirst();
	}
	
	public LinkedList<Xtring> findAll(String regex) {
		
		final NegativeRegularExpression negative = new NegativeRegularExpression(regex);
		
		// I'm not really proud of this...
		// TODO Find a better way to interrupt "OnMatch"
		final boolean[] interruptFlag = new boolean[]{false};
		
		final LinkedList<Xtring> result = new LinkedList<Xtring>();
		on(negative.getPositiveRegex()).run(new OnMatch() {
			public void execute() {
				if (interruptFlag[0]) return;
				if (negative.shouldCheckForNegativeExpressions()) {
					Xtring match = match();
					if (!negative.matchIsAllowed(match)) {
						result.clear();
						interruptFlag[0] = true;
						return;
					}
				}
				
				// OnMatch is applied from back to front,
				// so we have to add the matches to result 
				// in reverse order
				result.addFirst(match());
			}
		});
		return result;
	}

    //===========================
    // Low level interface
    //===========================
    
    public Xtring on(String regex) {
        return new Xtring(string, regex);
    }

    public boolean isEmpty() {
        return string.isEmpty();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof CharSequence)) return false;
        return obj.toString().equals(string); 
    }
    
    public int hashCode() {
        return string.hashCode();
    }
    
    public String toString() {
        return string;
    }
    
	/**
	 * This method executes {@link OnMatch#execute()} or
	 * {@link OnMatch#execute(StringBuilder)} on each match of the regular
	 * expression.
	 * <p>
	 * <em>WARNING!</em> This is a "low level" method intended to be used when
	 * other methods does not provide the necessary functionality. It's a pretty
	 * straight forward method, but it has a "catch":
	 * </p>
	 * <p>
	 * <em>The matches are performed in reverse order, from end to begin.</em>
	 * </p>
	 * <p>
	 * The method work this way because transformations are far easier to
	 * perform. This way we can add or remove characters on transformation
	 * without change the matches already done.
	 * </p>
	 */
	public Xtring run(OnMatch onMatch) {
        
        // Guard clauses
        if (string == null) return this;
        if (regex == null) return this;
        
        LinkedList<int[]> matches = allMatches();

        if (matches.isEmpty()) return this;

        StringBuilder result = new StringBuilder(string);
        if (matches.size() == 1) {
            int[] firstMatch = matches.getFirst();
            onMatch.runWith(result, 0, firstMatch[0], firstMatch[1], string.length(), true, true);
            return new Xtring(result);
        }
        
        // Changes to string must be made from end to start
        // otherwise the numbers will not match after we modify
        // the mutableString
        Iterator<int[]> iterator = matches.descendingIterator();
        int[] nextMatch = new int[] { string.length(), string.length() };
        int[] currentMatch = iterator.next();
        
        // First match
        if (iterator.hasNext()) {
            int[] previousMatch = iterator.next();
            onMatch.runWith(result, previousMatch[1], currentMatch[0], currentMatch[1], nextMatch[0], false, true);
            nextMatch = currentMatch;
            currentMatch = previousMatch;
        }
        
        // "Middle" matches
        while (iterator.hasNext()) {
            int[] previousMatch = iterator.next();
            onMatch.runWith(result, previousMatch[1], currentMatch[0], currentMatch[1], nextMatch[0], false, false);
            nextMatch = currentMatch;
            currentMatch = previousMatch;
        }
        
        // Last match
        onMatch.runWith(result, 0, currentMatch[0], currentMatch[1], nextMatch[0], true, false);
        
        return new Xtring(result);
    }

    private LinkedList<int[]> allMatches() {
        assert string != null;
        assert regex != null ;
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        
        LinkedList<int[]> matches = new LinkedList<int[]>();
        while (matcher.find()) {
        	
        	// No capture group, return the whole match
            if (matcher.groupCount() == 0)
                matches.add(new int[] { matcher.start(), matcher.end() });
            
            // Any capture group? So they are what we want
            else
                for (int i = 1; i <= matcher.groupCount(); i++)
                    matches.add(new int[] { matcher.start(i), matcher.end(i) });
        }
        return matches;
    }

    //===========================
    // CharSequence Interface
    //===========================
    
    public char charAt(int index) {
        return string.charAt(index);
    }

    public int length() {
        return string.length();
    }

    public CharSequence subSequence(int start, int end) {
        return string.subSequence(start, end);
    }

    //===========================
    // Helper class
    //===========================

	static class NegativeRegularExpression {
		private final CharSequence regex;
		private LinkedList<String> expressions = new LinkedList<String>();
		private Iterator<String> currentExpression;
		private String modifiedRegex;

		public NegativeRegularExpression(CharSequence regex) {
			this.regex = regex;
			initialize();
		}

		public void initialize() {
			// TODO Allow at least 10 levels of nesting parenthesis here.
			Matcher matcher = matcher("\\(!!([^\\)]*)\\)", regex);
			while (matcher.find()) {
				expressions.add(matcher.group(1));
			}
			modifiedRegex = matcher.replaceAll("(.*)");
			// The negative expressions will be checked in reverse
			// order, that's way we need a reverse iterator.
			currentExpression = expressions.descendingIterator();
		}
		
		public String getPositiveRegex() {
			return modifiedRegex;
		}

		public boolean matchIsAllowed(CharSequence match) {
			if (expressions.isEmpty()) return true;
			if (currentExpression == null) return true;
			if (!currentExpression.hasNext()) return true;
			String negatedRegexp = currentExpression.next();
			return !match.toString().matches(negatedRegexp);
		}
		
		private Matcher matcher(CharSequence regex, CharSequence string) {
			Pattern negativeRegex = Pattern.compile(regex.toString());
			return negativeRegex.matcher(string);
		}

		public boolean shouldCheckForNegativeExpressions() {
			return !expressions.isEmpty();
		}
	}
}
