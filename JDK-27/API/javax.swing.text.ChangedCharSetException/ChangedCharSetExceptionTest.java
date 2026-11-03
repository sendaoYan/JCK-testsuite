/*
 * @test
 * @summary javax.swing.text.ChangedCharSetException conformance: white-box structure + black-box behavior.
 * @run main ChangedCharSetExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import javax.swing.text.ChangedCharSetException;

public class ChangedCharSetExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.ChangedCharSetException");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.IOException"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ChangedCharSetException key = new ChangedCharSetException("UTF-8", true);
        check(key.getCharSetSpec().equals("UTF-8"), "getCharSetSpec true-case spec");
        check(key.keyEqualsCharSet() == true, "keyEqualsCharSet true");
        check(key instanceof IOException, "instanceof IOException");
        check(key instanceof Exception, "instanceof Exception");

        ChangedCharSetException notKey = new ChangedCharSetException("windows-1252", false);
        check(notKey.getCharSetSpec().equals("windows-1252"), "getCharSetSpec false-case spec");
        check(notKey.keyEqualsCharSet() == false, "keyEqualsCharSet false");

        ChangedCharSetException empty = new ChangedCharSetException("", false);
        check(empty.getCharSetSpec().equals(""), "empty spec roundtrip");
        check(empty.keyEqualsCharSet() == false, "empty spec key false");

        ChangedCharSetException nul = new ChangedCharSetException(null, true);
        check(nul.getCharSetSpec() == null, "null spec roundtrip");
        check(nul.keyEqualsCharSet() == true, "null spec key true");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
