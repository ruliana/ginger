package ginger.experimental;

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
    
    public static Transformation TO_LOWER_CASE = new Transformation() {
		public String transform(String string) {
			return string.toLowerCase();
		}
	};
    
    public static Transformation TO_UPPER_CASE = new Transformation() {
		public String transform(String string) {
			return string.toUpperCase();
		}
	};
    
    public static Transformation CAPITALIZE = new Transformation() {
		public String transform(String string) {
			if (string.length() <= 1) return string.toUpperCase(); 
			return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
		}
	};
    
    public static Transformation DECAPITALIZE = new Transformation() {
		public String transform(String string) {
			if (string.length() <= 1) return string.toLowerCase(); 
			return string.substring(0, 1).toLowerCase() + string.substring(1).toLowerCase();
		}
	};

    private final StringBuilder mutableString;

    public static RString r(String string) {
        return new RString(string);
    }

    public RString(String string) {
        this.mutableString = string == null
                ? new StringBuilder()
                : new StringBuilder(string);
    }

    public Position toLowerCase() {
    	return replace(TO_LOWER_CASE);
    }

    public Position toUpperCase() {
    	return replace(TO_UPPER_CASE);
    }

    public Position capitalize() {
    	return replace(CAPITALIZE);
    }

    public Position decapitalize() {
    	return replace(DECAPITALIZE);
    }

    public Position changePlaces() {
        return new Position(new Action() {
        	private Integer lastStart = null;
        	private Integer lastEnd = null;
            public void execute(int start, int end) {
            	if (lastStart == null) {
            		lastStart = start;
            		lastEnd = end;
            	} else {
                    String here = mutableString.substring(start, end);
                    String there = mutableString.substring(lastStart, lastEnd);
                    mutableString.replace(lastStart, lastEnd, here);
                    mutableString.replace(start, end, there);
                    lastStart = null;
                    lastEnd = null;
            	}
            }
        });
    }
    
    public Position delete() {
        return new Position(new Action() {
            public void execute(int start, int end) {
                mutableString.delete(start, end);
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
            public void execute(int start, int end) {
                if (start == end) return;
                mutableString.insert(end, after);
                mutableString.insert(start, before);
            }}) {
        	public RString around(String regex) {
        		onMatches(regex);
        		return RString.this;
        	}
        };
    }
    
    public Position replace(final Transformation transformation) {
    	return new Position(new Action() {
    		public void execute(int start, int end) {
    			if (transformation == null) return;
    			mutableString.replace(start, end, transformation.transform(mutableString.substring(start, end)));
    		}
    	});
    }

    public Position replace(final String string) {
        return new Position(new Action() {
            public void execute(int start, int end) {
                if (string == null) return;
                mutableString.replace(start, end, string);
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

    public static abstract class Action {
        
        // These are used to prevent double actions on the same match
        Integer previousStart;
        Integer previousEnd;

        protected void executeFromTo(int from, int to) {
        	
            int start = from < to ? from : to;
            int end   = from > to ? from : to;
            
            execute(from);
        	if (notVisitedYet(start, end)) execute(start, end);
            
            previousStart = start;
            previousEnd = end;
        }
        
        protected boolean notVisitedYet(int start, int end) {
        	return (previousStart == null  || previousEnd == null) || (previousStart != start && previousEnd != end);
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
        
        public RString words() {
        	return onMatch("\\w+");
        }
        
        public RString firstWord() {
        	return onMatch("^\\w+");
        }
        
        public RString lastWord() {
        	return onMatch("\\w+$");
        }
        
        public RString firstAndLastWord() {
        	return onMatch("^\\w+|\\w+$");
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
        	return onNoMatches(regex);
        }

        public RString inside(String regex) {
        	return onMatches(regex);
        }
        
        /**
         * Alias for {@link #onMatches(String)}
         */
        public RString onMatch(String regex) {
        	return onMatches(regex);
        }
        
        public RString onMatches(String regex) {
            new ForEachFind(regex) {
                public void execute(int previousEnd, int matchStart, int matchEnd, int nextStart) {
                    action.executeFromTo(matchStart, matchEnd);
                }
            };
            return RString.this;
        }
        
        /**
         * Alias for {@link #onNoMatches(String)}
         */
        public RString onNoMatch(String regex) {
        	return onNoMatches(regex);
        }
        
        public RString onNoMatches(String regex) {
            new ForEachFind(regex) {
                public void execute(int previousEnd, int matchStart,
                                    int matchEnd, int nextStart) {
                    action.executeFromTo(matchEnd, nextStart);
                    action.executeFromTo(matchStart, previousEnd);
                }
            };
            return RString.this;
        }
    }
    
    public static interface Transformation {
    	public String transform(String string);
    }
}
