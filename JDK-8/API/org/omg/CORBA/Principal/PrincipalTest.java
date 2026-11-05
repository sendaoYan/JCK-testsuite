/*
 * @test
 * @summary org.omg.CORBA.Principal conformance: concrete class behavior.
 * @run main PrincipalTest
 */

public class PrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.Principal p = new org.omg.CORBA.Principal();
        check(p != null, "constructs");
        try {
            p.name();
            throw new AssertionError("name() should throw NO_IMPLEMENT");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            passed++;
        }
        try {
            p.name(new byte[]{1});
            throw new AssertionError("name(byte[]) should throw NO_IMPLEMENT");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            passed++;
        }

        // ---- deepened: shape of the deprecated no-op class ----
        check(org.omg.CORBA.Principal.class.isAnnotationPresent(Deprecated.class), "Principal is @Deprecated");
        check(!java.lang.reflect.Modifier.isAbstract(org.omg.CORBA.Principal.class.getModifiers()),
              "concrete class (kept for compilation only)");
        check(org.omg.CORBA.TCKind.tk_Principal.value() == 13, "TCKind.tk_Principal == 13 still reserved");
        check(org.omg.CORBA.Any.class.getDeclaredMethods().length > 0
              && java.util.Arrays.stream(org.omg.CORBA.Any.class.getMethods())
                   .anyMatch(m -> m.getName().equals("insert_Principal")),
              "Any still exposes insert_Principal/extract_Principal");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
