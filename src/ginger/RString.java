package ginger;


import java.io.Serializable;
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

    private static abstract class Action {
        protected abstract void execute(Integer start, Integer end);
    }
    
    public class Position {
        
        private final Action action;

        public Position(Action action) {
            this.action = action;
        }
        
        public RString after(String regex) {
            new IfFind(regex) {
                public void execute(int matchStart, int matchEnd) {
                    action.execute(null, matchEnd);
                }
            };
            return RString.this;
        }

        public RString before(String regex) {
            new IfFind(regex) {
                public void execute(int matchStart, int matchEnd) {
                    action.execute(matchStart, null);
                }
            };
            return RString.this;
        }

        public RString around(String regex) {
            new IfFind(regex) {
                public void execute(int matchStart, int matchEnd) {
                    action.execute(null, matchEnd);
                    action.execute(matchStart, null);
                }
            };
            return RString.this;
        }

        public RString inside(String regex) {
            new IfFind(regex) {
                public void execute(int matchStart, int matchEnd) {
                    action.execute(matchStart, matchEnd);
                }
            };
            return RString.this;
        }
    }
    
    private abstract class IfFind {
        public IfFind(String regex) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(mutableString);
            if (matcher.find()) {
                if (matcher.groupCount() == 0)
                    execute(matcher.start(), matcher.end());
                else
                    execute(matcher.start(1), matcher.end(matcher.groupCount()));
            }
        }

        public abstract void execute(int matchStart, int matchEnd);
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
            public void execute(Integer start, Integer end) {
                if (start != null && end != null)
                    mutableString.delete(start, end);
                else if (start == null && end != null)
                    mutableString.delete(end, mutableString.length());
                else if (start != null && end == null)
                    mutableString.delete(0, start);
            }
        });
    }

    public Position insert(final String string) {
        return new Position(new Action() {
            public void execute(Integer start, Integer end) {
                if (string == null) return;
                if (start != null && end != null) {
                    mutableString.insert(end, string);
                    mutableString.insert(start, string);
                } else if (start == null && end != null)
                    mutableString.insert(end, string);
                else if (start != null && end == null)
                    mutableString.insert(start, string);
            }
        });
    }
    
    public Position insert(final String before, final String after) {
        return new Position(new Action() {
            public void execute(Integer start, Integer end) {
                if (before == null && after == null) return;
                String beforeString = before == null ? "" : before;
                String afterString = after == null ? "" : after;
                if (start != null && end != null) {
                    mutableString.insert(end, afterString);
                    mutableString.insert(start, beforeString);
                } else if (start == null && end != null)
                    mutableString.insert(end, afterString);
                else if (start != null && end == null)
                    mutableString.insert(start, beforeString);
            }
        });
    }

    public Position replace(final String string) {
        return new Position(new Action() {
            public void execute(Integer start, Integer end) {
                if (string == null) return;
                if (start != null && end != null)
                    mutableString.replace(start, end, string);
                else if (start == null && end != null)
                    mutableString.replace(end, mutableString.length(), string);
                else if (start != null && end == null)
                    mutableString.replace(0, start, string);
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

    //=======================
    // CharSequence Interface
    //=======================
    
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
