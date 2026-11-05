/*
 * @test
 * @summary org.omg.CosNaming.BindingType conformance: enumeration values.
 * @run main BindingTypeTest
 */

public class BindingTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.BindingType.nobject != null, "nobject constant non-null");
        check(org.omg.CosNaming.BindingType.nobject.value() == org.omg.CosNaming.BindingType._nobject, "value() matches int constant");
        check(org.omg.CosNaming.BindingType.from_int(org.omg.CosNaming.BindingType._nobject) == org.omg.CosNaming.BindingType.nobject, "from_int round-trips");
        // ---- deepened (batch C, jdk8u372 probed): enum value table + from_int matrix ----
        check(org.omg.CosNaming.BindingType._nobject == 0 && org.omg.CosNaming.BindingType._ncontext == 1,
              "int constants _nobject==0, _ncontext==1");
        check(org.omg.CosNaming.BindingType.nobject.value() == 0, "nobject.value()==0");
        check(org.omg.CosNaming.BindingType.ncontext.value() == 1, "ncontext.value()==1");
        check(org.omg.CosNaming.BindingType.nobject != org.omg.CosNaming.BindingType.ncontext, "two distinct constants");
        check(org.omg.CosNaming.BindingType.from_int(0) == org.omg.CosNaming.BindingType.nobject, "from_int(0) normalizes to nobject");
        check(org.omg.CosNaming.BindingType.from_int(org.omg.CosNaming.BindingType._ncontext) == org.omg.CosNaming.BindingType.ncontext,
              "from_int(_ncontext) normalizes to ncontext");
        try {
            org.omg.CosNaming.BindingType.from_int(5);
            throw new AssertionError("from_int(5) must throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(5) -> org.omg.CORBA.BAD_PARAM");
        }
        try {
            org.omg.CosNaming.BindingType.from_int(-1);
            throw new AssertionError("from_int(-1) must throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(-1) -> org.omg.CORBA.BAD_PARAM");
        }
        check(org.omg.CosNaming.BindingType.nobject.toString().startsWith("org.omg.CosNaming.BindingType@"),
              "toString not overridden (default Object pattern)");
        check(!java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.BindingType.class.getModifiers()),
              "BindingType class is public non-final (idlj mapping)");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.BindingType.class),
              "BindingType implements IDLEntity");
        check(org.omg.CosNaming.BindingType.nobject.value() == org.omg.CosNaming.BindingType.from_int(0).value(),
              "value() stable across from_int normalization");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
