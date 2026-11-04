/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.ExceptionConst conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal:+open
 * @run main ExceptionConstTest
 */
public class ExceptionConstTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.ExceptionConst", false, ExceptionConstTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.ExceptionConst"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
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
        check(fd.size() == 21, "field-count");
        check(fd.contains("THROWABLE"), "field-THROWABLE");
        check(fd.contains("RUNTIME_EXCEPTION"), "field-RUNTIME_EXCEPTION");
        check(fd.contains("LINKING_EXCEPTION"), "field-LINKING_EXCEPTION");
        check(fd.contains("CLASS_CIRCULARITY_ERROR"), "field-CLASS_CIRCULARITY_ERROR");
        check(fd.contains("CLASS_FORMAT_ERROR"), "field-CLASS_FORMAT_ERROR");
        check(fd.contains("EXCEPTION_IN_INITIALIZER_ERROR"), "field-EXCEPTION_IN_INITIALIZER_ERROR");
        check(fd.contains("INCOMPATIBLE_CLASS_CHANGE_ERROR"), "field-INCOMPATIBLE_CLASS_CHANGE_ERROR");
        check(fd.contains("ABSTRACT_METHOD_ERROR"), "field-ABSTRACT_METHOD_ERROR");
        check(fd.contains("ILLEGAL_ACCESS_ERROR"), "field-ILLEGAL_ACCESS_ERROR");
        check(fd.contains("INSTANTIATION_ERROR"), "field-INSTANTIATION_ERROR");
        check(fd.contains("NO_SUCH_FIELD_ERROR"), "field-NO_SUCH_FIELD_ERROR");
        check(fd.contains("NO_SUCH_METHOD_ERROR"), "field-NO_SUCH_METHOD_ERROR");
        check(fd.contains("NO_CLASS_DEF_FOUND_ERROR"), "field-NO_CLASS_DEF_FOUND_ERROR");
        check(fd.contains("UNSATISFIED_LINK_ERROR"), "field-UNSATISFIED_LINK_ERROR");
        check(fd.contains("VERIFY_ERROR"), "field-VERIFY_ERROR");
        check(fd.contains("NULL_POINTER_EXCEPTION"), "field-NULL_POINTER_EXCEPTION");
        check(fd.contains("ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION"), "field-ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION");
        check(fd.contains("ARITHMETIC_EXCEPTION"), "field-ARITHMETIC_EXCEPTION");
        check(fd.contains("NEGATIVE_ARRAY_SIZE_EXCEPTION"), "field-NEGATIVE_ARRAY_SIZE_EXCEPTION");
        check(fd.contains("CLASS_CAST_EXCEPTION"), "field-CLASS_CAST_EXCEPTION");
        check(fd.contains("ILLEGAL_MONITOR_STATE"), "field-ILLEGAL_MONITOR_STATE");
        check(md.size() == 1, "method-count");
        check(md.contains("createExceptions"), "method-createExceptions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
