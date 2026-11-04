/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serialize.IndentPrinter conformance: class hierarchy and members.
 * @run main IndentPrinterTest
 */
public class IndentPrinterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serialize.IndentPrinter", false, IndentPrinterTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serialize.Printer"), "superclass com.sun.org.apache.xml.internal.serialize.Printer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("enterDTD"), "method enterDTD");
        check(mn.contains("leaveDTD"), "method leaveDTD");
        check(mn.contains("printText"), "method printText");
        check(mn.contains("printSpace"), "method printSpace");
        check(mn.contains("breakLine"), "method breakLine");
        check(mn.contains("flushLine"), "method flushLine");
        check(mn.contains("flush"), "method flush");
        check(mn.contains("indent"), "method indent");
        check(mn.contains("unindent"), "method unindent");
        check(mn.contains("getNextIndent"), "method getNextIndent");
        check(mn.contains("setNextIndent"), "method setNextIndent");
        check(mn.contains("setThisIndent"), "method setThisIndent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
