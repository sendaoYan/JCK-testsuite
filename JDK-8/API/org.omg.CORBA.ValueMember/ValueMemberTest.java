/*
 * @test
 * @summary org.omg.CORBA.ValueMember conformance: value type fields and constructors.
 * @run main ValueMemberTest
 */

public class ValueMemberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ValueMember s0 = new org.omg.CORBA.ValueMember();
        check(s0.name == null, "no-arg name default");
        check(s0.id == null, "no-arg id default");
        check(s0.defined_in == null, "no-arg defined_in default");
        check(s0.version == null, "no-arg version default");
        check(s0.type == null, "no-arg type default");
        check(s0.type_def == null, "no-arg type_def default");
        check(s0.access == (short)0, "no-arg access default");
        org.omg.CORBA.ValueMember s1 = new org.omg.CORBA.ValueMember("s", "s", "s", "s", null, null, (short)7);
        check(s1.name == "s", "ctor name stored");
        check(s1.id == "s", "ctor id stored");
        check(s1.defined_in == "s", "ctor defined_in stored");
        check(s1.version == "s", "ctor version stored");
        check(s1.access == (short)7, "ctor access stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
