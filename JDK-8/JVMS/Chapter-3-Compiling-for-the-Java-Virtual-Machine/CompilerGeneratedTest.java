/*
 * @test
 * @summary JVMS 3 (Java SE 8): the compiler translates Java constructs into JVM
 *          constructs: a default constructor when none is declared, a synthetic
 *          field for an inner class's enclosing instance, and string concatenation
 *          expressed in terms of reference equality on the result.
 * @run main CompilerGeneratedTest
 */
import java.lang.reflect.*;

public class CompilerGeneratedTest {
    static class NoCtor {}

    static class InnerHost {
        private int x = 5;
        class Inner { int get() { return x; } }
    }

    public static void main(String[] args) throws Exception {
        // A default constructor is generated when no constructor is declared.
        Constructor<?> c = NoCtor.class.getDeclaredConstructor();
        if (c == null) throw new AssertionError("default constructor generated");

        // An inner class carries a synthetic field referencing its enclosing instance.
        Field[] fields = InnerHost.Inner.class.getDeclaredFields();
        boolean synthetic = false;
        for (Field f : fields) {
            if (f.isSynthetic()) synthetic = true;
        }
        if (!synthetic) throw new AssertionError("synthetic enclosing field expected");

        // The inner class can reach the host's private field via a synthetic accessor.
        InnerHost host = new InnerHost();
        if (host.new Inner().get() != 5) throw new AssertionError("inner access to private field");

        // String concatenation compiles to a builder; the result is a distinct object.
        String s = "a" + 1 + "b" + 2.5;
        if (!"a1b2.5".equals(s)) throw new AssertionError("concatenation result");

        System.out.println("PASS");
    }
}
