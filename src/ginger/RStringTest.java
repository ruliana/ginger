package ginger;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class RStringTest {

    @Test
    public void delete() throws Exception {
        assertEquals("testing the", new RString("testing the test").delete().after("the").toString());
        assertEquals(" the test", new RString("testing the test").delete().before("\\s+[^\\s]+\\s+").toString());
        assertEquals("the test", new RString("testing the test").delete().before("ing (the) te").toString());
        assertEquals("testing the", new RString("testing the test").delete().after("ing (the) te").toString());
        assertEquals("the", new RString("testing the test").delete().around("ing (the) te").toString());
        assertEquals("testing  test", new RString("testing the test").delete().inside("ing (the) te").toString());
    }

    @Test
    public void insert() throws Exception {
        assertEquals("testing thewabba test", new RString("testing the test").insert("wabba").after("the").toString());
        assertEquals("testingwabba the test", new RString("testing the test").insert("wabba").before("\\s+[^\\s]+\\s+").toString());
        assertEquals("testing wabbathe test", new RString("testing the test").insert("wabba").before("ing (the) te").toString());
        assertEquals("testing thewabba test", new RString("testing the test").insert("wabba").after("ing (the) te").toString());
        assertEquals("testing wabbathewabba test", new RString("testing the test").insert("wabba").around("ing (the) te").toString());
        assertEquals("testing wabbathewabba test", new RString("testing the test").insert("wabba").inside("ing (the) te").toString());
    }
    
    @Test
    public void insertTwoStrings() throws Exception {
        assertEquals("testing the) test", new RString("testing the test").insert("(", ")").after("the").toString());
        assertEquals("testing( the test", new RString("testing the test").insert("(", ")").before("\\s+[^\\s]+\\s+").toString());
        assertEquals("testing (the test", new RString("testing the test").insert("(", ")").before("ing (the) te").toString());
        assertEquals("testing the) test", new RString("testing the test").insert("(", ")").after("ing (the) te").toString());
        assertEquals("testing (the) test", new RString("testing the test").insert("(", ")").around("ing (the) te").toString());
        assertEquals("testing (the) test", new RString("testing the test").insert("(", ")").inside("ing (the) te").toString());
    }

    @Test
    public void replace() throws Exception {
        assertEquals("testing thewabba", new RString("testing the test").replace("wabba").after("the").toString());
        assertEquals("wabba the test", new RString("testing the test").replace("wabba").before("\\s+[^\\s]+\\s+").toString());
        assertEquals("wabba the test", new RString("testing the test").replace("wabba").before("ing( the) te").toString());
        assertEquals("testing the wabba", new RString("testing the test").replace("wabba").after("ing (the )te").toString());
        assertEquals("wabba the wabba", new RString("testing the test").replace("wabba").around("ing( the )te").toString());
        assertEquals("testing wabba test", new RString("testing the test").replace("wabba").inside("ing (the) te").toString());
    }
    
    @Test
    public void extract() throws Exception {
        assertEquals("the", new RString("testing the test").extract("t.e").toString());
        assertEquals("the", new RString("testing the test").extract("testing (.*) test").toString());
    }
    
    @Test
    public void serialize() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);
        
        objectOut.writeObject(new RString("test 1"));
        objectOut.writeObject(new RString("test 2"));
 
        objectOut.flush();
        objectOut.close();
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);
        
        assertEquals("test 1", objectIn.readObject().toString()); 
        assertEquals("test 2", objectIn.readObject().toString());
        
        objectIn.close();
    }
}
