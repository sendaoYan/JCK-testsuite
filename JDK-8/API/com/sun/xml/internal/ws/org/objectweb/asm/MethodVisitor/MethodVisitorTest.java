/*
 * @test
 * @summary com.sun.xml.internal.ws.org.objectweb.asm.MethodVisitor conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MethodVisitorTest
 */

import java.lang.reflect.Modifier;


public class MethodVisitorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.org.objectweb.asm.MethodVisitor", false, MethodVisitorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.org.objectweb.asm.MethodVisitor"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("visitAnnotation"), "method visitAnnotation");
        check(mn.contains("visitAnnotationDefault"), "method visitAnnotationDefault");
        check(mn.contains("visitAttribute"), "method visitAttribute");
        check(mn.contains("visitCode"), "method visitCode");
        check(mn.contains("visitEnd"), "method visitEnd");
        check(mn.contains("visitFieldInsn"), "method visitFieldInsn");
        check(mn.contains("visitFrame"), "method visitFrame");
        check(mn.contains("visitIincInsn"), "method visitIincInsn");
        check(mn.contains("visitInsn"), "method visitInsn");
        check(mn.contains("visitIntInsn"), "method visitIntInsn");
        check(mn.contains("visitJumpInsn"), "method visitJumpInsn");
        check(mn.contains("visitLabel"), "method visitLabel");
        check(mn.contains("visitLdcInsn"), "method visitLdcInsn");
        check(mn.contains("visitLineNumber"), "method visitLineNumber");
        check(mn.contains("visitLocalVariable"), "method visitLocalVariable");
        check(mn.contains("visitLookupSwitchInsn"), "method visitLookupSwitchInsn");
        check(mn.contains("visitMaxs"), "method visitMaxs");
        check(mn.contains("visitMethodInsn"), "method visitMethodInsn");
        check(mn.contains("visitMultiANewArrayInsn"), "method visitMultiANewArrayInsn");
        check(mn.contains("visitParameterAnnotation"), "method visitParameterAnnotation");
        check(mn.contains("visitTableSwitchInsn"), "method visitTableSwitchInsn");
        check(mn.contains("visitTryCatchBlock"), "method visitTryCatchBlock");
        check(mn.contains("visitTypeInsn"), "method visitTypeInsn");
        check(mn.contains("visitVarInsn"), "method visitVarInsn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
