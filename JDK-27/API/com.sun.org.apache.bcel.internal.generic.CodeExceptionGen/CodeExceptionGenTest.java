/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.CodeExceptionGen conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main CodeExceptionGenTest
 */
public class CodeExceptionGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.CodeExceptionGen", false, CodeExceptionGenTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.CodeExceptionGen"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "interface-count");
        check(ifs.contains("com.sun.org.apache.bcel.internal.generic.InstructionTargeter"), "if-com.sun.org.apache.bcel.internal.generic.InstructionTargeter");
        check(ifs.contains("java.lang.Cloneable"), "if-java.lang.Cloneable");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 0, "field-count");
        check(md.size() == 13, "method-count");
        check(md.contains("clone"), "method-clone");
        check(md.contains("containsTarget"), "method-containsTarget");
        check(md.contains("getCatchType"), "method-getCatchType");
        check(md.contains("getCodeException"), "method-getCodeException");
        check(md.contains("getEndPC"), "method-getEndPC");
        check(md.contains("getHandlerPC"), "method-getHandlerPC");
        check(md.contains("getStartPC"), "method-getStartPC");
        check(md.contains("setCatchType"), "method-setCatchType");
        check(md.contains("setEndPC"), "method-setEndPC");
        check(md.contains("setHandlerPC"), "method-setHandlerPC");
        check(md.contains("setStartPC"), "method-setStartPC");
        check(md.contains("toString"), "method-toString");
        check(md.contains("updateTarget"), "method-updateTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
