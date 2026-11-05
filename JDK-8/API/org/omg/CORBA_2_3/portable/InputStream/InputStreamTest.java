/*
 * @test
 * @summary org.omg.CORBA_2_3.portable.InputStream conformance: abstract class shape.
 * @run main InputStreamTest
 */

public class InputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA_2_3.portable.InputStream");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.CORBA.portable.InputStream.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.InputStream");
        // ---- deepened (jdk8u372-b07 probed): 2.3 InputStream value-stream method face ----
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == org.omg.CORBA.portable.InputStream.class, "extends org.omg.CORBA.portable.InputStream");
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.equals(new java.util.TreeSet<String>(java.util.Arrays.asList(
                  "read_value", "read_abstract_interface", "checkPermission"))),
              "declares read_value overloads + read_abstract_interface overloads + package-private checkPermission");
        check(c.getDeclaredMethods().length == 8, "8 declared methods (5 read_value + 2 read_abstract_interface + 1 checkPermission)");
        int readValueOverloads = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals("read_value")) readValueOverloads++;
        check(readValueOverloads == 5, "5 read_value overloads: (), (Class), (BoxedValueHelper), (String rep-id), (Serializable)");
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (m.getName().equals("read_value") && m.getParameterTypes().length == 0)
                check(m.getReturnType() == java.io.Serializable.class, "read_value() returns Serializable");
        check(c.getConstructor() != null, "public no-arg constructor present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
