/*
 * @test
 * @summary jdk.xml.internal.XMLLimitAnalyzer conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main XMLLimitAnalyzerTest
 */
public class XMLLimitAnalyzerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLLimitAnalyzer", false, XMLLimitAnalyzerTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLLimitAnalyzer"), "name");
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
        check(md.size() == 9, "method-count");
        check(md.contains("addValue"), "method-addValue");
        check(md.contains("debugPrint"), "method-debugPrint");
        check(md.contains("endEntity"), "method-endEntity");
        check(md.contains("getTotalValue"), "method-getTotalValue");
        check(md.contains("getValue"), "method-getValue");
        check(md.contains("getValueByIndex"), "method-getValueByIndex");
        check(md.contains("isTracking"), "method-isTracking");
        check(md.contains("reset"), "method-reset");
        check(md.contains("startEntity"), "method-startEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
