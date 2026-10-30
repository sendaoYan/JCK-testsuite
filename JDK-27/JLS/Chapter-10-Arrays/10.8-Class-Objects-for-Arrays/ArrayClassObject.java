/*
 * @test
 * @summary JLS 10.8: every array's Class object has Object as its direct
 *          superclass and implements Cloneable and java.io.Serializable.
 * @run main ArrayClassObject
 */

public class ArrayClassObject {
    public static void main(String[] args) {
        int[] ia = new int[3];
        Class<?> c = ia.getClass();
        if (c.getSuperclass() != Object.class)
            throw new AssertionError("direct superclass should be Object");

        boolean cloneable = false;
        boolean serializable = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == Cloneable.class) cloneable = true;
            if (i == java.io.Serializable.class) serializable = true;
        }
        if (!cloneable) throw new AssertionError("array should be Cloneable");
        if (!serializable) throw new AssertionError("array should be Serializable");
        System.out.println("PASS");
    }
}
