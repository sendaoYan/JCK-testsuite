/*
 * @test
 * @summary org.omg.PortableServer.portable.Delegate conformance: interface shape.
 * @run main DelegateTest
 */

public class DelegateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.portable.Delegate");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): 8-channel delegation interface face ----
        java.lang.reflect.Method[] dm = c.getDeclaredMethods();
        check(dm.length == 8, "exactly 8 delegate methods");
        check(c.getInterfaces().length == 0, "no parent interfaces");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : dm) {
            names.add(m.getName());
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), m.getName() + " is abstract");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no checked exceptions");
        }
        check(names.equals(new java.util.HashSet<>(java.util.Arrays.asList(
                  "orb", "this_object", "poa", "object_id", "default_POA", "is_a", "non_existent", "get_interface_def"))),
              "delegate method name set exact");
        check(c.getDeclaredMethod("orb", org.omg.PortableServer.Servant.class).getReturnType() == org.omg.CORBA.ORB.class,
              "orb(Servant) -> ORB");
        check(c.getDeclaredMethod("this_object", org.omg.PortableServer.Servant.class).getReturnType() == org.omg.CORBA.Object.class,
              "this_object(Servant) -> Object");
        check(c.getDeclaredMethod("object_id", org.omg.PortableServer.Servant.class).getReturnType() == byte[].class,
              "object_id(Servant) -> byte[]");
        check(c.getDeclaredMethod("is_a", org.omg.PortableServer.Servant.class, String.class).getReturnType() == boolean.class,
              "is_a(Servant,String) -> boolean (only 2-param method)");
        check(c.getDeclaredMethod("is_a", org.omg.PortableServer.Servant.class, String.class).getParameterTypes().length == 2,
              "is_a is the only two-argument delegate method");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
