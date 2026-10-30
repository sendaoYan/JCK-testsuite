/*
 * @test
 * @summary JLS 8.8.10: a class that declares only a private constructor cannot be
 *          instantiated, but its static members remain usable.
 * @run main UtilityClassPrivateConstructor
 */

class ClassOnly {
    private ClassOnly() {}
    static String just = "only the lonely";
    static String get() { return just; }
}

public class UtilityClassPrivateConstructor {
    public static void main(String[] args) {
        if (!"only the lonely".equals(ClassOnly.get())) throw new AssertionError("static member");
        System.out.println("PASS");
    }
}
