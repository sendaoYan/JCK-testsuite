/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator.LocalVariableRegistry conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler.util:+open
 * @run main LocalVariableRegistryTest
 */
public class LocalVariableRegistryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator$LocalVariableRegistry", false, LocalVariableRegistryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator$LocalVariableRegistry"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
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
        check(fd.size() == 2, "field-count");
        check(fd.contains("_variables"), "field-_variables");
        check(fd.contains("_nameToLVGMap"), "field-_nameToLVGMap");
        check(md.size() == 5, "method-count");
        check(md.contains("lookUpByName"), "method-lookUpByName");
        check(md.contains("lookupRegisteredLocalVariable"), "method-lookupRegisteredLocalVariable");
        check(md.contains("registerByName"), "method-registerByName");
        check(md.contains("registerLocalVariable"), "method-registerLocalVariable");
        check(md.contains("removeByNameTracking"), "method-removeByNameTracking");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
