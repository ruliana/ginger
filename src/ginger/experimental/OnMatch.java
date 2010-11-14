package ginger.experimental;

/**
 * This class provides and interface to manipulate matches on Xtring (method
 * {@link Xtring#run(OnMatch)}). It provides several helper methods to match
 * and manipulate the Xtring.
 * 
 * <p>
 * Example: <br />
 * String: "testing the xtring"<br />
 * Regular expression: " the " (with whitespaces around)
 * </p>
 * 
 * <pre>
 * t <= previousStart() = 0
 * e   
 * s
 * t    previous() = "testing"
 * i
 * n
 * g
 *   <= matchStart(), previousEnd() = 7                  
 * t 
 * h    match() = " the "
 * e 
 *   <= matchEnd(), nextStart() = 12
 * x
 * t 
 * r    next() = "xtring"
 * i
 * n
 * g <= nextEnd() = 20
 * 
 * </pre>
 * 
 * @author Ronie Uliana
 * @since 2010-04
 */
public abstract class OnMatch {
    
    private int previousStart;
    private int matchStart;
    private int matchEnd;
    private int nextEnd;
    private StringBuilder string;
    private boolean isFirst;
    private boolean isLast;

    protected int previousStart() {
        return previousStart;
    }

    protected int previousEnd() {
        return matchStart();
    }

    protected Xtring previous() {
        return substring(previousStart(), previousEnd());
    }
    
    protected int matchStart() {
        return matchStart;
    }

    protected int matchEnd() {
        return matchEnd;
    }

    protected Xtring match() {
        return substring(matchStart(), matchEnd());
    }
    
    protected int nextStart() {
        return matchEnd();
    }

    protected int nextEnd() {
        return nextEnd;
    }

    protected Xtring next() {
        return substring(nextStart(), nextEnd());
    }
    
    protected boolean isFirst() {
        return isFirst;
    }
    
    protected boolean isLast() {
        return isLast;
    }
    
    protected boolean isUniqueMatch() {
        return isFirst() && isLast();
    }
    
    private Xtring substring(int from, int to) {
        return new Xtring(string.substring(from, to));
    }
    
    public void execute(StringBuilder result) {
        execute();
    }
    
    public void execute() {
        throw new RuntimeException("Must be overriden");
    }

    public void runWith(StringBuilder mutableString, int previousStart, int matchStart, int matchEnd, int nextEnd, boolean isFirst, boolean isLast) {
        this.string = mutableString;
        this.previousStart = previousStart;
        this.matchStart = matchStart;
        this.matchEnd = matchEnd;
        this.nextEnd = nextEnd;
        this.isFirst = isFirst;
        this.isLast = isLast;
        execute(mutableString);
    }
}
