/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.Constants conformance: reflection.
 * @run main ConstantsTest
 */

public class ConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.Constants", false, ConstantsTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Constants"), "simple name Constants");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.InstructionConstants"), "interface com.sun.org.apache.bcel.internal.generic.InstructionConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
