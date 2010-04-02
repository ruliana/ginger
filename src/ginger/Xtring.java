package ginger;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Xtring implements CharSequence {

    private final String string;
    private final String regex;

    public Xtring(CharSequence string) {
        this(string, null);
    }

    public Xtring(CharSequence string, String regex) {
        this.string = string == null ? "" : string.toString();
        this.regex = regex;
    }

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
    
    public Xtring run(OnMatch onMatch) {
        
        // Guard clauses
        if (string == null) return this;
        if (regex == null) return this;
        
        Deque<int[]> matches = allMatches();

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
        if (iterator.hasNext()) {
            int[] previousMatch = iterator.next();
            onMatch.runWith(result, previousMatch[1], currentMatch[0], currentMatch[1], nextMatch[0], false, true);
            nextMatch = currentMatch;
            currentMatch = previousMatch;
        }
        while (iterator.hasNext()) {
            int[] previousMatch = iterator.next();
            onMatch.runWith(result, previousMatch[1], currentMatch[0], currentMatch[1], nextMatch[0], false, false);
            nextMatch = currentMatch;
            currentMatch = previousMatch;
        }
        onMatch.runWith(result, 0, currentMatch[0], currentMatch[1], nextMatch[0], true, false);
        
        return new Xtring(result);
    }

    private Deque<int[]> allMatches() {
        assert string != null;
        assert regex != null ;
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);
        
        Deque<int[]> matches = new LinkedList<int[]>();
        while (matcher.find()) {
            if (matcher.groupCount() == 0)
                matches.add(new int[] { matcher.start(), matcher.end() });
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
}
