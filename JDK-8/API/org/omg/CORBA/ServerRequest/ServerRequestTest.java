/*
 * @test
 * @summary org.omg.CORBA.ServerRequest conformance: abstract class shape.
 * @run main ServerRequestTest
 */

public class ServerRequestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.ServerRequest");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() == java.lang.Object.class, "direct subclass of Object");

        // exact method-name surface (9 methods, javap-verified on jdk8u372; getDeclaredMethods
        // so the inherited java.lang.Object methods are excluded)
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList(
            "arguments", "ctx", "except", "op_name", "operation", "params", "result",
            "set_exception", "set_result"));
        check(names.equals(expected), "ServerRequest declares exactly the 9 DSI ops: " + names);

        check(returnsType(c, "op_name", String.class), "op_name() returns String");
        check(returnsType(c, "operation", String.class), "operation() returns String");
        check(returnsType(c, "ctx", org.omg.CORBA.Context.class), "ctx() returns Context");
        check(takesType(c, "result", org.omg.CORBA.Any.class),
              "result(Any) is the reply setter (no no-arg result getter, jdk8u372)");
        check(takesType(c, "arguments", org.omg.CORBA.NVList.class), "arguments(NVList) exists");
        check(takesType(c, "params", org.omg.CORBA.NVList.class), "params(NVList) exists");
        check(takesType(c, "except", java.lang.Exception.class) || takesType(c, "except", org.omg.CORBA.Any.class),
              "except takes Any/Exception");
        check(takesType(c, "set_exception", org.omg.CORBA.Any.class), "set_exception(Any) exists");
        check(takesType(c, "set_result", org.omg.CORBA.Any.class), "set_result(Any) exists");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean returnsType(Class<?> c, String n, Class<?> rt) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(n) && m.getParameterTypes().length == 0 && m.getReturnType() == rt) return true;
        return false;
    }

    static boolean takesType(Class<?> c, String n, Class<?> pt) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(n))
                for (Class<?> p : m.getParameterTypes()) if (p == pt) return true;
        return false;
    }
}
