/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.Select conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main SelectTest
 */
public class SelectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.Select", false, SelectTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.Select"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.BranchInstruction"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 3, "interface-count");
        check(ifs.contains("com.sun.org.apache.bcel.internal.generic.VariableLengthInstruction"), "if-com.sun.org.apache.bcel.internal.generic.VariableLengthInstruction");
        check(ifs.contains("com.sun.org.apache.bcel.internal.generic.StackConsumer"), "if-com.sun.org.apache.bcel.internal.generic.StackConsumer");
        check(ifs.contains("com.sun.org.apache.bcel.internal.generic.StackProducer"), "if-com.sun.org.apache.bcel.internal.generic.StackProducer");
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
        check(fd.size() == 6, "field-count");
        check(fd.contains("match"), "field-match");
        check(fd.contains("indices"), "field-indices");
        check(fd.contains("targets"), "field-targets");
        check(fd.contains("fixed_length"), "field-fixed_length");
        check(fd.contains("match_length"), "field-match_length");
        check(fd.contains("padding"), "field-padding");
        check(md.size() == 11, "method-count");
        check(md.contains("clone"), "method-clone");
        check(md.contains("containsTarget"), "method-containsTarget");
        check(md.contains("dump"), "method-dump");
        check(md.contains("getIndices"), "method-getIndices");
        check(md.contains("getMatchs"), "method-getMatchs");
        check(md.contains("getTargets"), "method-getTargets");
        check(md.contains("initFromFile"), "method-initFromFile");
        check(md.contains("setTarget"), "method-setTarget");
        check(md.contains("toString"), "method-toString");
        check(md.contains("updatePosition"), "method-updatePosition");
        check(md.contains("updateTarget"), "method-updateTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
