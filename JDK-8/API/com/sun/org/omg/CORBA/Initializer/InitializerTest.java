/*
 * @test
 * @summary com.sun.org.omg.CORBA.Initializer conformance: behavioral construction.
 * @run main InitializerTest
 */

public class InitializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.omg.CORBA.Initializer x = new com.sun.org.omg.CORBA.Initializer();
        x.name = "nm"; x.members = new org.omg.CORBA.StructMember[0];
        check("nm".equals(x.name), "name");
        check(x.members != null && x.members.length == 0, "members");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
