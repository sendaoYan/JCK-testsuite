/*
 * @test
 * @summary com.sun.xml.internal.ws.org.objectweb.asm.Type conformance: reflection (kind, superclass, interfaces, methods).
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;


public class TypeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.org.objectweb.asm.Type", false, TypeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.org.objectweb.asm.Type"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getArgumentTypes"), "method getArgumentTypes");
        check(mn.contains("getClassName"), "method getClassName");
        check(mn.contains("getConstructorDescriptor"), "method getConstructorDescriptor");
        check(mn.contains("getDescriptor"), "method getDescriptor");
        check(mn.contains("getDimensions"), "method getDimensions");
        check(mn.contains("getElementType"), "method getElementType");
        check(mn.contains("getInternalName"), "method getInternalName");
        check(mn.contains("getMethodDescriptor"), "method getMethodDescriptor");
        check(mn.contains("getObjectType"), "method getObjectType");
        check(mn.contains("getOpcode"), "method getOpcode");
        check(mn.contains("getReturnType"), "method getReturnType");
        check(mn.contains("getSize"), "method getSize");
        check(mn.contains("getSort"), "method getSort");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("toString"), "method toString");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ARRAY"), "field ARRAY");
        check(fn.contains("BOOLEAN"), "field BOOLEAN");
        check(fn.contains("BOOLEAN_TYPE"), "field BOOLEAN_TYPE");
        check(fn.contains("BYTE"), "field BYTE");
        check(fn.contains("BYTE_TYPE"), "field BYTE_TYPE");
        check(fn.contains("CHAR"), "field CHAR");
        check(fn.contains("CHAR_TYPE"), "field CHAR_TYPE");
        check(fn.contains("DOUBLE"), "field DOUBLE");
        check(fn.contains("DOUBLE_TYPE"), "field DOUBLE_TYPE");
        check(fn.contains("FLOAT"), "field FLOAT");
        check(fn.contains("FLOAT_TYPE"), "field FLOAT_TYPE");
        check(fn.contains("INT"), "field INT");
        check(fn.contains("INT_TYPE"), "field INT_TYPE");
        check(fn.contains("LONG"), "field LONG");
        check(fn.contains("LONG_TYPE"), "field LONG_TYPE");
        check(fn.contains("OBJECT"), "field OBJECT");
        check(fn.contains("SHORT"), "field SHORT");
        check(fn.contains("SHORT_TYPE"), "field SHORT_TYPE");
        check(fn.contains("VOID"), "field VOID");
        check(fn.contains("VOID_TYPE"), "field VOID_TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
