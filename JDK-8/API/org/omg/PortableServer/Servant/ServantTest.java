/*
 * @test
 * @summary org.omg.PortableServer.Servant conformance: abstract class shape.
 * @run main ServantTest
 */

public class ServantTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.Servant");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        // ---- deepened (jdk8u372-b07 probed): unset-delegate matrix / Delegate routing / offline association ----
        check(c.getDeclaredConstructors().length == 1
           && c.getDeclaredConstructors()[0].getParameterTypes().length == 0,
              "single public no-arg ctor");
        check(c.getSuperclass() == java.lang.Object.class, "directly extends Object");
        java.lang.reflect.Method ai = c.getDeclaredMethod("_all_interfaces",
              org.omg.PortableServer.POA.class, byte[].class);
        check(java.lang.reflect.Modifier.isAbstract(ai.getModifiers()), "_all_interfaces is the only abstract member");
        int finals = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isFinal(m.getModifiers())) finals++;
        check(finals == 7, "7 final methods (_get_delegate.._object_id) lock the delegation channel");
        check(c.getDeclaredMethods().length == 12, "12 declared methods total (7 final + 4 plain + 1 abstract)");

        final String[] MY_IDS = {"IDL:DeepE/Servant:1.0"};
        org.omg.PortableServer.Servant sv = new org.omg.PortableServer.Servant() {
            public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] oid) { return MY_IDS; }
        };
        check(java.util.Arrays.equals(sv._all_interfaces(null, new byte[]{7}), MY_IDS),
              "anonymous subclass routes _all_interfaces verbatim");

        // unset delegate: every channel -> BAD_INV_ORDER, message exact (jdk8u372-b07)
        java.io.PrintStream realErr = System.err;
        System.setErr(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
        java.util.List<java.util.concurrent.Callable<Object>> unset = new java.util.ArrayList<>();
        unset.add(() -> sv._get_delegate());
        unset.add(() -> sv._this_object());
        unset.add(() -> sv._orb());
        unset.add(() -> sv._poa());
        unset.add(() -> sv._object_id());
        unset.add(() -> sv._default_POA());
        unset.add(() -> sv._is_a("IDL:x:1.0"));
        unset.add(() -> sv._non_existent());
        unset.add(() -> sv._get_interface_def());
        for (java.util.concurrent.Callable<Object> call : unset) {
            try {
                call.call();
                check(false, "unset-delegate call must throw BAD_INV_ORDER");
            } catch (org.omg.CORBA.BAD_INV_ORDER expected) {
                check(expected.minor == 0
                   && expected.getMessage().equals("The Servant has not been associated with an ORB instance"),
                      "unset delegate -> BAD_INV_ORDER minor==0, message exact");
            }
        }

        // custom Delegate routing: 8 channels, call order recorded
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        final java.util.List<String> log = new java.util.ArrayList<>();
        final org.omg.PortableServer.portable.Delegate d = new org.omg.PortableServer.portable.Delegate() {
            public org.omg.CORBA.ORB orb(org.omg.PortableServer.Servant self) { log.add("orb"); return orb; }
            public org.omg.CORBA.Object this_object(org.omg.PortableServer.Servant self) { log.add("this_object"); return null; }
            public org.omg.PortableServer.POA poa(org.omg.PortableServer.Servant self) { log.add("poa"); return null; }
            public byte[] object_id(org.omg.PortableServer.Servant self) { log.add("object_id"); return new byte[]{1}; }
            public org.omg.PortableServer.POA default_POA(org.omg.PortableServer.Servant self) { log.add("default_POA"); return null; }
            public boolean is_a(org.omg.PortableServer.Servant self, String rid) { log.add("is_a"); return true; }
            public boolean non_existent(org.omg.PortableServer.Servant self) { log.add("non_existent"); return false; }
            public org.omg.CORBA.Object get_interface_def(org.omg.PortableServer.Servant self) { log.add("get_interface_def"); return null; }
        };
        sv._set_delegate(d);
        check(sv._orb() == orb, "_orb() routes to Delegate.orb");
        check(sv._is_a("x") && !sv._non_existent(), "boolean channels route through Delegate");
        byte[] routedOid = sv._object_id();
        check(routedOid != null && routedOid[0] == 1, "_object_id() routes through Delegate");
        check(log.equals(java.util.Arrays.asList("orb", "is_a", "non_existent", "object_id")),
              "each Servant helper makes exactly the matching Delegate call, in order");
        sv._set_delegate(null);
        try {
            sv._orb();
            check(false, "_set_delegate(null) must revert to unset state");
        } catch (org.omg.CORBA.BAD_INV_ORDER expected) {
            check(true, "_set_delegate(null) returns Servant to the unset state");
        }

        // offline association: _this_object(orb) instantly installs a RootPOA delegate
        org.omg.CORBA.Object assoc = sv._this_object(orb);
        check(assoc != null && assoc.toString().startsWith("IOR:"), "_this_object(orb) returns an IOR-backed object");
        check(sv._orb() == orb, "after association _orb() == the ORB passed in");
        check(sv._poa() == sv._default_POA(), "_poa() == _default_POA() after association");
        check(sv._poa().toString().equals("POA[ObjectAdapterID[RootPOA], uniquePOAId=0, state=RUN, invocationCount=0]"),
              "RootPOA toString exact (offline default adapter)");
        check(sv._get_delegate().getClass().getName().equals("com.sun.corba.se.impl.oa.poa.DelegateImpl"),
              "jdk8 delegate impl class exact");
        check(sv._this_object() != sv._this_object(), "_this_object() mints a fresh object reference per call");
        check(!log.contains("this_object"), "_this_object(orb) overwrote the custom delegate (Routing log untouched)");
        check(sv._get_delegate() != d, "custom delegate was replaced by the ORB delegate");
        try {
            sv._object_id();
            check(false, "associated-but-inactive servant _object_id must throw");
        } catch (org.omg.CORBA.OBJ_ADAPTER expected) {
            check(expected.minor == 0x535503FD, "not-yet-activated servant -> OBJ_ADAPTER minor=0x535503FD (SUN noContext)");
        }
        try {
            sv._is_a("IDL:x:1.0");
            check(false, "associated-but-inactive servant _is_a must throw");
        } catch (org.omg.CORBA.OBJ_ADAPTER expected) {
            check(expected.minor == 0x535503FD, "_is_a on inactive servant -> OBJ_ADAPTER minor=0x535503FD");
        }
        try {
            sv._get_interface_def();
            check(false, "_get_interface_def must throw on this build");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            check(expected.minor == 0x535503EA, "_get_interface_def -> NO_IMPLEMENT minor=0x535503EA (methodNotImplemented)");
        }
        System.setErr(realErr);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
