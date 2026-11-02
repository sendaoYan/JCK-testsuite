/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.Instruction conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main InstructionTest
 */
public class InstructionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.Instruction", false, InstructionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.Instruction"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
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
        check(fd.size() == 2, "field-count");
        check(fd.contains("length"), "field-length");
        check(fd.contains("opcode"), "field-opcode");
        check(md.size() == 17, "method-count");
        check(md.contains("accept"), "method-accept");
        check(md.contains("consumeStack"), "method-consumeStack");
        check(md.contains("copy"), "method-copy");
        check(md.contains("dump"), "method-dump");
        check(md.contains("equals"), "method-equals");
        check(md.contains("getComparator"), "method-getComparator");
        check(md.contains("getLength"), "method-getLength");
        check(md.contains("getName"), "method-getName");
        check(md.contains("getOpcode"), "method-getOpcode");
        check(md.contains("hashCode"), "method-hashCode");
        check(md.contains("initFromFile"), "method-initFromFile");
        check(md.contains("isValidByte"), "method-isValidByte");
        check(md.contains("isValidShort"), "method-isValidShort");
        check(md.contains("produceStack"), "method-produceStack");
        check(md.contains("readInstruction"), "method-readInstruction");
        check(md.contains("setComparator"), "method-setComparator");
        check(md.contains("toString"), "method-toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
