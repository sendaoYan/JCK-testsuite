/*
 * @test
 * @summary JLS 14.20.1: a try-catch statement selects the first (leftmost) catch
 *          clause whose type is assignment-compatible with the thrown value; a
 *          multi-catch clause has a parameter type that is the lub of its
 *          alternatives.
 * @run main TryCatch
 */

import java.io.FileNotFoundException;
import java.io.IOException;

public class TryCatch {
    static void blow() throws Exception { throw new IOException("io"); }

    static void reflective() throws ReflectiveOperationException {
        throw new ClassNotFoundException();
    }

    public static void main(String[] args) throws Exception {
        String which = "";
        try {
            blow();
        } catch (FileNotFoundException e) {
            which = "fnf";
        } catch (IOException e) {
            which = "io";
        } catch (Exception e) {
            which = "ex";
        }
        if (!"io".equals(which))
            throw new AssertionError("first matching catch: " + which);

        try {
            reflective();
        } catch (ClassNotFoundException | IllegalAccessException e) {
            ReflectiveOperationException roe = e;   // lub of the alternatives
            if (!(roe instanceof ClassNotFoundException))
                throw new AssertionError("multi-catch lub type");
        }

        System.out.println("PASS");
    }
}
