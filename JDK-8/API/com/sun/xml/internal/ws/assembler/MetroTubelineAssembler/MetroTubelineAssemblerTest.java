/*
 * @test
 * @summary com.sun.xml.internal.ws.assembler.MetroTubelineAssembler conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MetroTubelineAssemblerTest
 */

import java.lang.reflect.Modifier;


public class MetroTubelineAssemblerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.assembler.MetroTubelineAssembler", false, MetroTubelineAssemblerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.assembler.MetroTubelineAssembler"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.pipe.TubelineAssembler"), "implements com.sun.xml.internal.ws.api.pipe.TubelineAssembler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createClient"), "method createClient");
        check(mn.contains("createClientContext"), "method createClientContext");
        check(mn.contains("createServer"), "method createServer");
        check(mn.contains("createServerContext"), "method createServerContext");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("JAXWS_TUBES_CONFIG_NAMES"), "field JAXWS_TUBES_CONFIG_NAMES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
