/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.CannotProceed conformance: UserException hierarchy and constructors.
 * @run main CannotProceedTest
 */

public class CannotProceedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.CannotProceed.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.CannotProceed.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.CannotProceed.class), "implements IDLEntity");
        org.omg.CosNaming.NamingContextPackage.CannotProceed e = new org.omg.CosNaming.NamingContextPackage.CannotProceed();
        check(e.cxt == null, "cxt default null");
        check(e.rest_of_name == null, "rest_of_name default null");
        org.omg.CosNaming.NamingContextPackage.CannotProceed em = new org.omg.CosNaming.NamingContextPackage.CannotProceed("msg", null, null);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (batch C, jdk8u372 probed): ctor chains + toString/getMessage exact shape ----
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.CannotProceed.class), "extends org.omg.CORBA.UserException");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NamingContextPackage.CannotProceed.class.getModifiers()), "exception class final");
        org.omg.CosNaming.NamingContextPackage.CannotProceed e0 = new org.omg.CosNaming.NamingContextPackage.CannotProceed();
        check(e0.getMessage().equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"), "no-arg getMessage() == repository id");
        check(e0.toString().equals("org.omg.CosNaming.NamingContextPackage.CannotProceed: IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"), "no-arg toString() == 'org.omg.CosNaming.NamingContextPackage.CannotProceedCN: repoId'");
        org.omg.CosNaming.NamingContextPackage.CannotProceed e1 = new org.omg.CosNaming.NamingContextPackage.CannotProceed("boom", null, new org.omg.CosNaming.NameComponent[0]);
        check(e1.getMessage().equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0  boom"), "reason ctor getMessage() == repoId + two spaces + reason (idlj mapping)");
        check(e1.toString().equals("org.omg.CosNaming.NamingContextPackage.CannotProceed: IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0  boom"), "reason ctor toString() exact");
        check(e1.toString().contains("CannotProceed"), "toString contains simple class name");
        check(e1.getMessage().endsWith("boom"), "reason preserved in message tail");
        e1.initCause(null);
        check(e1.getCause() == null, "cause stays null by default");
        org.omg.CosNaming.NameComponent[] rest = new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("r", "k")};
        org.omg.CosNaming.NamingContextPackage.CannotProceed c1 = new org.omg.CosNaming.NamingContextPackage.CannotProceed(null, rest);
        check(c1.cxt == null, "fielded ctor stores null NamingContext cxt verbatim");
        check(c1.rest_of_name == rest && c1.rest_of_name.length == 1, "rest_of_name stored verbatim");
        org.omg.CosNaming.NamingContextPackage.CannotProceed c2 = new org.omg.CosNaming.NamingContextPackage.CannotProceed("why", null, rest);
        check(c2.cxt == null && c2.rest_of_name.length == 1, "full ctor field readback");
        check(c2.getMessage().endsWith("why"), "full ctor keeps reason text");
        org.omg.CosNaming.NamingContextPackage.CannotProceed c3 = new org.omg.CosNaming.NamingContextPackage.CannotProceed();
        check(c3.cxt == null && c3.rest_of_name == null, "no-arg ctor fields null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
