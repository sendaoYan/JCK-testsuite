/*
 * @test
 * @summary JLS 8.3.1.4: a field may be declared volatile; the modifier is
 *          visible to reflection.
 * @run main VolatileField
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class Test {
    static volatile int i = 0, j = 0;
}

public class VolatileField {
    public static void main(String[] args) throws Exception {
        Field i = Test.class.getDeclaredField("i");
        Field j = Test.class.getDeclaredField("j");
        if (!Modifier.isVolatile(i.getModifiers())) throw new AssertionError("i not volatile");
        if (!Modifier.isVolatile(j.getModifiers())) throw new AssertionError("j not volatile");
        System.out.println("PASS");
    }
}
