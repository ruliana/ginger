package ginger;

import ginger.rstring.Command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Ronie Uliana
 * @since 2010-03
 */
public class RString {

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
    private Command command = null;

    public RString(String string) {
        this.mutableString = string == null 
            ? new StringBuilder() 
            : new StringBuilder(string);
    }

    public RString delete() {
        command = new Command() {
            public void execute(Integer start, Integer end) {
                if (start != null && end != null)
                    mutableString.delete(start, end);
                else if (start == null && end != null)
                    mutableString.delete(end, mutableString.length());
                else if (start != null && end == null)
                    mutableString.delete(0, start);
            }
        };
        return this;
    }

    public RString insert(final String string) {
        command = new Command() {
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
        };
        return this;
    }
    
    public RString insert(final String before, final String after) {
        command = new Command() {
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
        };
        return this;
    }

    public RString replace(final String string) {
        command = new Command() {
            public void execute(Integer start, Integer end) {
                if (string == null) return;
                if (start != null && end != null)
                    mutableString.replace(start, end, string);
                else if (start == null && end != null)
                    mutableString.replace(end, mutableString.length(), string);
                else if (start != null && end == null)
                    mutableString.replace(0, start, string);
            }
        };
        return this;
    }

    public RString after(String regex) {
        if (command == null) return this;

        new IfFind(regex) {
            public void execute(int matchStart, int matchEnd) {
                command.execute(null, matchEnd);
            }
        };
        return this;
    }

    public RString before(String regex) {
        if (command == null) return this;

        new IfFind(regex) {
            public void execute(int matchStart, int matchEnd) {
                command.execute(matchStart, null);
            }
        };
        return this;
    }

    public RString around(String regex) {
        if (command == null) return this;

        new IfFind(regex) {
            public void execute(int matchStart, int matchEnd) {
                command.execute(null, matchEnd);
                command.execute(matchStart, null);
            }
        };
        return this;
    }

    public RString inside(String regex) {
        if (command == null) return this;

        new IfFind(regex) {
            public void execute(int matchStart, int matchEnd) {
                command.execute(matchStart, matchEnd);
            }
        };
        return this;
    }
    
    public RString extract(String regex) {
        delete().around(regex);
        return this;
    }

    @Override
    public String toString() {
        return mutableString.toString();
    }
}
