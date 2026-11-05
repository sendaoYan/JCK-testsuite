/*
 * @test
 * @summary org.omg.CORBA.DynUnion conformance: interface shape.
 * @run main DynUnionTest
 */

public class DynUnionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DynUnion");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.DynAny.class.isAssignableFrom(c), "extends org.omg.CORBA.DynAny");

        // ---- deepened: exact own method surface (javap-verified on jdk8u372, deprecated org.omg.CORBA.DynAny family) ----
        java.util.Set<String> own = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.DynUnion.class.getDeclaredMethods()) own.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList("discriminator", "discriminator_kind", "member", "member_kind", "member_name", "set_as_default"));
        check(own.equals(expected), "DynUnion declares exactly 6 own ops: " + own);
        check(c.getMethod("discriminator").getReturnType() == org.omg.CORBA.DynAny.class, "discriminator() returns DynAny");
        check(c.getMethod("member").getReturnType() == org.omg.CORBA.DynAny.class, "member() returns DynAny");
        check(c.getMethod("member_name").getReturnType() == String.class, "member_name() returns String");
        check(c.getMethod("member_name", String.class) != null, "member_name(String) setter exists");
        check(c.getMethod("set_as_default", boolean.class) != null, "set_as_default(boolean) setter exists");
        check(org.omg.CORBA.DynAny.class.isAssignableFrom(org.omg.CORBA.DynUnion.class), "DynUnion extends org.omg.CORBA.DynAny (old family)");
        check(org.omg.CORBA.Object.class.isAssignableFrom(org.omg.CORBA.DynUnion.class), "DynUnion extends org.omg.CORBA.Object");
        check(org.omg.CORBA.DynUnion.class.isAnnotationPresent(Deprecated.class), "DynUnion is @Deprecated (superseded by org.omg.DynamicAny)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
