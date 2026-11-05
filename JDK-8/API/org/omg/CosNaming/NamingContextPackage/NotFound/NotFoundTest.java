/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.NotFound conformance: UserException hierarchy and constructors.
 * @run main NotFoundTest
 */

public class NotFoundTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.NotFound.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.NotFound.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.NotFound.class), "implements IDLEntity");
        org.omg.CosNaming.NamingContextPackage.NotFound e = new org.omg.CosNaming.NamingContextPackage.NotFound();
        check(e.why == null, "why default null");
        check(e.rest_of_name == null, "rest_of_name default null");
        org.omg.CosNaming.NamingContextPackage.NotFound em = new org.omg.CosNaming.NamingContextPackage.NotFound("msg", null, null);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (batch C, jdk8u372 probed): ctor chains + toString/getMessage exact shape ----
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.NotFound.class), "extends org.omg.CORBA.UserException");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NamingContextPackage.NotFound.class.getModifiers()), "exception class final");
        org.omg.CosNaming.NamingContextPackage.NotFound e0 = new org.omg.CosNaming.NamingContextPackage.NotFound();
        check(e0.getMessage().equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"), "no-arg getMessage() == repository id");
        check(e0.toString().equals("org.omg.CosNaming.NamingContextPackage.NotFound: IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"), "no-arg toString() == 'org.omg.CosNaming.NamingContextPackage.NotFoundCN: repoId'");
        org.omg.CosNaming.NamingContextPackage.NotFound e1 = new org.omg.CosNaming.NamingContextPackage.NotFound("boom", org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node, new org.omg.CosNaming.NameComponent[0]);
        check(e1.getMessage().equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0  boom"), "reason ctor getMessage() == repoId + two spaces + reason (idlj mapping)");
        check(e1.toString().equals("org.omg.CosNaming.NamingContextPackage.NotFound: IDL:omg.org/CosNaming/NamingContext/NotFound:1.0  boom"), "reason ctor toString() exact");
        check(e1.toString().contains("NotFound"), "toString contains simple class name");
        check(e1.getMessage().endsWith("boom"), "reason preserved in message tail");
        e1.initCause(null);
        check(e1.getCause() == null, "cause stays null by default");
        org.omg.CosNaming.NameComponent[] rest = new org.omg.CosNaming.NameComponent[]{
            new org.omg.CosNaming.NameComponent("r1", "k1"), new org.omg.CosNaming.NameComponent("r2", "k2")};
        org.omg.CosNaming.NamingContextPackage.NotFound f1 =
            new org.omg.CosNaming.NamingContextPackage.NotFound(org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_context, rest);
        check(f1.why == org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_context, "fielded ctor stores why constant");
        check(f1.rest_of_name == rest, "fielded ctor stores rest_of_name array reference verbatim");
        check(f1.rest_of_name.length == 2 && f1.rest_of_name[0].id.equals("r1"), "rest_of_name content readback");
        check(f1.getMessage().equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"),
              "fielded ctor (no reason String) message is bare repository id");
        org.omg.CosNaming.NamingContextPackage.NotFound f2 =
            new org.omg.CosNaming.NamingContextPackage.NotFound("boom", org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node, rest);
        check(f2.why == org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node && f2.rest_of_name.length == 2,
              "full ctor stores both fields");
        check(f2.getMessage().endsWith("boom"), "full ctor keeps reason text");
        org.omg.CosNaming.NamingContextPackage.NotFound f3 = new org.omg.CosNaming.NamingContextPackage.NotFound();
        check(f3.why == null && f3.rest_of_name == null, "no-arg ctor leaves fields null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
