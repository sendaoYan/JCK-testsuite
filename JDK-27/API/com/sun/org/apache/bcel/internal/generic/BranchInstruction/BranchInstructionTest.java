/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.BranchInstruction conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main BranchInstructionTest
 */
public class BranchInstructionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.BranchInstruction", false, BranchInstructionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.BranchInstruction"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.Instruction"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("com.sun.org.apache.bcel.internal.generic.InstructionTargeter"), "if-com.sun.org.apache.bcel.internal.generic.InstructionTargeter");
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
        check(fd.size() == 3, "field-count");
        check(fd.contains("index"), "field-index");
        check(fd.contains("target"), "field-target");
        check(fd.contains("position"), "field-position");
        check(md.size() == 13, "method-count");
        check(md.contains("containsTarget"), "method-containsTarget");
        check(md.contains("dump"), "method-dump");
        check(md.contains("getIndex"), "method-getIndex");
        check(md.contains("getPosition"), "method-getPosition");
        check(md.contains("getTarget"), "method-getTarget");
        check(md.contains("getTargetOffset"), "method-getTargetOffset");
        check(md.contains("initFromFile"), "method-initFromFile");
        check(md.contains("setIndex"), "method-setIndex");
        check(md.contains("setPosition"), "method-setPosition");
        check(md.contains("setTarget"), "method-setTarget");
        check(md.contains("toString"), "method-toString");
        check(md.contains("updatePosition"), "method-updatePosition");
        check(md.contains("updateTarget"), "method-updateTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
