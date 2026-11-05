/*
 * @test
 * @summary org.omg.CORBA.NameValuePair conformance: value type fields and constructors.
 * @run main NameValuePairTest
 */

public class NameValuePairTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.NameValuePair s0 = new org.omg.CORBA.NameValuePair();
        check(s0.id == null, "no-arg id default");
        check(s0.value == null, "no-arg value default");
        org.omg.CORBA.NameValuePair s1 = new org.omg.CORBA.NameValuePair("s", null);
        check(s1.id == "s", "ctor id stored");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.NameValuePair().id == null && new org.omg.CORBA.NameValuePair().value == null,
              "default ctor leaves both fields null");
        org.omg.CORBA.Any anyNv = orb.create_any();
        anyNv.insert_long(11);
        org.omg.CORBA.NameValuePair nv = new org.omg.CORBA.NameValuePair("k", anyNv);
        check("k".equals(nv.id), "full ctor stores id");
        check(nv.value == anyNv, "full ctor stores same Any reference");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.NameValuePair.class),
              "NameValuePair implements IDLEntity");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.NameValuePair.class.getModifiers()),
              "NameValuePair class is final");
        check(nv.equals(nv), "equals is identity semantics: self true");
        check(!nv.equals(new org.omg.CORBA.NameValuePair("k", anyNv)),
              "equals is identity semantics: same-args new instance false (no value equals)");
        check(!nv.equals(null), "equals(null) false");
        nv.id = "k2";
        nv.value = null;
        check("k2".equals(nv.id) && nv.value == null, "fields are mutable and read back");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
