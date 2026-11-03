/*
 * @test
 * @summary javax.management.openmbean.InvalidOpenTypeException conformance: white-box structure + black-box behavior.
 * @run main InvalidOpenTypeExceptionTest
 */

import java.lang.reflect.Modifier;
import javax.management.openmbean.InvalidOpenTypeException;

public class InvalidOpenTypeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.InvalidOpenTypeException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(IllegalArgumentException.class.isAssignableFrom(c), "extends IllegalArgumentException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(c.getConstructor(String.class) != null, "String ctor");
        check(c.getConstructor() != null, "no-arg ctor");
    }

    static void testBlackBox() {
        IllegalArgumentException e = new InvalidOpenTypeException("msg");
        check(e.getMessage().equals("msg"), "message");
        IllegalArgumentException e2 = new InvalidOpenTypeException();
        check(e2.getMessage() == null, "null message no-arg");

        // behavioral: thrown by TabularDataSupport.calculateIndex for a wrong row type
        try {
            javax.management.openmbean.CompositeType goodRow =
                new javax.management.openmbean.CompositeType("Row", "d",
                    new String[]{"name"}, new String[]{"nd"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING});
            javax.management.openmbean.TabularType tt =
                new javax.management.openmbean.TabularType("T", "d", goodRow, new String[]{"name"});
            javax.management.openmbean.TabularDataSupport td = new javax.management.openmbean.TabularDataSupport(tt);
            javax.management.openmbean.CompositeType badRow =
                new javax.management.openmbean.CompositeType("Other", "d",
                    new String[]{"x"}, new String[]{"dx"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING});
            javax.management.openmbean.CompositeDataSupport bad =
                new javax.management.openmbean.CompositeDataSupport(badRow, new String[]{"x"}, new Object[]{"v"});
            td.calculateIndex(bad);
            throw new AssertionError("expected InvalidOpenTypeException");
        } catch (InvalidOpenTypeException ex) {
            check(ex.getMessage().contains("is not assignable"), "thrown message");
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
