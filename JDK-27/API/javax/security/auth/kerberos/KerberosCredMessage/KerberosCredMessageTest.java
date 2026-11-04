/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosCredMessage conformance: white-box structure + black-box behavior.
 * @run main KerberosCredMessageTest
 */

import javax.security.auth.Destroyable;
import javax.security.auth.kerberos.KerberosCredMessage;
import javax.security.auth.kerberos.KerberosPrincipal;
import java.util.Arrays;

public class KerberosCredMessageTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " [expected " + type.getSimpleName() + ", got " + t + "]", t);
        }
        throw new AssertionError(msg + " [expected " + type.getSimpleName() + "]");
    }
    static boolean hasInterface(Class<?> c, Class<?> want) {
        for (Class<?> i : c.getInterfaces()) if (i == want) return true;
        return false;
    }
    static boolean declares(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.kerberos.KerberosCredMessage");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasInterface(c, Destroyable.class), "implements javax.security.auth.Destroyable");
        for (String m : new String[]{"getEncoded", "getSender", "getRecipient", "destroy", "isDestroyed", "toString", "equals", "hashCode"}) {
            check(declares(c, m), "method " + m + " declared on class");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KerberosPrincipal s = new KerberosPrincipal("sender@EXAMPLE.COM");
        KerberosPrincipal r = new KerberosPrincipal("recipient@EXAMPLE.COM");
        byte[] msg = new byte[]{1, 2, 3, 4};

        // constructor(sender, recipient, encoded DER message)
        KerberosCredMessage m = new KerberosCredMessage(s, r, msg);
        check("sender@EXAMPLE.COM".equals(m.getSender().getName()), "getSender name");
        check("recipient@EXAMPLE.COM".equals(m.getRecipient().getName()), "getRecipient name");
        check(Arrays.equals(new byte[]{1,2,3,4}, m.getEncoded()), "getEncoded contents");

        // constructor copies message; getEncoded returns a copy
        byte[] in = new byte[]{7,8,9};
        KerberosCredMessage cm = new KerberosCredMessage(s, r, in);
        in[0] = 42;
        check(cm.getEncoded()[0] == 7, "constructor copies message");
        byte[] got = cm.getEncoded();
        got[0] = 99;
        check(cm.getEncoded()[0] == 7, "getEncoded returns copy");

        // equals / hashCode: sender + recipient + message
        KerberosCredMessage m1 = new KerberosCredMessage(s, r, new byte[]{1,2});
        KerberosCredMessage m2 = new KerberosCredMessage(
                new KerberosPrincipal("sender@EXAMPLE.COM"),
                new KerberosPrincipal("recipient@EXAMPLE.COM"), new byte[]{1,2});
        KerberosCredMessage m3 = new KerberosCredMessage(r, s, new byte[]{1,2});
        KerberosCredMessage m4 = new KerberosCredMessage(s, r, new byte[]{1,3});
        check(m1.equals(m1), "equals self");
        check(m1.equals(m2), "equals same fields");
        check(!m1.equals(m3), "not equals swapped sender/recipient");
        check(!m1.equals(m4), "not equals different message");
        check(!m1.equals(null), "not equals null");
        check(!m1.equals("x"), "not equals non-message");
        check(m1.hashCode() == m2.hashCode(), "hashCode equal messages");

        // toString before destroy contains sender and recipient names
        check(m1.toString().contains("sender@EXAMPLE.COM") && m1.toString().contains("recipient@EXAMPLE.COM"),
                "toString contains principals");

        // null arguments -> NPE (per javadoc)
        checkThrows(NullPointerException.class, () -> new KerberosCredMessage(null, r, new byte[1]), "null sender -> NPE");
        checkThrows(NullPointerException.class, () -> new KerberosCredMessage(s, null, new byte[1]), "null recipient -> NPE");
        checkThrows(NullPointerException.class, () -> new KerberosCredMessage(s, r, null), "null message -> NPE");

        // destroy lifecycle
        KerberosCredMessage dm = new KerberosCredMessage(s, r, new byte[]{1,2,3});
        check(!dm.isDestroyed(), "not destroyed initially");
        dm.destroy();
        check(dm.isDestroyed(), "destroyed flag");
        check("Destroyed KerberosCredMessage".equals(dm.toString()), "toString after destroy");
        check(dm.hashCode() == -1, "hashCode after destroy == -1");
        check(dm.equals(dm), "destroyed equals self");
        checkThrows(IllegalStateException.class, () -> dm.getSender(), "getSender after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dm.getRecipient(), "getRecipient after destroy -> ISE");
        checkThrows(IllegalStateException.class, () -> dm.getEncoded(), "getEncoded after destroy -> ISE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
