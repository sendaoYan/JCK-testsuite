/*
 * @test
 * @summary org.omg.CORBA.UnionMember conformance: value type fields and constructors.
 * @run main UnionMemberTest
 */

public class UnionMemberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.UnionMember s0 = new org.omg.CORBA.UnionMember();
        check(s0.name == null, "no-arg name default");
        check(s0.label == null, "no-arg label default");
        check(s0.type == null, "no-arg type default");
        check(s0.type_def == null, "no-arg type_def default");
        org.omg.CORBA.UnionMember s1 = new org.omg.CORBA.UnionMember("s", null, null, null);
        check(s1.name == "s", "ctor name stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
