/*
 * @test
 * @summary com.sun.xml.internal.ws.api.server.InstanceResolver conformance: reflection (kind, superclass, interfaces, methods).
 * @run main InstanceResolverTest
 */

import java.lang.reflect.Modifier;


public class InstanceResolverTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.server.InstanceResolver", false, InstanceResolverTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.server.InstanceResolver"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createDefault"), "method createDefault");
        check(mn.contains("createFromInstanceResolverAnnotation"), "method createFromInstanceResolverAnnotation");
        check(mn.contains("createInvoker"), "method createInvoker");
        check(mn.contains("createNewInstance"), "method createNewInstance");
        check(mn.contains("createSingleton"), "method createSingleton");
        check(mn.contains("dispose"), "method dispose");
        check(mn.contains("postInvoke"), "method postInvoke");
        check(mn.contains("resolve"), "method resolve");
        check(mn.contains("start"), "method start");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
