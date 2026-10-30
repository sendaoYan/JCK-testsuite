/*
 * @test
 * @summary JLS 8.4.8.2: a class method hides an inherited class method; the
 *          method selected is determined by the compile-time type of the
 *          qualifier, whereas an instance method is selected by dynamic dispatch.
 * @run main HidingClassMethods
 */

class Super {
    static String greeting() { return "Goodnight"; }
    String name() { return "Richard"; }
}

class Sub extends Super {
    static String greeting() { return "Hello"; }
    String name() { return "Dick"; }
}

public class HidingClassMethods {
    public static void main(String[] args) {
        Super s = new Sub();
        // greeting is static: selected by the declared type of s (Super)
        // name is an instance method: selected by the run-time class of s (Sub)
        String r = s.greeting() + ", " + s.name();
        if (!"Goodnight, Dick".equals(r)) throw new AssertionError("got: " + r);
        System.out.println("PASS");
    }
}
