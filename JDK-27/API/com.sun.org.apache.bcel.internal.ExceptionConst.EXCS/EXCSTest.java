/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.ExceptionConst.EXCS conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal:+open
 * @run main EXCSTest
 */
public class EXCSTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.ExceptionConst$EXCS", false, EXCSTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.ExceptionConst$EXCS"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
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
        check(ec.size() == 5, "enum-constant-count");
        check(ec.contains("EXCS_CLASS_AND_INTERFACE_RESOLUTION"), "enum-EXCS_CLASS_AND_INTERFACE_RESOLUTION");
        check(ec.contains("EXCS_FIELD_AND_METHOD_RESOLUTION"), "enum-EXCS_FIELD_AND_METHOD_RESOLUTION");
        check(ec.contains("EXCS_INTERFACE_METHOD_RESOLUTION"), "enum-EXCS_INTERFACE_METHOD_RESOLUTION");
        check(ec.contains("EXCS_STRING_RESOLUTION"), "enum-EXCS_STRING_RESOLUTION");
        check(ec.contains("EXCS_ARRAY_EXCEPTION"), "enum-EXCS_ARRAY_EXCEPTION");
        check(fd.size() == 0, "field-count");
        check(md.size() == 2, "method-count");
        check(md.contains("valueOf"), "method-valueOf");
        check(md.contains("values"), "method-values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
