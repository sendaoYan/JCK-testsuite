/*
 * @test
 * @summary com.sun.xml.internal.ws.model.ReflectAnnotationReader conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ReflectAnnotationReaderTest
 */

import java.lang.reflect.Modifier;


public class ReflectAnnotationReaderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.ReflectAnnotationReader", false, ReflectAnnotationReaderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.ReflectAnnotationReader"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.databinding.MetadataReader"), "implements com.sun.xml.internal.ws.api.databinding.MetadataReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("getParameterAnnotations"), "method getParameterAnnotations");
        check(mn.contains("getProperties"), "method getProperties");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
