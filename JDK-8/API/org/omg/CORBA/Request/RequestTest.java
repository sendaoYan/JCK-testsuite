/*
 * @test
 * @summary org.omg.CORBA.Request conformance: abstract class shape.
 * @run main RequestTest
 */

public class RequestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.Request");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() == java.lang.Object.class, "direct subclass of Object");

        // exact method-name surface (21 own ops, javap-verified on jdk8u372; getDeclaredMethods
        // so the inherited java.lang.Object methods are excluded)
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList(
            "add_in_arg", "add_inout_arg", "add_named_in_arg", "add_named_inout_arg", "add_named_out_arg",
            "add_out_arg", "arguments", "contexts", "ctx", "env", "exceptions", "get_response", "invoke",
            "operation", "poll_response", "result", "return_value", "send_deferred", "send_oneway",
            "set_return_type", "target"));
        check(names.equals(expected), "Request declares exactly the 21 DII ops: " + names);

        // argument/return shapes of key ops
        check(returnsType(c, "target", org.omg.CORBA.Object.class), "target() returns org.omg.CORBA.Object");
        check(returnsType(c, "arguments", org.omg.CORBA.NVList.class), "arguments() returns NVList");
        check(returnsType(c, "result", org.omg.CORBA.NamedValue.class), "result() returns NamedValue");
        check(returnsType(c, "env", org.omg.CORBA.Environment.class), "env() returns Environment");
        check(returnsType(c, "exceptions", org.omg.CORBA.ExceptionList.class), "exceptions() returns ExceptionList");
        check(returnsType(c, "contexts", org.omg.CORBA.ContextList.class), "contexts() returns ContextList");
        check(returnsType(c, "return_value", org.omg.CORBA.Any.class), "return_value() returns Any");
        check(takesType(c, "ctx", org.omg.CORBA.Context.class), "ctx(Context) setter exists");
        check(takesType(c, "set_return_type", org.omg.CORBA.TypeCode.class), "set_return_type(TypeCode)");
        check(takesType(c, "add_named_in_arg", String.class), "add_named_in_arg(String)");

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
