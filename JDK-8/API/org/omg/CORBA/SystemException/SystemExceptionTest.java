/*
 * @test
 * @summary org.omg.CORBA.SystemException conformance: abstract class shape.
 * @run main SystemExceptionTest
 */

public class SystemExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final String[] FAMILY = {
        "ACTIVITY_COMPLETED", "ACTIVITY_REQUIRED", "BAD_CONTEXT", "BAD_INV_ORDER", "BAD_OPERATION",
        "BAD_PARAM", "BAD_QOS", "BAD_TYPECODE", "CODESET_INCOMPATIBLE", "COMM_FAILURE", "DATA_CONVERSION",
        "FREE_MEM", "IMP_LIMIT", "INITIALIZE", "INTERNAL", "INTF_REPOS", "INVALID_ACTIVITY",
        "INVALID_TRANSACTION", "INV_FLAG", "INV_IDENT", "INV_OBJREF", "INV_POLICY", "MARSHAL",
        "NO_IMPLEMENT", "NO_MEMORY", "NO_PERMISSION", "NO_RESOURCES", "NO_RESPONSE", "OBJ_ADAPTER",
        "OBJECT_NOT_EXIST", "PERSIST_STORE", "REBIND", "TIMEOUT", "TRANSACTION_MODE",
        "TRANSACTION_REQUIRED", "TRANSACTION_ROLLEDBACK", "TRANSACTION_UNAVAILABLE", "TRANSIENT",
        "UNKNOWN"
    };

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.SystemException");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(java.lang.RuntimeException.class.isAssignableFrom(c), "extends java.lang.RuntimeException");

        // public final fields minor/completed on the base class
        check(java.lang.reflect.Modifier.isPublic(
                org.omg.CORBA.SystemException.class.getField("minor").getModifiers()), "minor is public");
        check(org.omg.CORBA.SystemException.class.getField("minor").getType() == int.class, "minor is int");
        check(org.omg.CORBA.SystemException.class.getField("completed").getType()
                == org.omg.CORBA.CompletionStatus.class, "completed is CompletionStatus");

        // ---- batch contract over the whole standard-exception family (39 classes) ----
        for (String name : FAMILY) {
            Class<?> k = Class.forName("org.omg.CORBA." + name);
            check(org.omg.CORBA.SystemException.class.isAssignableFrom(k), name + " extends SystemException");
            check(java.lang.RuntimeException.class.isAssignableFrom(k), name + " is unchecked");
            check(!java.lang.reflect.Modifier.isAbstract(k.getModifiers()), name + " is concrete");
            org.omg.CORBA.SystemException e =
                (org.omg.CORBA.SystemException) k.getDeclaredConstructor().newInstance();
            check(e.minor == 0, name + "() default minor == 0");
            check(e.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO,
                  name + "() default completed == COMPLETED_NO (jdk8u372)");
            String ts = e.toString();
            check(ts.startsWith("org.omg.CORBA." + name + ": "), name + " toString starts with class prefix");
            check(ts.contains("minor code: 0"), name + " toString contains minor code: 0");
            check(ts.contains("completed: No"), name + " toString contains completed: No");
            check("".equals(e.getMessage()),
                  name + "() default message is the empty string (delegates to this(\"\"))");

            org.omg.CORBA.SystemException es =
                (org.omg.CORBA.SystemException) k.getDeclaredConstructor(String.class).newInstance("boom");
            check("boom".equals(es.getMessage()), name + "(String) message readback");
            check(es.minor == 0, name + "(String) minor stays 0");
            check(es.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO,
                  name + "(String) completed stays COMPLETED_NO");

            org.omg.CORBA.SystemException ef =
                (org.omg.CORBA.SystemException) k.getDeclaredConstructor(String.class, int.class,
                        org.omg.CORBA.CompletionStatus.class)
                    .newInstance("m", 12345, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
            check(ef.minor == 12345, name + "(String,int,CompletionStatus) minor readback");
            check(ef.completed == org.omg.CORBA.CompletionStatus.COMPLETED_YES, name + " full ctor completed readback");
            // jdk8u372: toString splits the minor into vmcid = minor & 0xFFFFF000 and a
            // 12-bit minor code, so 12345 (0x3039) renders as "vmcid: 0x3000  minor code: 57"
            check(ef.toString().contains("vmcid: 0x3000"), name + " full ctor toString vmcid 0x3000");
            check(ef.toString().contains("minor code: 57"), name + " full ctor toString minor code 57");
            check(ef.toString().contains("completed: Yes"), name + " full ctor toString completed Yes");
        }
        check(FAMILY.length == 39, "family table has 39 entries");

        // ---- OMGVMCID-tagged minors render as "vmcid: OMG" ----
        org.omg.CORBA.BAD_PARAM bp =
            new org.omg.CORBA.BAD_PARAM("x", org.omg.CORBA.OMGVMCID.value | 7,
                                        org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        check(bp.toString().contains("vmcid: OMG"), "OMGVMCID minor renders as 'vmcid: OMG'");
        check(bp.toString().contains("minor code: 7"), "vmcid low bits render as minor code: 7");
        org.omg.CORBA.MARSHAL mz = new org.omg.CORBA.MARSHAL(); // vmcid 0 renders hex
        check(mz.toString().contains("vmcid: 0x0"), "zero vmcid renders as 'vmcid: 0x0'");

        // ---- base-class constructors via a concrete subclass ----
        org.omg.CORBA.UNKNOWN u0 = new org.omg.CORBA.UNKNOWN();
        check(u0.minor == 0 && u0.completed == org.omg.CORBA.CompletionStatus.COMPLETED_NO,
              "UNKNOWN defaults via base ctor");
        org.omg.CORBA.UNKNOWN u2 = new org.omg.CORBA.UNKNOWN(3, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        check(u2.minor == 3 && u2.completed == org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE,
              "UNKNOWN (int,CompletionStatus) ctor readback");
        check("".equals(u2.getMessage()), "minor/completed ctor message is the empty string");

        // toString DOES embed a non-empty message (jdk8u372: "name: msg  vmcid: ...  minor code: ...")
        org.omg.CORBA.INTERNAL im = new org.omg.CORBA.INTERNAL("secret");
        check(im.toString().contains("secret"), "toString embeds the message text when non-empty");
        check(new org.omg.CORBA.INTERNAL().toString().startsWith("org.omg.CORBA.INTERNAL: "),
              "empty message still keeps the 'name: ' prefix");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
