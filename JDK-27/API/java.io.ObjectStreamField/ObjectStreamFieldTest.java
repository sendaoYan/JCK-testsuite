/*
 * @test
 * @summary java.io.ObjectStreamField API conformance: field descriptor accessors.
 * @run main ObjectStreamFieldTest
 */

import java.io.ObjectStreamField;

public class ObjectStreamFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjectStreamField() {
        ObjectStreamField f = new ObjectStreamField("name", String.class);
        check("name".equals(f.getName()), "getName");
        check(f.getType() == String.class, "getType");
        check("Ljava/lang/String;".equals(f.getTypeString()), "getTypeString for object");
        check(!f.isPrimitive(), "object is not primitive");
        check(f.getTypeCode() == 'L', "object typeCode");

        ObjectStreamField fi = new ObjectStreamField("count", int.class);
        check(fi.isPrimitive(), "int is primitive");
        check(fi.getTypeString() == null, "getTypeString is null for primitive");
        check(fi.getTypeCode() == 'I', "int typeCode");
        check(f.toString().contains("name"), "toString includes the name");
    }

    public static void main(String[] args) {
        testObjectStreamField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
