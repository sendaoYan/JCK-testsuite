/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.InstructionFactory conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main InstructionFactoryTest
 */
public class InstructionFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.InstructionFactory", false, InstructionFactoryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.InstructionFactory"), "name");
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
        check(fd.contains("cg"), "field-cg");
        check(fd.contains("cp"), "field-cp");
        check(md.size() == 31, "method-count");
        check(md.contains("createAppend"), "method-createAppend");
        check(md.contains("createArrayLoad"), "method-createArrayLoad");
        check(md.contains("createArrayStore"), "method-createArrayStore");
        check(md.contains("createBinaryOperation"), "method-createBinaryOperation");
        check(md.contains("createBranchInstruction"), "method-createBranchInstruction");
        check(md.contains("createCast"), "method-createCast");
        check(md.contains("createCheckCast"), "method-createCheckCast");
        check(md.contains("createConstant"), "method-createConstant");
        check(md.contains("createDup"), "method-createDup");
        check(md.contains("createDup_1"), "method-createDup_1");
        check(md.contains("createDup_2"), "method-createDup_2");
        check(md.contains("createFieldAccess"), "method-createFieldAccess");
        check(md.contains("createGetField"), "method-createGetField");
        check(md.contains("createGetStatic"), "method-createGetStatic");
        check(md.contains("createInstanceOf"), "method-createInstanceOf");
        check(md.contains("createInvoke"), "method-createInvoke");
        check(md.contains("createLoad"), "method-createLoad");
        check(md.contains("createNew"), "method-createNew");
        check(md.contains("createNewArray"), "method-createNewArray");
        check(md.contains("createNull"), "method-createNull");
        check(md.contains("createPop"), "method-createPop");
        check(md.contains("createPrintln"), "method-createPrintln");
        check(md.contains("createPutField"), "method-createPutField");
        check(md.contains("createPutStatic"), "method-createPutStatic");
        check(md.contains("createReturn"), "method-createReturn");
        check(md.contains("createStore"), "method-createStore");
        check(md.contains("createThis"), "method-createThis");
        check(md.contains("getClassGen"), "method-getClassGen");
        check(md.contains("getConstantPool"), "method-getConstantPool");
        check(md.contains("setClassGen"), "method-setClassGen");
        check(md.contains("setConstantPool"), "method-setConstantPool");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
