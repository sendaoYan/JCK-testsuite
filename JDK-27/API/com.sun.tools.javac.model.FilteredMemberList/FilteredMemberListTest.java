/*
 * @test
 * @summary com.sun.tools.javac.model.FilteredMemberList conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.model:+open
 * @run main FilteredMemberListTest
 */
public class FilteredMemberListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.model.FilteredMemberList", false, FilteredMemberListTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.model.FilteredMemberList"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractList"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
