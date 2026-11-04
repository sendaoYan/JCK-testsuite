/*
 * @test
 * @summary javax.net.ssl.StandardConstants conformance: SNI_HOST_NAME constant + class shape.
 * @run main StandardConstantsTest
 */
import java.lang.reflect.*;
public class StandardConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.StandardConstants");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()), "public final class");
        int pubStaticFinalInt = 0;
        for (Field f : c.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                    && Modifier.isPublic(f.getModifiers()) && f.getType() == int.class) pubStaticFinalInt++;
        }
        check(pubStaticFinalInt == 1, "exactly 1 public static final int constant, found " + pubStaticFinalInt);
        check(javax.net.ssl.StandardConstants.SNI_HOST_NAME == 0, "SNI_HOST_NAME == 0");
        Field f = c.getDeclaredField("SNI_HOST_NAME");
        check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                && f.getType() == int.class, "SNI_HOST_NAME is static final int");
        // the type value agrees with SNIHostName instances
        check(new javax.net.ssl.SNIHostName("a.com").getType() == javax.net.ssl.StandardConstants.SNI_HOST_NAME,
                "SNIHostName type matches SNI_HOST_NAME");
        // private constructor: not instantiable
        boolean privateCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPrivate(k.getModifiers())) privateCtor = true;
        check(privateCtor, "private constructor (constants-only class)");
        Object res = null;
        try { c.getDeclaredConstructor().newInstance(); }
        catch (IllegalAccessException e) { res = e; }
        check(res instanceof IllegalAccessException, "newInstance -> IllegalAccessException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
