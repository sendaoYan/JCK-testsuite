/*
 * @test
 * @summary org.omg.PortableServer.IdUniquenessPolicyValue conformance: enumeration values.
 * @run main IdUniquenessPolicyValueTest
 */

public class IdUniquenessPolicyValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID != null, "UNIQUE_ID constant non-null");
        check(org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID.value() == org.omg.PortableServer.IdUniquenessPolicyValue._UNIQUE_ID, "value() matches int constant");
        check(org.omg.PortableServer.IdUniquenessPolicyValue.from_int(org.omg.PortableServer.IdUniquenessPolicyValue._UNIQUE_ID) == org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID, "from_int round-trips");
        // ---- deepened (jdk8u372-b07 probed): full numeric table / from_int normalization /
        // BAD_PARAM negatives / IDL-to-Java mapping shape (no values(), no name toString) ----
        final org.omg.PortableServer.IdUniquenessPolicyValue[] ALL = {
            org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID,
            org.omg.PortableServer.IdUniquenessPolicyValue.MULTIPLE_ID};
        final int[] IV = {
            org.omg.PortableServer.IdUniquenessPolicyValue._UNIQUE_ID,
            org.omg.PortableServer.IdUniquenessPolicyValue._MULTIPLE_ID};
        check(ALL.length == 2 && IV.length == 2, "family size 2 (IDL enum literals)");
        check(IV[0] == 0 && IV[0] == 0 && IV[1] == 1, "exact numeric table UNIQUE_ID=0, MULTIPLE_ID=1");
        check(ALL[1] != null, "MULTIPLE_ID constant non-null");
        check(ALL[1].value() == IV[1], "MULTIPLE_ID.value() == _MULTIPLE_ID == 1");
        for (int i = 0; i < ALL.length; i++) {
            for (int j = 0; j < ALL.length; j++) {
                check((i == j) == (ALL[i] == ALL[j]), "singleton identity ALL[" + i + "]==ALL[" + j + "] iff i==j");
            }
        }
        for (int i = 0; i < ALL.length; i++) {
            check(ALL[i].value() == IV[i], "value()[" + i + "] == " + IV[i]);
            check(org.omg.PortableServer.IdUniquenessPolicyValue.from_int(IV[i]) == ALL[i], "from_int(" + IV[i] + ") normalizes to canonical singleton");
            check(ALL[i] instanceof org.omg.CORBA.portable.IDLEntity, "ALL[" + i + "] implements IDLEntity");
            check(ALL[i].toString().startsWith("org.omg.PortableServer.IdUniquenessPolicyValue@"),
                  "JDK-8 mapping keeps default Object.toString (no constant-name override) " + i);
        }
        for (int bad : new int[]{2, 3, 7, -1, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            try {
                org.omg.PortableServer.IdUniquenessPolicyValue.from_int(bad);
                throw new AssertionError("from_int(" + bad + ") must throw BAD_PARAM");
            } catch (org.omg.CORBA.BAD_PARAM expected) {
                check(expected.minor == 0, "from_int(" + bad + ") -> BAD_PARAM minor==0");
                check(expected.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO,
                      "from_int(" + bad + ") -> BAD_PARAM completed==COMPLETED_NO");
            }
        }
        Class<?> ec = org.omg.PortableServer.IdUniquenessPolicyValue.class;
        check(!java.lang.reflect.Modifier.isFinal(ec.getModifiers()), "class is not final (IDL mapping shape)");
        check(ec.getDeclaredConstructors().length == 1
           && java.lang.reflect.Modifier.isProtected(ec.getDeclaredConstructors()[0].getModifiers()),
              "single protected (int) ctor: not directly instantiable");
        check(ec.getDeclaredMethod("value").getReturnType() == int.class, "declares value() returning int");
        check(ec.getDeclaredMethod("from_int", int.class).getReturnType() == ec,
              "declares static from_int(int) returning self type");
        check(ec.getMethod("toString").getDeclaringClass() == java.lang.Object.class,
              "toString is not overridden: inherits java.lang.Object default");
        try {
            Class.forName("org.omg.PortableServer.IdUniquenessPolicyValueHelper");
            check(false, "unexpected: Helper class exists for this enum");
        } catch (ClassNotFoundException expected) {
            check(true, "JDK-8 mapping: no Helper class generated for this enum");
        }
        try {
            Class.forName("org.omg.PortableServer.IdUniquenessPolicyValueHolder");
            check(false, "unexpected: Holder class exists for this enum");
        } catch (ClassNotFoundException expected) {
            check(true, "JDK-8 mapping: no Holder class generated for this enum");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
