/*
 * @test
 * @summary JLS 8.3.1.3: variables may be marked transient to indicate they are
 *          not part of the persistent state of an object; the modifier is
 *          visible to reflection.
 * @run main TransientField
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class Point {
    int x, y;
    transient float rho, theta;
}

public class TransientField {
    public static void main(String[] args) throws Exception {
        Field rho = Point.class.getDeclaredField("rho");
        Field x = Point.class.getDeclaredField("x");
        if (!Modifier.isTransient(rho.getModifiers()))
            throw new AssertionError("rho must be transient");
        if (Modifier.isTransient(x.getModifiers()))
            throw new AssertionError("x must not be transient");
        System.out.println("PASS");
    }
}
