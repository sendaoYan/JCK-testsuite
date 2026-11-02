/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.InstructionHandle conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main InstructionHandleTest
 */
public class InstructionHandleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.InstructionHandle", false, InstructionHandleTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.InstructionHandle"), "name");
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
        check(fd.contains("EMPTY_ARRAY"), "field-EMPTY_ARRAY");
        check(fd.contains("i_position"), "field-i_position");
        check(md.size() == 19, "method-count");
        check(md.contains("accept"), "method-accept");
        check(md.contains("addAttribute"), "method-addAttribute");
        check(md.contains("addHandle"), "method-addHandle");
        check(md.contains("addTargeter"), "method-addTargeter");
        check(md.contains("getAttribute"), "method-getAttribute");
        check(md.contains("getAttributes"), "method-getAttributes");
        check(md.contains("getInstruction"), "method-getInstruction");
        check(md.contains("getNext"), "method-getNext");
        check(md.contains("getPosition"), "method-getPosition");
        check(md.contains("getPrev"), "method-getPrev");
        check(md.contains("getTargeters"), "method-getTargeters");
        check(md.contains("hasTargeters"), "method-hasTargeters");
        check(md.contains("removeAllTargeters"), "method-removeAllTargeters");
        check(md.contains("removeAttribute"), "method-removeAttribute");
        check(md.contains("removeTargeter"), "method-removeTargeter");
        check(md.contains("setInstruction"), "method-setInstruction");
        check(md.contains("swapInstruction"), "method-swapInstruction");
        check(md.contains("toString"), "method-toString");
        check(md.contains("updatePosition"), "method-updatePosition");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
