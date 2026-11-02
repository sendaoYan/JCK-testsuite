/*
 * @test
 * @summary java.rmi.server.RemoteObjectInvocationHandler conformance: type shape.
 * @run main RemoteObjectInvocationHandlerTest
 */

import java.lang.reflect.Modifier;

public class RemoteObjectInvocationHandlerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RemoteObjectInvocationHandler");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == true, "public");
        check(Modifier.isProtected(m) == false, "protected");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == false, "abstract");
        check(Modifier.isFinal(m) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.rmi.server.RemoteObject"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        java.util.Set<String> got = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) got.add(i.getName());
        check(got.contains("java.lang.reflect.InvocationHandler"), "implements java.lang.reflect.InvocationHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
