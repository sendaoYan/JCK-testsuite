/*
 * @test
 * @summary org.omg.CORBA.CustomMarshal conformance: interface shape.
 * @run main CustomMarshalTest
 */

public class CustomMarshalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.CustomMarshal");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.CustomMarshal.class.getMethods()) names.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList("marshal","unmarshal"));
        check(names.equals(expected), "CustomMarshal declares exactly marshal,unmarshal: " + names);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
