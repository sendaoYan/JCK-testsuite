/*
 * @test
 * @summary com.sun.xml.internal.ws.org.objectweb.asm.ClassWriter conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ClassWriterTest
 */

import java.lang.reflect.Modifier;


public class ClassWriterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.org.objectweb.asm.ClassWriter", false, ClassWriterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.org.objectweb.asm.ClassWriter"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.org.objectweb.asm.ClassVisitor"), "implements com.sun.xml.internal.ws.org.objectweb.asm.ClassVisitor");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getCommonSuperClass"), "method getCommonSuperClass");
        check(mn.contains("newClass"), "method newClass");
        check(mn.contains("newConst"), "method newConst");
        check(mn.contains("newField"), "method newField");
        check(mn.contains("newMethod"), "method newMethod");
        check(mn.contains("newNameType"), "method newNameType");
        check(mn.contains("newUTF8"), "method newUTF8");
        check(mn.contains("toByteArray"), "method toByteArray");
        check(mn.contains("visit"), "method visit");
        check(mn.contains("visitAnnotation"), "method visitAnnotation");
        check(mn.contains("visitAttribute"), "method visitAttribute");
        check(mn.contains("visitEnd"), "method visitEnd");
        check(mn.contains("visitField"), "method visitField");
        check(mn.contains("visitInnerClass"), "method visitInnerClass");
        check(mn.contains("visitMethod"), "method visitMethod");
        check(mn.contains("visitOuterClass"), "method visitOuterClass");
        check(mn.contains("visitSource"), "method visitSource");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("COMPUTE_FRAMES"), "field COMPUTE_FRAMES");
        check(fn.contains("COMPUTE_MAXS"), "field COMPUTE_MAXS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
