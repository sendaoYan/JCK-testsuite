/*
 * @test
 * @summary org.omg.PortableServer.POAManagerPackage.AdapterInactive conformance: UserException hierarchy and constructors.
 * @run main AdapterInactiveTest
 */

public class AdapterInactiveTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAManagerPackage.AdapterInactive.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAManagerPackage.AdapterInactive.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAManagerPackage.AdapterInactive.class), "implements IDLEntity");
        org.omg.PortableServer.POAManagerPackage.AdapterInactive e = new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
        org.omg.PortableServer.POAManagerPackage.AdapterInactive em = new org.omg.PortableServer.POAManagerPackage.AdapterInactive("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): ctor matrix / toString exact / svUID / Java serialization ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableServer.POAManagerPackage.AdapterInactive.class.getModifiers()), "class is final");
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactive.class.getDeclaredConstructors().length == 2, "exactly 2 ctors: () and (String)");
        check(new org.omg.PortableServer.POAManagerPackage.AdapterInactive().toString().equals("org.omg.PortableServer.POAManagerPackage.AdapterInactive: IDL:omg.org/PortableServer/POAManager/AdapterInactive:1.0"),
              "no-arg toString exact = FQCN: repoId");
        check(new org.omg.PortableServer.POAManagerPackage.AdapterInactive("boom").toString().equals("org.omg.PortableServer.POAManagerPackage.AdapterInactive: IDL:omg.org/PortableServer/POAManager/AdapterInactive:1.0  boom"),
              "message toString exact = FQCN: repoId + two spaces + message");
        check(new org.omg.PortableServer.POAManagerPackage.AdapterInactive("boom").getMessage().equals("IDL:omg.org/PortableServer/POAManager/AdapterInactive:1.0  boom"),
              "getMessage = repoId + two spaces + message");
        check(new org.omg.PortableServer.POAManagerPackage.AdapterInactive().getMessage().equals("IDL:omg.org/PortableServer/POAManager/AdapterInactive:1.0"),
              "no-arg getMessage = repoId only (no trailing spaces)");
        check(java.io.ObjectStreamClass.lookup(org.omg.PortableServer.POAManagerPackage.AdapterInactive.class).getSerialVersionUID() == 6945182851085567131L,
              "serialVersionUID stable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new org.omg.PortableServer.POAManagerPackage.AdapterInactive("ser"));
        }
        org.omg.PortableServer.POAManagerPackage.AdapterInactive serBack = (org.omg.PortableServer.POAManagerPackage.AdapterInactive) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(serBack.getClass() == org.omg.PortableServer.POAManagerPackage.AdapterInactive.class, "Java serialization round-trip preserves class");
        check(serBack.getMessage().equals("IDL:omg.org/PortableServer/POAManager/AdapterInactive:1.0  ser"), "Java serialization round-trip preserves message");
        check(serBack instanceof org.omg.CORBA.portable.IDLEntity, "round-trip keeps IDLEntity");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
