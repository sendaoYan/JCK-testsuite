/*
 * @test
 * @summary com.sun.xml.internal.ws.transport.http.HttpAdapterList conformance: reflection (kind, superclass, interfaces, methods).
 * @run main HttpAdapterListTest
 */

import java.lang.reflect.Modifier;


public class HttpAdapterListTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.transport.http.HttpAdapterList", false, HttpAdapterListTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.transport.http.HttpAdapterList"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.util.AbstractList"), "superclass java.util.AbstractList");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser$AdapterFactory"), "implements com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser$AdapterFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createAdapter"), "method createAdapter");
        check(mn.contains("createHttpAdapter"), "method createHttpAdapter");
        check(mn.contains("createPortAddressResolver"), "method createPortAddressResolver");
        check(mn.contains("get"), "method get");
        check(mn.contains("size"), "method size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
