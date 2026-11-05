/*
 * @test
 * @summary org.ietf.jgss.GSSException conformance: white-box structure + black-box behavior.
 * @run main GSSExceptionTest
 */

import org.ietf.jgss.*;
import java.lang.reflect.*;

public class GSSExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasCtor(Class<?> c, Class<?>... p) { try { c.getDeclaredConstructor(p); return true; } catch (Exception e) { return false; } }
    static Field field(Class<?> c, String n) { try { return c.getDeclaredField(n); } catch (NoSuchFieldException e) { return null; } }
    static void checkConstInt(Class<?> c, String n) {
        Field f = field(c, n);
        check(f != null && Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                && Modifier.isFinal(f.getModifiers()) && f.getType() == int.class,
                "public static final int " + n);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.GSSException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class not interface");
        check(c.getSuperclass() == Exception.class, "extends java.lang.Exception");
        check(hasCtor(c, int.class), "ctor(int)");
        check(hasCtor(c, int.class, int.class, String.class), "ctor(int,int,String)");
        check(hasMethod(c, "getMajor"), "getMajor");
        check(hasMethod(c, "getMinor"), "getMinor");
        check(hasMethod(c, "getMajorString"), "getMajorString");
        check(hasMethod(c, "getMinorString"), "getMinorString");
        check(hasMethod(c, "setMinor"), "setMinor");
        check(hasMethod(c, "toString"), "toString");
        check(hasMethod(c, "getMessage"), "getMessage");
        String[] names = { "BAD_BINDINGS", "BAD_MECH", "BAD_NAME", "BAD_NAMETYPE", "BAD_STATUS",
                "BAD_MIC", "CONTEXT_EXPIRED", "CREDENTIALS_EXPIRED", "DEFECTIVE_CREDENTIAL",
                "DEFECTIVE_TOKEN", "FAILURE", "NO_CONTEXT", "NO_CRED", "BAD_QOP", "UNAUTHORIZED",
                "UNAVAILABLE", "DUPLICATE_ELEMENT", "NAME_NOT_MN", "DUPLICATE_TOKEN", "OLD_TOKEN",
                "UNSEQ_TOKEN", "GAP_TOKEN" };
        for (String n : names) checkConstInt(c, n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // all major codes are 1..22 in declaration order
        int[] codes = { GSSException.BAD_BINDINGS, GSSException.BAD_MECH, GSSException.BAD_NAME,
                GSSException.BAD_NAMETYPE, GSSException.BAD_STATUS, GSSException.BAD_MIC,
                GSSException.CONTEXT_EXPIRED, GSSException.CREDENTIALS_EXPIRED,
                GSSException.DEFECTIVE_CREDENTIAL, GSSException.DEFECTIVE_TOKEN, GSSException.FAILURE,
                GSSException.NO_CONTEXT, GSSException.NO_CRED, GSSException.BAD_QOP,
                GSSException.UNAUTHORIZED, GSSException.UNAVAILABLE, GSSException.DUPLICATE_ELEMENT,
                GSSException.NAME_NOT_MN, GSSException.DUPLICATE_TOKEN, GSSException.OLD_TOKEN,
                GSSException.UNSEQ_TOKEN, GSSException.GAP_TOKEN };
        for (int i = 0; i < codes.length; i++) {
            check(codes[i] == i + 1, "major code " + (i + 1) + " value");
        }

        // sentinel majors used across the jgss batch
        check(GSSException.BAD_MECH == 2, "BAD_MECH == 2");
        check(GSSException.BAD_NAME == 3, "BAD_NAME == 3");
        check(GSSException.FAILURE == 11, "FAILURE == 11");
        check(GSSException.CONTEXT_EXPIRED == 7, "CONTEXT_EXPIRED == 7");
        check(GSSException.NO_CONTEXT == 12, "NO_CONTEXT == 12");
        check(GSSException.NO_CRED == 13, "NO_CRED == 13");

        // every defined major has a non-null message from the table
        for (int i = 0; i < codes.length; i++) {
            check(new GSSException(codes[i]).getMajorString() != null, "majorString defined for code " + (i + 1));
        }

        // default ctor(int): major string derived from code, minor not set
        GSSException e = new GSSException(GSSException.BAD_NAME);
        check(e.getMajor() == GSSException.BAD_NAME, "getMajor BAD_NAME");
        check("Invalid name provided".equals(e.getMajorString()), "getMajorString BAD_NAME");
        check(e.getMinor() == 0, "getMinor defaults to 0");
        check(e.getMinorString() == null, "getMinorString defaults to null");
        check("Invalid name provided".equals(e.getMessage()), "getMessage equals major string when minor 0");

        // FAILURE major string
        check("Failure unspecified at GSS-API level".equals(
                new GSSException(GSSException.FAILURE).getMajorString()), "getMajorString FAILURE");

        // invalid major code falls back to FAILURE
        check(new GSSException(0).getMajor() == GSSException.FAILURE, "invalid major 0 -> FAILURE");
        check(new GSSException(999).getMajor() == GSSException.FAILURE, "invalid major 999 -> FAILURE");
        check(new GSSException(999).getMajorString() != null, "invalid major still has majorString");

        // 3-arg ctor with minor detail
        GSSException e2 = new GSSException(GSSException.BAD_MECH, 5, "detail");
        check(e2.getMajor() == GSSException.BAD_MECH, "getMajor BAD_MECH");
        check(e2.getMinor() == 5, "getMinor");
        check("detail".equals(e2.getMinorString()), "getMinorString");
        check("Unsupported mechanism requested (Mechanism level: detail)".equals(e2.getMessage()),
                "getMessage includes minor detail");

        // setMinor round trip updates minor + message
        e2.setMinor(7, "new");
        check(e2.getMinor() == 7, "setMinor getMinor");
        check("new".equals(e2.getMinorString()), "setMinor getMinorString");
        check("Unsupported mechanism requested (Mechanism level: new)".equals(e2.getMessage()),
                "setMinor updates getMessage");

        // toString prefix
        check(e.toString().startsWith("GSSException: "), "toString prefix");
        check("GSSException: Invalid name provided".equals(new GSSException(GSSException.BAD_NAME).toString()),
                "toString for minor==0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
