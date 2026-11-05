/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.NoServant conformance: UserException hierarchy and constructors.
 * @run main NoServantTest
 */

public class NoServantTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.NoServant.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.NoServant.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.NoServant.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.NoServant e = new org.omg.PortableServer.POAPackage.NoServant();
        org.omg.PortableServer.POAPackage.NoServant em = new org.omg.PortableServer.POAPackage.NoServant("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): ctor matrix / toString exact / svUID / Java serialization ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableServer.POAPackage.NoServant.class.getModifiers()), "class is final");
        check(org.omg.PortableServer.POAPackage.NoServant.class.getDeclaredConstructors().length == 2, "exactly 2 ctors: () and (String)");
        check(new org.omg.PortableServer.POAPackage.NoServant().toString().equals("org.omg.PortableServer.POAPackage.NoServant: IDL:omg.org/PortableServer/POA/NoServant:1.0"),
              "no-arg toString exact = FQCN: repoId");
        check(new org.omg.PortableServer.POAPackage.NoServant("boom").toString().equals("org.omg.PortableServer.POAPackage.NoServant: IDL:omg.org/PortableServer/POA/NoServant:1.0  boom"),
              "message toString exact = FQCN: repoId + two spaces + message");
        check(new org.omg.PortableServer.POAPackage.NoServant("boom").getMessage().equals("IDL:omg.org/PortableServer/POA/NoServant:1.0  boom"),
              "getMessage = repoId + two spaces + message");
        check(new org.omg.PortableServer.POAPackage.NoServant().getMessage().equals("IDL:omg.org/PortableServer/POA/NoServant:1.0"),
              "no-arg getMessage = repoId only (no trailing spaces)");
        check(java.io.ObjectStreamClass.lookup(org.omg.PortableServer.POAPackage.NoServant.class).getSerialVersionUID() == -5893773687270266061L,
              "serialVersionUID stable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new org.omg.PortableServer.POAPackage.NoServant("ser"));
        }
        org.omg.PortableServer.POAPackage.NoServant serBack = (org.omg.PortableServer.POAPackage.NoServant) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(serBack.getClass() == org.omg.PortableServer.POAPackage.NoServant.class, "Java serialization round-trip preserves class");
        check(serBack.getMessage().equals("IDL:omg.org/PortableServer/POA/NoServant:1.0  ser"), "Java serialization round-trip preserves message");
        check(serBack instanceof org.omg.CORBA.portable.IDLEntity, "round-trip keeps IDLEntity");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
