package ginger;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides a utility class to manipulate String conveniently.
 * <p>
 * Examples:
 * </p>
 * 
 * <pre>
 * // Results in &quot;testing the&quot; 
 * new RString(&quot;testing the test&quot;).delete().after(&quot;the&quot;);
 * 
 * // The position to action can be based on the capture group
 * // this on also results in &quot;testing the&quot; 
 * new RString(&quot;testing the test&quot;).delete().after(&quot;ing (the) te&quot;);
 * 
 * // We can replace the matching group
 * new RString(&quot;testing the test&quot;).replace(&quot;wabba&quot;).inside(&quot;ing (the) te&quot;);
 * 
 * // Or around it (this results in &quot;wabba the wabba&quot;
 * new RString(&quot;testing the test&quot;).replace(&quot;wabba&quot;).around(&quot;ing( the )te&quot;);
 * 
 * // Also, it's possible to simply extract a group
 * new RString(&quot;testing the test&quot;).extract(&quot;testing (.*) test&quot;);
 * 
 * // More conveniently, we can shorten &quot;new RString(xxx)&quot; to &quot;r(xxx)&quot;
 * r(&quot;testing the test&quot;).extract(&quot;testing (.*) test&quot;);
 * </pre>
 * 
 * @author Ronie Uliana
 * @since 2010-03
 */
public class RString implements CharSequence, Serializable {

    private static final long serialVersionUID = -4404387882574357527L;

    public static abstract class Action {
        protected void executeFromTo(int from, int to) {
            
            execute(from);
            
            int start;
            int end;
            if (from < to) {
                start = from;
                end = to;
            } else {
                start = to;
                end = from;
            }
            
            execute(start, end);
        }
        
        protected void execute(int start, int end) {
            // Can be overridden
        }
        
        protected void execute(int point) {
            // Can be overridden
        }
    }

    public class Position {

        private final Action action;

        public Position(Action action) {
            this.action = action;
        }

        public RString after(String regex) {
            new ForEachFind(regex) {
                public void execute(int previousEnd, int matchStart, int matchEnd, int nextStart) {
                    action.executeFromTo(matchEnd, nextStart);
                }
            };
            return RString.this;
        }

        public RString before(String regex) {
            new ForEachFind(regex) {
                public void execute(int previousEnd, int matchStart, int matchEnd, int nextStart) {
                    action.executeFromTo(matchStart, previousEnd);
                }
            };
            return RString.this;
        }

        public RString around(String regex) {
            new ForEachFind(regex) {
                public void execute(int previousEnd, int matchStart,
                                    int matchEnd, int nextStart) {
                    action.executeFromTo(matchEnd, nextStart);
                    action.executeFromTo(matchStart, previousEnd);
                }
            };
            return RString.this;
        }

        public RString inside(String regex) {
            new ForEachFind(regex) {
                public void execute(int previousEnd, int matchStart, int matchEnd, int nextStart) {
                    action.executeFromTo(matchStart, matchEnd);
                }
            };
            return RString.this;
        }
    }

    private abstract class ForEachFind {
        public ForEachFind(String regex) {
            String copyOfString = mutableString.toString();

            Deque<int[]> matches = new LinkedList<int[]>();

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(copyOfString);
            while (matcher.find()) {
                if (matcher.groupCount() == 0)
                    matches.add(new int[] { matcher.start(), matcher.end() });
                else
                    for (int i = 1; i <= matcher.groupCount(); i++)
                        matches.add(new int[] { matcher.start(i), matcher.end(i) });
            }

            if (matches.isEmpty()) return;

            if (matches.size() == 1) {
                execute(0, matches.getFirst()[0], matches.getFirst()[1], copyOfString.length());
                return;
            }
            
            // Changes to string must be made from end to start
            // otherwise the numbers will not match after we modify
            // the mutableString
            Iterator<int[]> iterator = matches.descendingIterator();
            int[] nextMatch = new int[] { copyOfString.length(), copyOfString.length() };
            int[] currentMatch = iterator.next();
            while (iterator.hasNext()) {
                int[] previousMatch = iterator.next();
                execute(previousMatch[1], currentMatch[0], currentMatch[1], nextMatch[0]);
                nextMatch = currentMatch;
                currentMatch = previousMatch;
            }
            execute(0, currentMatch[0], currentMatch[1], nextMatch[0]);
        }

        public abstract void execute(int previousEnd, int start, int end, int nextStart);
    }

    private final StringBuilder mutableString;

    public static RString r(String string) {
        return new RString(string);
    }

    public RString(String string) {
        this.mutableString = string == null
                ? new StringBuilder()
                : new StringBuilder(string);
    }

    public Position delete() {
        return new Position(new Action() {
            
            // These two prevents to delete what was already deleted when using delete.around
            Integer previousStart;
            Integer previousEnd;

            public void execute(int start, int end) {

                if ((previousStart == null || previousEnd == null)
                        || (previousStart != start && previousEnd != end)) {
                    
                    mutableString.delete(start, end);
                }
                previousStart = start;
                previousEnd = end;
            }
        });
    }

    public Position insert(final String string) {
        return new Position(new Action() {
            public void execute(int point) {
                if (string == null) return;
                mutableString.insert(point, string);
            }
        });
    }

    public Position insert(final String before, final String after) {
        return new Position(new Action() {
            
            // These two prevents double match when using insert.around
            Integer previousStart;
            Integer previousEnd;
            
            public void execute(int start, int end) {
                if (start == end) return;
                
                if (after != null && (previousEnd == null || previousEnd != end))
                    mutableString.insert(end, after);
                
                if (before != null && (previousStart == null || previousStart != start)) 
                    mutableString.insert(start, before);
                
                previousStart = start;
                previousEnd = end;
            }
        });
    }

    public Position replace(final String string) {
        return new Position(new Action() {
            // These two prevents double match when using replace.around
            Integer previousStart;
            Integer previousEnd;
            
            public void execute(int start, int end) {
                if (string == null) return;
                
                if ((previousStart == null || previousEnd == null)
                        || (previousStart != start && previousEnd != end)) {
                
                    mutableString.replace(start, end, string);
                }
                previousStart = start;
                previousEnd = end;
            }
        });
    }

    public RString extract(String regex) {
        delete().around(regex);
        return this;
    }

    @Override
    public String toString() {
        return mutableString.toString();
    }

    // =======================
    // CharSequence Interface
    // =======================

    @Override
    public char charAt(int index) {
        return mutableString.charAt(index);
    }

    @Override
    public int length() {
        return mutableString.length();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return mutableString.substring(start, end);
    }
}
