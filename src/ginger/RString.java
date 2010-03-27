package ginger;

import ginger.rstring.Command;

import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Ronie Uliana
 * @since 2010-03
 */
public class RString {

    private final StringBuilder mutableString;
    private Command command = null;

    public RString(String string) {
        this.mutableString = string == null
                ? new StringBuilder()
                : new StringBuilder(string);
    }

    public RString delete() {
        command = new Command() {
            public void execute(int start, int end) {
                mutableString.delete(start, end);
            }
        };
        return this;
    }

    public RString after(String regex) {
        if (command == null) return this;
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mutableString);
        if (matcher.find()) {
            command.execute(matcher.end(), mutableString.length());
        }
        return this;
    }

    public RString before(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    public RString around(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    public RString inside(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        return mutableString.toString();
    }
}
