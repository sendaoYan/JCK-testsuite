/*
 * @test
 * @summary org.omg.PortableServer.CurrentOperations conformance: interface shape.
 * @run main CurrentOperationsTest
 */

public class CurrentOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.CurrentOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.CurrentOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.CurrentOperations");
        // ---- deepened (jdk8u372-b07 probed): 2 own methods both throwing NoContext ----
        java.lang.reflect.Method[] ops = c.getDeclaredMethods();
        check(ops.length == 2, "exactly 2 own methods: get_POA/get_object_id");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == org.omg.CORBA.CurrentOperations.class,
              "parent is CORBA.CurrentOperations only");
        check(org.omg.CORBA.CurrentOperations.class.getDeclaredMethods().length == 0,
              "CORBA.CurrentOperations is an empty marker interface");
        check(c.getMethods().length == 2, "full inherited face is still just these 2 methods");
        check(c.getDeclaredMethod("get_POA").getReturnType() == org.omg.PortableServer.POA.class
           && c.getDeclaredMethod("get_object_id").getReturnType() == byte[].class,
              "get_POA() -> POA, get_object_id() -> byte[]");
        for (String n : new String[]{"get_POA", "get_object_id"}) {
            Class<?>[] exc = c.getDeclaredMethod(n).getExceptionTypes();
            check(exc.length == 1 && exc[0] == org.omg.PortableServer.CurrentPackage.NoContext.class,
                  n + " throws exactly NoContext");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
