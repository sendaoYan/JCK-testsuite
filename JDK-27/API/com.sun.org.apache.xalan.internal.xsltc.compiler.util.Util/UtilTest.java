/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler.util:+open
 * @run main UtilTest
 */
public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util", false, UtilTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util"), "name");
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
        check(fd.size() == 0, "field-count");
        check(md.size() == 16, "method-count");
        check(md.contains("TRACE1"), "method-TRACE1");
        check(md.contains("TRACE2"), "method-TRACE2");
        check(md.contains("TRACE3"), "method-TRACE3");
        check(md.contains("baseName"), "method-baseName");
        check(md.contains("escape"), "method-escape");
        check(md.contains("getJCRefType"), "method-getJCRefType");
        check(md.contains("getLocalName"), "method-getLocalName");
        check(md.contains("getPrefix"), "method-getPrefix");
        check(md.contains("internalName"), "method-internalName");
        check(md.contains("isLiteral"), "method-isLiteral");
        check(md.contains("isValidQNames"), "method-isValidQNames");
        check(md.contains("noExtName"), "method-noExtName");
        check(md.contains("pathName"), "method-pathName");
        check(md.contains("println"), "method-println");
        check(md.contains("replace"), "method-replace");
        check(md.contains("toJavaName"), "method-toJavaName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
