/*
 * @test
 * @summary org.omg.CORBA._IDLTypeStub conformance: stub/tie class shape.
 * @run main _IDLTypeStubTest
 */

public class _IDLTypeStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA._IDLTypeStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.CORBA.IDLType.class.isAssignableFrom(c), "implements org.omg.CORBA.IDLType");
        check(org.omg.CORBA.IRObject.class.isAssignableFrom(c), "implements org.omg.CORBA.IRObject");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "implements org.omg.CORBA.Object");

        // ---- construction without a delegate (jdk8u372 measured) ----
        org.omg.CORBA._IDLTypeStub stub = new org.omg.CORBA._IDLTypeStub();
        check(stub != null, "no-arg construction works offline");
        String[] ids = stub._ids();
        check(ids != null, "_ids() non-null");
        check(ids.length == 2, "_ids() has 2 repository ids");
        check("IDL:omg.org/CORBA/IDLType:1.0".equals(ids[0]), "_ids()[0] == IDL:omg.org/CORBA/IDLType:1.0");
        check("IDL:omg.org/CORBA/IRObject:1.0".equals(ids[1]), "_ids()[1] == IDL:omg.org/CORBA/IRObject:1.0");

        check(stub.toString().startsWith("org.omg.CORBA._IDLTypeStub"),
              "toString names the stub class even without delegate");
        check(stub.toString().contains("no delegate set"), "toString mentions missing delegate");
        check(stub.equals(stub), "equals(self) true without delegate");
        check(stub.hashCode() == stub.hashCode(), "hashCode stable");
        check(!stub.equals(new org.omg.CORBA._IDLTypeStub()), "equals(other stub) false");

        try {
            stub._hash(0);
            throw new AssertionError("_hash without delegate should throw BAD_OPERATION");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "_hash(0) without delegate -> BAD_OPERATION");
        }
        try {
            stub._is_a("IDL:omg.org/CORBA/IDLType:1.0");
            throw new AssertionError("_is_a without delegate should throw BAD_OPERATION");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "_is_a without delegate -> BAD_OPERATION");
        }
        try {
            stub._get_delegate();
            throw new AssertionError("_get_delegate without delegate should throw BAD_OPERATION");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "_get_delegate() without delegate -> BAD_OPERATION('The delegate has not been set!')");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
