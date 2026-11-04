/*
 * @test
 * @summary org.omg.CORBA.StructMember conformance: value type fields and constructors.
 * @run main StructMemberTest
 */

public class StructMemberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.StructMember s0 = new org.omg.CORBA.StructMember();
        check(s0.name == null, "no-arg name default");
        check(s0.type == null, "no-arg type default");
        check(s0.type_def == null, "no-arg type_def default");
        org.omg.CORBA.StructMember s1 = new org.omg.CORBA.StructMember("s", null, null);
        check(s1.name == "s", "ctor name stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
