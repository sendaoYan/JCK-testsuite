/*
 * @test
 * @summary java.lang.reflect.Field conformance: type/modifiers/get/set and static access.
 * @run main FieldTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public int pub;
        private String priv;
        public static final double CONST = 3.14;
    }

    static void testField() throws Exception {
        Field f = Sample.class.getField("pub");
        check("pub".equals(f.getName()), "getName");
        check(f.getType() == int.class, "getType");
        check(f.getGenericType() == int.class, "getGenericType");
        check(f.getModifiers() == Modifier.PUBLIC, "getModifiers");
        check(f.getDeclaringClass() == Sample.class, "getDeclaringClass");
        Sample s = new Sample();
        f.setInt(s, 5);
        check(f.getInt(s) == 5, "setInt/getInt");
        f.set(s, Integer.valueOf(9));
        check(((Integer) f.get(s)).intValue() == 9, "set/get Object");

        Field c = Sample.class.getField("CONST");
        check(Modifier.isStatic(c.getModifiers()), "static");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(((Double) c.get(null)).doubleValue() == 3.14, "get static");

        Field priv = Sample.class.getDeclaredField("priv");
        check(priv.getType() == String.class, "declared field type");
        check(Sample.class.getFields().length == 2, "getFields length");
        check(Sample.class.getDeclaredFields().length == 3, "getDeclaredFields length");
        check(f.toString() != null, "toString");
        check(f.toGenericString() != null, "toGenericString");
    }

    public static void main(String[] args) throws Exception {
        testField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
