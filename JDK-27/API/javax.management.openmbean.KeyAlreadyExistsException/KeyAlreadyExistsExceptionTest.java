/*
 * @test
 * @summary javax.management.openmbean.KeyAlreadyExistsException conformance: white-box structure + black-box behavior.
 * @run main KeyAlreadyExistsExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.management.openmbean.KeyAlreadyExistsException;

public class KeyAlreadyExistsExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.KeyAlreadyExistsException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(IllegalArgumentException.class.isAssignableFrom(c), "extends IllegalArgumentException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(c.getConstructor(String.class) != null, "String ctor");
        check(c.getConstructor() != null, "no-arg ctor");
    }

    static void testBlackBox() {
        IllegalArgumentException e = new KeyAlreadyExistsException("msg");
        check(e.getMessage().equals("msg"), "message");
        IllegalArgumentException e2 = new KeyAlreadyExistsException();
        check(e2.getMessage() == null, "null message no-arg");

        // behavioral: thrown by TabularDataSupport.put for a duplicate calculated index
        try {
            javax.management.openmbean.CompositeType row =
                new javax.management.openmbean.CompositeType("Row", "d",
                    new String[]{"name"}, new String[]{"nd"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING});
            javax.management.openmbean.TabularType tt =
                new javax.management.openmbean.TabularType("T", "d", row, new String[]{"name"});
            javax.management.openmbean.TabularDataSupport td = new javax.management.openmbean.TabularDataSupport(tt);
            td.put(new javax.management.openmbean.CompositeDataSupport(row, new String[]{"name"}, new Object[]{"x"}));
            td.put(new javax.management.openmbean.CompositeDataSupport(row, new String[]{"name"}, new Object[]{"x"}));
            throw new AssertionError("expected KeyAlreadyExistsException");
        } catch (KeyAlreadyExistsException ex) {
            check(ex.getMessage().contains("already refers to a value"), "thrown message");
        } catch (Exception ex) {
            throw new AssertionError("wrong exception " + ex);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
