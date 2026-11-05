/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.WrongPolicy conformance: UserException hierarchy and constructors.
 * @run main WrongPolicyTest
 */

public class WrongPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.WrongPolicy.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.WrongPolicy.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.WrongPolicy.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.WrongPolicy e = new org.omg.PortableServer.POAPackage.WrongPolicy();
        org.omg.PortableServer.POAPackage.WrongPolicy em = new org.omg.PortableServer.POAPackage.WrongPolicy("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): ctor matrix / toString exact / svUID / Java serialization ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableServer.POAPackage.WrongPolicy.class.getModifiers()), "class is final");
        check(org.omg.PortableServer.POAPackage.WrongPolicy.class.getDeclaredConstructors().length == 2, "exactly 2 ctors: () and (String)");
        check(new org.omg.PortableServer.POAPackage.WrongPolicy().toString().equals("org.omg.PortableServer.POAPackage.WrongPolicy: IDL:omg.org/PortableServer/POA/WrongPolicy:1.0"),
              "no-arg toString exact = FQCN: repoId");
        check(new org.omg.PortableServer.POAPackage.WrongPolicy("boom").toString().equals("org.omg.PortableServer.POAPackage.WrongPolicy: IDL:omg.org/PortableServer/POA/WrongPolicy:1.0  boom"),
              "message toString exact = FQCN: repoId + two spaces + message");
        check(new org.omg.PortableServer.POAPackage.WrongPolicy("boom").getMessage().equals("IDL:omg.org/PortableServer/POA/WrongPolicy:1.0  boom"),
              "getMessage = repoId + two spaces + message");
        check(new org.omg.PortableServer.POAPackage.WrongPolicy().getMessage().equals("IDL:omg.org/PortableServer/POA/WrongPolicy:1.0"),
              "no-arg getMessage = repoId only (no trailing spaces)");
        check(java.io.ObjectStreamClass.lookup(org.omg.PortableServer.POAPackage.WrongPolicy.class).getSerialVersionUID() == 1949765652955335195L,
              "serialVersionUID stable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new org.omg.PortableServer.POAPackage.WrongPolicy("ser"));
        }
        org.omg.PortableServer.POAPackage.WrongPolicy serBack = (org.omg.PortableServer.POAPackage.WrongPolicy) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(serBack.getClass() == org.omg.PortableServer.POAPackage.WrongPolicy.class, "Java serialization round-trip preserves class");
        check(serBack.getMessage().equals("IDL:omg.org/PortableServer/POA/WrongPolicy:1.0  ser"), "Java serialization round-trip preserves message");
        check(serBack instanceof org.omg.CORBA.portable.IDLEntity, "round-trip keeps IDLEntity");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
