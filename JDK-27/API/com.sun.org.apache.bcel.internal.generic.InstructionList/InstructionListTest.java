/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.InstructionList conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main InstructionListTest
 */
public class InstructionListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.InstructionList", false, InstructionListTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.InstructionList"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("java.lang.Iterable"), "if-java.lang.Iterable");
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
        check(md.size() == 27, "method-count");
        check(md.contains("addObserver"), "method-addObserver");
        check(md.contains("append"), "method-append");
        check(md.contains("contains"), "method-contains");
        check(md.contains("copy"), "method-copy");
        check(md.contains("delete"), "method-delete");
        check(md.contains("dispose"), "method-dispose");
        check(md.contains("findHandle"), "method-findHandle");
        check(md.contains("getByteCode"), "method-getByteCode");
        check(md.contains("getEnd"), "method-getEnd");
        check(md.contains("getInstructionHandles"), "method-getInstructionHandles");
        check(md.contains("getInstructionPositions"), "method-getInstructionPositions");
        check(md.contains("getInstructions"), "method-getInstructions");
        check(md.contains("getLength"), "method-getLength");
        check(md.contains("getStart"), "method-getStart");
        check(md.contains("insert"), "method-insert");
        check(md.contains("isEmpty"), "method-isEmpty");
        check(md.contains("iterator"), "method-iterator");
        check(md.contains("move"), "method-move");
        check(md.contains("redirectBranches"), "method-redirectBranches");
        check(md.contains("redirectExceptionHandlers"), "method-redirectExceptionHandlers");
        check(md.contains("redirectLocalVariables"), "method-redirectLocalVariables");
        check(md.contains("removeObserver"), "method-removeObserver");
        check(md.contains("replaceConstantPool"), "method-replaceConstantPool");
        check(md.contains("setPositions"), "method-setPositions");
        check(md.contains("size"), "method-size");
        check(md.contains("toString"), "method-toString");
        check(md.contains("update"), "method-update");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
