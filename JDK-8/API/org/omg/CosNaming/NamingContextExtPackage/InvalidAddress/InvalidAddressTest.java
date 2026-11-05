/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtPackage.InvalidAddress conformance: UserException hierarchy and constructors.
 * @run main InvalidAddressTest
 */

public class InvalidAddressTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CosNaming.NamingContextExtPackage.InvalidAddress.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CosNaming.NamingContextExtPackage.InvalidAddress.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NamingContextExtPackage.InvalidAddress.class), "implements IDLEntity");
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddress e = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddress();
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddress em = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddress("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (batch C, jdk8u372 probed): ctor chains + toString/getMessage exact shape ----
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CosNaming.NamingContextExtPackage.InvalidAddress.class), "extends org.omg.CORBA.UserException");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NamingContextExtPackage.InvalidAddress.class.getModifiers()), "exception class final");
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddress e0 = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddress();
        check(e0.getMessage().equals("IDL:omg.org/CosNaming/NamingContextExt/InvalidAddress:1.0"), "no-arg getMessage() == repository id");
        check(e0.toString().equals("org.omg.CosNaming.NamingContextExtPackage.InvalidAddress: IDL:omg.org/CosNaming/NamingContextExt/InvalidAddress:1.0"), "no-arg toString() == 'org.omg.CosNaming.NamingContextExtPackage.InvalidAddressCN: repoId'");
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddress e1 = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddress("boom");
        check(e1.getMessage().equals("IDL:omg.org/CosNaming/NamingContextExt/InvalidAddress:1.0  boom"), "reason ctor getMessage() == repoId + two spaces + reason (idlj mapping)");
        check(e1.toString().equals("org.omg.CosNaming.NamingContextExtPackage.InvalidAddress: IDL:omg.org/CosNaming/NamingContextExt/InvalidAddress:1.0  boom"), "reason ctor toString() exact");
        check(e1.toString().contains("InvalidAddress"), "toString contains simple class name");
        check(e1.getMessage().endsWith("boom"), "reason preserved in message tail");
        e1.initCause(null);
        check(e1.getCause() == null, "cause stays null by default");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
