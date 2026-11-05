/*
 * @test
 * @summary org.omg.IOP.CodecOperations conformance: interface shape.
 * @run main CodecOperationsTest
 */

public class CodecOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.IOP.CodecOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): full method face ----
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.IOP.CodecOperations.class.getDeclaredMethods()) names.add(m.getName());
        check(names.equals(new java.util.TreeSet<String>(java.util.Arrays.asList(
                  "encode", "decode", "encode_value", "decode_value"))),
              "exactly 4 methods: encode/decode/encode_value/decode_value");
        check(org.omg.IOP.CodecOperations.class.getDeclaredMethods().length == 4, "4 declared methods");
        java.lang.reflect.Method enc = org.omg.IOP.CodecOperations.class.getDeclaredMethod("encode", org.omg.CORBA.Any.class);
        check(java.lang.reflect.Modifier.isAbstract(enc.getModifiers()), "encode(Any) abstract");
        check(enc.getExceptionTypes().length == 1
              && enc.getExceptionTypes()[0] == org.omg.IOP.CodecPackage.InvalidTypeForEncoding.class,
              "encode throws InvalidTypeForEncoding");
        java.lang.reflect.Method dec = org.omg.IOP.CodecOperations.class.getDeclaredMethod("decode", byte[].class);
        check(dec.getExceptionTypes()[0] == org.omg.IOP.CodecPackage.FormatMismatch.class,
              "decode throws FormatMismatch");
        java.lang.reflect.Method decv = org.omg.IOP.CodecOperations.class.getDeclaredMethod(
            "decode_value", byte[].class, org.omg.CORBA.TypeCode.class);
        check(decv.getExceptionTypes().length == 2,
              "decode_value throws FormatMismatch + TypeMismatch (both declared)");
        check(java.util.Arrays.asList(decv.getExceptionTypes()).contains(org.omg.IOP.CodecPackage.TypeMismatch.class),
              "decode_value declares TypeMismatch");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
