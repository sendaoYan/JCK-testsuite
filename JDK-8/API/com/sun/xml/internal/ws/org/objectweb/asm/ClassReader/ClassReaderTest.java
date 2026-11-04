/*
 * @test
 * @summary com.sun.xml.internal.ws.org.objectweb.asm.ClassReader conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ClassReaderTest
 */

import java.lang.reflect.Modifier;


public class ClassReaderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.org.objectweb.asm.ClassReader", false, ClassReaderTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.org.objectweb.asm.ClassReader"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("accept"), "method accept");
        check(mn.contains("getAccess"), "method getAccess");
        check(mn.contains("getClassName"), "method getClassName");
        check(mn.contains("getInterfaces"), "method getInterfaces");
        check(mn.contains("getItem"), "method getItem");
        check(mn.contains("getSuperName"), "method getSuperName");
        check(mn.contains("readByte"), "method readByte");
        check(mn.contains("readClass"), "method readClass");
        check(mn.contains("readConst"), "method readConst");
        check(mn.contains("readInt"), "method readInt");
        check(mn.contains("readLabel"), "method readLabel");
        check(mn.contains("readLong"), "method readLong");
        check(mn.contains("readShort"), "method readShort");
        check(mn.contains("readUTF8"), "method readUTF8");
        check(mn.contains("readUnsignedShort"), "method readUnsignedShort");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("EXPAND_FRAMES"), "field EXPAND_FRAMES");
        check(fn.contains("SKIP_CODE"), "field SKIP_CODE");
        check(fn.contains("SKIP_DEBUG"), "field SKIP_DEBUG");
        check(fn.contains("SKIP_FRAMES"), "field SKIP_FRAMES");
        check(fn.contains("b"), "field b");
        check(fn.contains("header"), "field header");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
