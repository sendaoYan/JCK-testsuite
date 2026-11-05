/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.AdapterNonExistent conformance: UserException hierarchy and constructors.
 * @run main AdapterNonExistentTest
 */

public class AdapterNonExistentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.AdapterNonExistent.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.AdapterNonExistent.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.AdapterNonExistent.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.AdapterNonExistent e = new org.omg.PortableServer.POAPackage.AdapterNonExistent();
        org.omg.PortableServer.POAPackage.AdapterNonExistent em = new org.omg.PortableServer.POAPackage.AdapterNonExistent("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): ctor matrix / toString exact / svUID / Java serialization ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableServer.POAPackage.AdapterNonExistent.class.getModifiers()), "class is final");
        check(org.omg.PortableServer.POAPackage.AdapterNonExistent.class.getDeclaredConstructors().length == 2, "exactly 2 ctors: () and (String)");
        check(new org.omg.PortableServer.POAPackage.AdapterNonExistent().toString().equals("org.omg.PortableServer.POAPackage.AdapterNonExistent: IDL:omg.org/PortableServer/POA/AdapterNonExistent:1.0"),
              "no-arg toString exact = FQCN: repoId");
        check(new org.omg.PortableServer.POAPackage.AdapterNonExistent("boom").toString().equals("org.omg.PortableServer.POAPackage.AdapterNonExistent: IDL:omg.org/PortableServer/POA/AdapterNonExistent:1.0  boom"),
              "message toString exact = FQCN: repoId + two spaces + message");
        check(new org.omg.PortableServer.POAPackage.AdapterNonExistent("boom").getMessage().equals("IDL:omg.org/PortableServer/POA/AdapterNonExistent:1.0  boom"),
              "getMessage = repoId + two spaces + message");
        check(new org.omg.PortableServer.POAPackage.AdapterNonExistent().getMessage().equals("IDL:omg.org/PortableServer/POA/AdapterNonExistent:1.0"),
              "no-arg getMessage = repoId only (no trailing spaces)");
        check(java.io.ObjectStreamClass.lookup(org.omg.PortableServer.POAPackage.AdapterNonExistent.class).getSerialVersionUID() == -9166706041988650696L,
              "serialVersionUID stable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new org.omg.PortableServer.POAPackage.AdapterNonExistent("ser"));
        }
        org.omg.PortableServer.POAPackage.AdapterNonExistent serBack = (org.omg.PortableServer.POAPackage.AdapterNonExistent) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(serBack.getClass() == org.omg.PortableServer.POAPackage.AdapterNonExistent.class, "Java serialization round-trip preserves class");
        check(serBack.getMessage().equals("IDL:omg.org/PortableServer/POA/AdapterNonExistent:1.0  ser"), "Java serialization round-trip preserves message");
        check(serBack instanceof org.omg.CORBA.portable.IDLEntity, "round-trip keeps IDLEntity");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
