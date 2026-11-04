/*
 * @test
 * @summary javax.swing.text.BadLocationException conformance: checked exception / fields / serialization (headless-safe).
 * @run main BadLocationExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.text.BadLocationException;

public class BadLocationExceptionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testBasic() {
        BadLocationException e = new BadLocationException("boom", 3);
        check("boom".equals(e.getMessage()), "message");
        check(e.offsetRequested() == 3, "offsetRequested 3");
        check(e instanceof Exception, "is checked Exception");

        BadLocationException neg = new BadLocationException("neg", -1);
        check(neg.offsetRequested() == -1, "negative offsetRequested");
        check("neg".equals(neg.getMessage()), "negative message");

        BadLocationException zero = new BadLocationException(null, 0);
        check(zero.getMessage() == null, "null message preserved");
        check(zero.offsetRequested() == 0, "zero offsetRequested");
    }

    static void testSerialization() throws Exception {
        BadLocationException e = new BadLocationException("s", 42);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        BadLocationException r = (BadLocationException) ois.readObject();
        check(r != e, "round-trip distinct instance");
        check("s".equals(r.getMessage()), "round-trip message");
        check(r.offsetRequested() == 42, "round-trip offsetRequested");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
