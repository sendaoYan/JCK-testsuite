/*
 * @test
 * @summary java.lang.IO conformance: white-box structure + black-box behavior.
 * @run main IOTest
 */

import java.lang.IO;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class IOTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.IO");
        check(Modifier.isFinal(c.getModifiers()), "IO is final");
        check(c.getConstructors().length == 0, "IO has no public constructors");
        check(c.getMethod("println", Object.class) != null, "println(Object) present");
        check(c.getMethod("println") != null, "println() present");
        check(c.getMethod("print", Object.class) != null, "print(Object) present");
        check(c.getMethod("readln") != null, "readln() present");
        check(c.getMethod("readln", String.class) != null, "readln(String) present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testOutput() {
        // These write to System.out; they must not throw.
        IO.println("jck-io-println");
        IO.println();
        IO.print("jck-io-print");
        passed += 3;
    }

    
    static void testBlackBox() throws Exception {
        testOutput();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
