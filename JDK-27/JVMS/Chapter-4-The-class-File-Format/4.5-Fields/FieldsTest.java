/*
 * @test
 * @summary JVMS 4.5: field_info structures record a field's access flags and
 *          descriptor.  Reflection exposes the same flags and declared types.
 * @run main FieldsTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldsTest {
    public static final int PUB = 1;
    private int priv;
    protected int prot;
    int pack;
    static int stat;
    final int fin = 0;
    volatile int vol;

    public static void main(String[] args) throws Exception {
        Class<?> c = FieldsTest.class;

        requireFlags(c.getField("PUB"), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, "PUB");
        requireFlags(c.getDeclaredField("priv"), Modifier.PRIVATE, "priv");
        requireFlags(c.getDeclaredField("prot"), Modifier.PROTECTED, "prot");

        Field pack = c.getDeclaredField("pack");
        if ((pack.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED)) != 0)
            throw new AssertionError("package field should have no access modifier");

        requireFlags(c.getDeclaredField("stat"), Modifier.STATIC, "stat");
        requireFlags(c.getDeclaredField("fin"), Modifier.FINAL, "fin");
        requireFlags(c.getDeclaredField("vol"), Modifier.VOLATILE, "vol");

        if (c.getField("PUB").getType() != int.class) throw new AssertionError("PUB type");
        if (c.getDeclaredField("priv").getType() != int.class) throw new AssertionError("priv type");

        System.out.println("PASS");
    }

    static void requireFlags(Field f, int mask, String name) {
        if ((f.getModifiers() & mask) != mask)
            throw new AssertionError(name + ": expected flags 0x" + Integer.toHexString(mask)
                + " but got 0x" + Integer.toHexString(f.getModifiers()));
    }
}
