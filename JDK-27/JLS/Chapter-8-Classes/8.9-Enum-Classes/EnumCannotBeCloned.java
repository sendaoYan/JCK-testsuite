/*
 * @test
 * @summary JLS 8.9.3: the clone method inherited from java.lang.Enum always throws
 *          CloneNotSupportedException, so enum constants cannot be duplicated.
 * @run main/othervm --add-opens=java.base/java.lang=ALL-UNNAMED EnumCannotBeCloned
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnumCannotBeCloned {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    enum E { A }

    public static void main(String[] args) throws Exception {
        Method cl = Enum.class.getDeclaredMethod("clone");
        cl.setAccessible(true);
        try {
            cl.invoke(E.A);
            throw new AssertionError("expected CloneNotSupportedException");
        } catch (InvocationTargetException e) {
            check(e.getCause() instanceof CloneNotSupportedException,
                  "clone() throws CloneNotSupportedException");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
