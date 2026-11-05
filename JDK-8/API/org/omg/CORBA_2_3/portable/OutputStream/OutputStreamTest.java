/*
 * @test
 * @summary org.omg.CORBA_2_3.portable.OutputStream conformance: abstract class shape.
 * @run main OutputStreamTest
 */

public class OutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA_2_3.portable.OutputStream");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.portable.OutputStream.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.OutputStream");
        // ---- deepened (jdk8u372-b07 probed): 2.3 OutputStream value-stream method face ----
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == org.omg.CORBA.portable.OutputStream.class, "extends org.omg.CORBA.portable.OutputStream");
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.equals(new java.util.TreeSet<String>(java.util.Arrays.asList(
                  "write_value", "write_abstract_interface", "checkPermission"))),
              "declares write_value overloads + write_abstract_interface + package-private checkPermission");
        check(c.getDeclaredMethods().length == 6, "6 declared methods (4 write_value + 1 write_abstract_interface + 1 checkPermission)");
        int wv = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals("write_value")) wv++;
        check(wv == 4, "4 write_value overloads: (Serializable), (Serializable,Class), (Serializable,String), (Serializable,BoxedValueHelper)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
