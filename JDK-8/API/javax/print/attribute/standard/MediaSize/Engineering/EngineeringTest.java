/*
 * @test
 * @summary javax.print.attribute.standard.MediaSize.Engineering conformance: media size constants.
 * @run main EngineeringTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class EngineeringTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.attribute.standard.MediaSize$" + "Engineering");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        java.lang.reflect.Field f_A = c.getDeclaredField("A");
        check(java.lang.reflect.Modifier.isStatic(f_A.getModifiers()), "A is static");
        check(f_A.getType() == javax.print.attribute.standard.MediaSize.class, "A type MediaSize");
        check(f_A.get(null) != null, "A non-null");
        java.lang.reflect.Field f_B = c.getDeclaredField("B");
        check(java.lang.reflect.Modifier.isStatic(f_B.getModifiers()), "B is static");
        check(f_B.getType() == javax.print.attribute.standard.MediaSize.class, "B type MediaSize");
        check(f_B.get(null) != null, "B non-null");
        java.lang.reflect.Field f_C = c.getDeclaredField("C");
        check(java.lang.reflect.Modifier.isStatic(f_C.getModifiers()), "C is static");
        check(f_C.getType() == javax.print.attribute.standard.MediaSize.class, "C type MediaSize");
        check(f_C.get(null) != null, "C non-null");
        java.lang.reflect.Field f_D = c.getDeclaredField("D");
        check(java.lang.reflect.Modifier.isStatic(f_D.getModifiers()), "D is static");
        check(f_D.getType() == javax.print.attribute.standard.MediaSize.class, "D type MediaSize");
        check(f_D.get(null) != null, "D non-null");
        java.lang.reflect.Field f_E = c.getDeclaredField("E");
        check(java.lang.reflect.Modifier.isStatic(f_E.getModifiers()), "E is static");
        check(f_E.getType() == javax.print.attribute.standard.MediaSize.class, "E type MediaSize");
        check(f_E.get(null) != null, "E non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
