/*
 * @test
 * @summary javax.net.ssl.SNIServerName conformance: white-box structure + black-box behavior.
 * @run main SNIServerNameTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SNIServerNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SNIServerName");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Arrays.stream(Class.forName("javax.net.ssl.SNIServerName").getMethods()).anyMatch(m -> m.getName().equals("getType")), "method getType");
        check(Arrays.stream(Class.forName("javax.net.ssl.SNIServerName").getMethods()).anyMatch(m -> m.getName().equals("getEncoded")), "method getEncoded");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        class SNS extends javax.net.ssl.SNIServerName { SNS(int t, byte[] e) { super(t, e); } }
        SNS n = new SNS(5, new byte[]{1, 2, 3});
        check(n.getType() == 5, "type");
        check(Arrays.equals(n.getEncoded(), new byte[]{1, 2, 3}), "encoded roundtrip");
        byte[] enc = n.getEncoded(); enc[0] = 99;
        check(n.getEncoded()[0] == 1, "encoded defensive copy");
        check(new SNS(0, new byte[]{1,2}).equals(new SNS(0, new byte[]{1,2})), "equals same");
        check(!new SNS(0, new byte[]{1,2}).equals(new SNS(1, new byte[]{1,2})), "equals diff type");
        check(n.hashCode() == new SNS(5, new byte[]{1,2,3}).hashCode(), "hashCode consistent");
        check(n.toString() != null && n.toString().length() > 0, "toString non-empty");
        check(expect(() -> { new SNS(-1, new byte[]{1}); return "ok"; }).equals("IllegalArgumentException"), "negative type rejected");
        check(expect(() -> { new SNS(0, null); return "ok"; }).equals("NullPointerException"), "null encoded NPE");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
