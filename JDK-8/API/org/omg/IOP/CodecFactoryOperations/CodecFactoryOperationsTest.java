/*
 * @test
 * @summary org.omg.IOP.CodecFactoryOperations conformance: interface shape.
 * @run main CodecFactoryOperationsTest
 */

public class CodecFactoryOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.CodecFactoryOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): full method face ----
        java.lang.reflect.Method[] ms = org.omg.IOP.CodecFactoryOperations.class.getDeclaredMethods();
        check(ms.length == 1, "exactly one method");
        check(ms[0].getName().equals("create_codec"), "method is create_codec");
        check(java.util.Arrays.equals(ms[0].getParameterTypes(), new Class<?>[]{org.omg.IOP.Encoding.class}),
              "create_codec(org.omg.IOP.Encoding)");
        check(ms[0].getReturnType() == org.omg.IOP.Codec.class, "returns org.omg.IOP.Codec");
        check(java.lang.reflect.Modifier.isAbstract(ms[0].getModifiers()), "abstract");
        check(ms[0].getExceptionTypes().length == 1
              && ms[0].getExceptionTypes()[0] == org.omg.IOP.CodecFactoryPackage.UnknownEncoding.class,
              "throws UnknownEncoding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
