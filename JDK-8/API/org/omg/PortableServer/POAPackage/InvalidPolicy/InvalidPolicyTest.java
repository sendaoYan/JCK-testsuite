/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.InvalidPolicy conformance: UserException hierarchy and constructors.
 * @run main InvalidPolicyTest
 */

public class InvalidPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.InvalidPolicy.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.InvalidPolicy.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.InvalidPolicy.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.InvalidPolicy e = new org.omg.PortableServer.POAPackage.InvalidPolicy();
        check(e.index == (short)0, "index default (short)0");
        org.omg.PortableServer.POAPackage.InvalidPolicy em = new org.omg.PortableServer.POAPackage.InvalidPolicy("msg", (short)0);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): 3-ctor matrix / index field / toString / svUID / serialization ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableServer.POAPackage.InvalidPolicy.class.getModifiers()), "class is final");
        check(org.omg.PortableServer.POAPackage.InvalidPolicy.class.getDeclaredConstructors().length == 3,
              "exactly 3 ctors: ()/(short)/(String,short)");
        check(new org.omg.PortableServer.POAPackage.InvalidPolicy().index == 0, "no-arg index == 0");
        check(new org.omg.PortableServer.POAPackage.InvalidPolicy((short) 3).index == 3, "(short) ctor stores index");
        check(new org.omg.PortableServer.POAPackage.InvalidPolicy((short) 3).getMessage().equals(
                  "IDL:omg.org/PortableServer/POA/InvalidPolicy:1.0"),
              "(short) ctor getMessage = repoId only");
        check(new org.omg.PortableServer.POAPackage.InvalidPolicy("m", (short) 7).index == 7, "(String,short) ctor stores index");
        check(new org.omg.PortableServer.POAPackage.InvalidPolicy("m", (short) 7).getMessage().equals(
                  "IDL:omg.org/PortableServer/POA/InvalidPolicy:1.0  m"),
              "(String,short) getMessage = repoId + two spaces + message");
        check(new org.omg.PortableServer.POAPackage.InvalidPolicy((short) -1).index == -1, "negative short index stored verbatim");
        check(new org.omg.PortableServer.POAPackage.InvalidPolicy((short) Short.MAX_VALUE).index == Short.MAX_VALUE,
              "Short.MAX_VALUE index stored");
        check(new org.omg.PortableServer.POAPackage.InvalidPolicy((short) Short.MIN_VALUE).index == Short.MIN_VALUE,
              "Short.MIN_VALUE index stored");
        check(new org.omg.PortableServer.POAPackage.InvalidPolicy().toString().equals(
                  "org.omg.PortableServer.POAPackage.InvalidPolicy: IDL:omg.org/PortableServer/POA/InvalidPolicy:1.0"),
              "no-arg toString exact");
        check(new org.omg.PortableServer.POAPackage.InvalidPolicy("m", (short) 1).toString().equals(
                  "org.omg.PortableServer.POAPackage.InvalidPolicy: IDL:omg.org/PortableServer/POA/InvalidPolicy:1.0  m"),
              "message toString exact");
        org.omg.PortableServer.POAPackage.InvalidPolicy idx = new org.omg.PortableServer.POAPackage.InvalidPolicy();
        idx.index = (short) 42;
        check(idx.index == 42, "index field is public mutable");
        check(java.io.ObjectStreamClass.lookup(org.omg.PortableServer.POAPackage.InvalidPolicy.class).getSerialVersionUID()
                  == 3204212102282117205L, "serialVersionUID stable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new org.omg.PortableServer.POAPackage.InvalidPolicy("ser", (short) 5));
        }
        org.omg.PortableServer.POAPackage.InvalidPolicy serBack = (org.omg.PortableServer.POAPackage.InvalidPolicy)
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(serBack.getClass() == org.omg.PortableServer.POAPackage.InvalidPolicy.class, "serialization round-trip preserves class");
        check(serBack.index == 5 && serBack.getMessage().equals("IDL:omg.org/PortableServer/POA/InvalidPolicy:1.0  ser"),
              "serialization round-trip keeps index and message");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
