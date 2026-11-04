/*
 * @test
 * @summary javax.security.auth.kerberos.KeyTab conformance: white-box structure + black-box behavior.
 * @run main KeyTabTest
 */

import javax.security.auth.kerberos.KeyTab;
import javax.security.auth.kerberos.KerberosPrincipal;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class KeyTabTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.kerberos.KeyTab");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "extends Object");
        // no public constructor: only static factory methods
        check(c.getConstructors().length == 0, "no public constructors");
        for (String m : new String[]{"getInstance", "getUnboundInstance", "getKeys", "exists",
                "toString", "hashCode", "equals", "getPrincipal", "isBound"}) {
            check(c.getMethods() != null && hasMethod(c, m), "public method " + m + " present");
        }
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        File missing = new File(dir.toFile(), "missing.keytab");
        try {
            KerberosPrincipal p = new KerberosPrincipal("host/foo.example.com@EXAMPLE.COM");
            KerberosPrincipal other = new KerberosPrincipal("other@EXAMPLE.COM");

            // unbound default instance: not bound, no principal
            KeyTab unbound = KeyTab.getUnboundInstance();
            check(!unbound.isBound(), "unbound default not bound");
            check(unbound.getPrincipal() == null, "unbound default principal null");

            // unbound file instance: never reads file, missing -> exists false
            KeyTab unboundFile = KeyTab.getUnboundInstance(missing);
            check(!unboundFile.isBound(), "unbound file not bound");
            check(unboundFile.getPrincipal() == null, "unbound file principal null");
            check(!unboundFile.exists(), "missing file not exists");
            check(unboundFile.getKeys(p).length == 0, "unbound missing file no keys");

            // bound-to-unknown-principal (legacy) instance
            KeyTab unknownBound = KeyTab.getInstance(missing);
            check(unknownBound.isBound(), "getInstance(File) bound");
            check(unknownBound.getPrincipal() == null, "getInstance(File) principal null");
            check(!unknownBound.exists(), "getInstance(File) missing file not exists");

            // bound to a specific principal
            KeyTab bound = KeyTab.getInstance(p, missing);
            check(bound.isBound(), "bound instance bound");
            check(p.equals(bound.getPrincipal()), "bound instance principal");
            check(!bound.exists(), "bound missing file not exists");
            check(bound.getKeys(p).length == 0, "bound missing file no keys for own principal");
            check(bound.getKeys(other).length == 0, "bound instance no keys for other principal");

            // default bound-to-principal instance (does not touch filesystem for isBound/getPrincipal)
            KeyTab boundDefault = KeyTab.getInstance(p);
            check(boundDefault.isBound(), "default bound instance bound");
            check(p.equals(boundDefault.getPrincipal()), "default bound instance principal");

            // equals / hashCode: file + principal + bound flag
            KeyTab a1 = KeyTab.getInstance(missing);
            KeyTab a2 = KeyTab.getInstance(new File(dir.toFile(), "missing.keytab"));
            KeyTab a3 = KeyTab.getUnboundInstance(missing);
            check(a1.equals(a1), "equals self");
            check(a1.equals(a2), "equals same file/bound/princ");
            check(a1.hashCode() == a2.hashCode(), "hashCode same");
            check(!a1.equals(a3), "bound vs unbound not equal");
            check(!a1.equals(null), "not equals null");
            check(!a1.equals("x"), "not equals non-keytab");

            // toString carries bound/principal info
            check(bound.toString().contains("host/foo.example.com@EXAMPLE.COM"), "bound toString contains principal");

            // null argument validation
            checkThrows(NullPointerException.class, () -> KeyTab.getInstance((File) null), "getInstance((File)null) -> NPE");
            checkThrows(NullPointerException.class, () -> KeyTab.getUnboundInstance((File) null), "getUnboundInstance((File)null) -> NPE");
            checkThrows(NullPointerException.class, () -> KeyTab.getInstance((KerberosPrincipal) null), "getInstance((KerberosPrincipal)null) -> NPE");
            checkThrows(NullPointerException.class, () -> KeyTab.getInstance(p, null), "getInstance(p, null file) -> NPE");
            checkThrows(NullPointerException.class, () -> KeyTab.getInstance(null, missing), "getInstance(null princ, file) -> NPE");
            checkThrows(NullPointerException.class, () -> bound.getKeys(null), "getKeys(null) -> NPE");
        } finally {
            Files.deleteIfExists(missing.toPath());
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
