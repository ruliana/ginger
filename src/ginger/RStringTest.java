package ginger;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RStringTest {

    @Test
    public void delete() throws Exception {
        assertEquals("testing the", new RString("testing the test").delete().after("the").toString());
        assertEquals("testing", new RString("testing the test").delete().before("\\s+[^\\s]\\s+"));
        assertEquals("the test", new RString("testing the test").delete().before("ing (the) te"));
        assertEquals("testing the", new RString("testing the test").delete().after("ing (the) te"));
        assertEquals("the", new RString("testing the test").delete().around("ing (the) te"));
        assertEquals("testing  test", new RString("testing the test").delete().inside("ing (the) te"));
    }
}
