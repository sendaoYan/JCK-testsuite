/*
 * @test
 * @summary javax.management.openmbean.InvalidKeyException conformance: white-box structure + black-box behavior.
 * @run main InvalidKeyExceptionTest
 */

import java.lang.reflect.Modifier;

public class InvalidKeyExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.InvalidKeyException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(IllegalArgumentException.class.isAssignableFrom(c), "extends IllegalArgumentException");
        check(!Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()), "concrete");
        check(c.getConstructor(String.class) != null, "String ctor");
        check(c.getConstructor() != null, "no-arg ctor");
    }

    static void testBlackBox() {
        IllegalArgumentException e = new javax.management.openmbean.InvalidKeyException("msg");
        check(e.getMessage().equals("msg"), "message");
        IllegalArgumentException e2 = new javax.management.openmbean.InvalidKeyException();
        check(e2.getMessage() == null, "null message no-arg");
        // behavioral: thrown by CompositeDataSupport.get for an unknown key
        try {
            new javax.management.openmbean.CompositeDataSupport(
                new javax.management.openmbean.CompositeType("T", "d",
                    new String[]{"a"}, new String[]{"da"}, new javax.management.openmbean.OpenType[]{javax.management.openmbean.SimpleType.STRING}),
                new String[]{"a"}, new Object[]{"v"}).get("missing");
            throw new AssertionError("expected InvalidKeyException");
        } catch (javax.management.openmbean.InvalidKeyException ex) {
            check(ex.getMessage().contains("missing"), "thrown message contains key");
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
