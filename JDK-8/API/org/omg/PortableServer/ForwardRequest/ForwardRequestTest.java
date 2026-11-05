/*
 * @test
 * @summary org.omg.PortableServer.ForwardRequest conformance: UserException hierarchy and constructors.
 * @run main ForwardRequestTest
 */

public class ForwardRequestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.ForwardRequest.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.ForwardRequest.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.ForwardRequest.class), "implements IDLEntity");
        org.omg.PortableServer.ForwardRequest e = new org.omg.PortableServer.ForwardRequest();
        check(e.forward_reference == null, "forward_reference default null");
        org.omg.PortableServer.ForwardRequest em = new org.omg.PortableServer.ForwardRequest("msg", null);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): 3-ctor matrix / forward_reference field / toString / svUID / serialization ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableServer.ForwardRequest.class.getModifiers()), "class is final");
        check(org.omg.PortableServer.ForwardRequest.class.getDeclaredConstructors().length == 3,
              "exactly 3 ctors: ()/(Object)/(String,Object)");
        final org.omg.CORBA.Object fwd = new org.omg.CORBA.portable.ObjectImpl() {
            public String[] _ids() { return new String[]{"IDL:fake:1.0"}; }
        };
        check(new org.omg.PortableServer.ForwardRequest(fwd).forward_reference == fwd,
              "(Object) ctor stores forward_reference by reference");
        check(new org.omg.PortableServer.ForwardRequest(fwd).getMessage().equals(
                  "IDL:omg.org/PortableServer/ForwardRequest:1.0"),
              "(Object) ctor getMessage = repoId only");
        check(new org.omg.PortableServer.ForwardRequest("msg", fwd).forward_reference == fwd,
              "(String,Object) ctor stores forward_reference");
        check(new org.omg.PortableServer.ForwardRequest("msg", fwd).getMessage().equals(
                  "IDL:omg.org/PortableServer/ForwardRequest:1.0  msg"),
              "(String,Object) getMessage = repoId + two spaces + msg");
        check(new org.omg.PortableServer.ForwardRequest().toString().equals(
                  "org.omg.PortableServer.ForwardRequest: IDL:omg.org/PortableServer/ForwardRequest:1.0"),
              "no-arg toString exact");
        check(new org.omg.PortableServer.ForwardRequest("m", null).toString().equals(
                  "org.omg.PortableServer.ForwardRequest: IDL:omg.org/PortableServer/ForwardRequest:1.0  m"),
              "message toString exact");
        org.omg.PortableServer.ForwardRequest frMut = new org.omg.PortableServer.ForwardRequest();
        frMut.forward_reference = fwd;
        check(frMut.forward_reference == fwd, "forward_reference field is public mutable");
        check(java.io.ObjectStreamClass.lookup(org.omg.PortableServer.ForwardRequest.class).getSerialVersionUID()
                  == -4159318367582473975L, "serialVersionUID stable");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new org.omg.PortableServer.ForwardRequest("ser", null));
        }
        org.omg.PortableServer.ForwardRequest serBack = (org.omg.PortableServer.ForwardRequest)
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(serBack.getClass() == org.omg.PortableServer.ForwardRequest.class, "serialization round-trip preserves class");
        check(serBack.getMessage().equals("IDL:omg.org/PortableServer/ForwardRequest:1.0  ser") && serBack.forward_reference == null,
              "serialization round-trip keeps message and null forward");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
