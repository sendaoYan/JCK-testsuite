/*
 * @test
 * @summary JVMS 4.1/4.5 (Java SE 8): access_flags on the ClassFile and field_info
 *          structures encode public/static/final/private modifiers, observable via
 *          reflection.
 * @run main AccessFlagsTest
 */
import java.lang.reflect.*;

public class AccessFlagsTest {
    public static final int CONST = 3;
    private int instance = 4;

    public static void main(String[] args) throws Exception {
        if (!Modifier.isPublic(AccessFlagsTest.class.getModifiers()))
            throw new AssertionError("class public access flag");

        Field c = AccessFlagsTest.class.getDeclaredField("CONST");
        int mc = c.getModifiers();
        if (!Modifier.isPublic(mc) || !Modifier.isStatic(mc) || !Modifier.isFinal(mc))
            throw new AssertionError("field public static final flags");

        Field i = AccessFlagsTest.class.getDeclaredField("instance");
        if (!Modifier.isPrivate(i.getModifiers()))
            throw new AssertionError("field private flag");

        if (Modifier.isStatic(AccessFlagsTest.class.getModifiers()))
            throw new AssertionError("class should not be static");

        System.out.println("PASS");
    }
}
