/*
 * @test
 * @summary javax.management.openmbean.OpenDataException conformance: white-box structure + black-box behavior.
 * @run main OpenDataExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.management.JMException;
import javax.management.openmbean.OpenDataException;

public class OpenDataExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenDataException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(JMException.class.isAssignableFrom(c), "extends JMException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(c.getConstructor(String.class) != null, "String ctor");
        check(c.getConstructor() != null, "no-arg ctor");
    }

    static void testBlackBox() {
        Exception e = new OpenDataException("msg");
        check(e.getMessage().equals("msg"), "message");
        Exception e2 = new OpenDataException();
        check(e2.getMessage() == null, "null message no-arg");

        // behavioral: thrown by CompositeType for a duplicate item name
        try {
            new javax.management.openmbean.CompositeType("T", "d",
                new String[]{"a", "a"}, new String[]{"x", "y"},
                new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING, javax.management.openmbean.SimpleType.INTEGER});
            throw new AssertionError("expected OpenDataException");
        } catch (OpenDataException ex) {
            check(ex.getMessage().contains("duplicates a previous item names"), "thrown message");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
