/*
 * @test
 * @summary JLS 8.3.1 (Java SE 8): static fields are shared per class, final fields
 *          are constant, and transient/volatile fields carry their modifiers.
 * @run main FieldModifiersTest
 */
import java.lang.reflect.Modifier;

public class FieldModifiersTest {
    static int shared = 0;
    final int fin = 5;
    transient int tr = 1;
    volatile int vo = 2;

    public static void main(String[] args) throws Exception {
        FieldModifiersTest a = new FieldModifiersTest();
        FieldModifiersTest b = new FieldModifiersTest();
        a.shared++;
        if (b.shared != 1) throw new AssertionError("static field shared");
        if (a.fin != 5) throw new AssertionError("final field");

        int fmod = FieldModifiersTest.class.getDeclaredField("tr").getModifiers();
        if (!Modifier.isTransient(fmod)) throw new AssertionError("transient modifier");
        int vmod = FieldModifiersTest.class.getDeclaredField("vo").getModifiers();
        if (!Modifier.isVolatile(vmod)) throw new AssertionError("volatile modifier");

        System.out.println("PASS");
    }
}
