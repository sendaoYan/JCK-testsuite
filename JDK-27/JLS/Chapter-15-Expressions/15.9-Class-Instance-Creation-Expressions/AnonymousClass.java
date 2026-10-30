/*
 * @test
 * @summary JLS 15.9.5: an anonymous class declaration defines and instantiates a
 *          class in one expression; its body may override methods and capture
 *          effectively final variables.
 * @run main AnonymousClass
 */

public class AnonymousClass {
    interface Greeter { String greet(); }

    public static void main(String[] args) {
        String prefix = "hi";   // effectively final, captured by the anonymous class
        Greeter g = new Greeter() {
            public String greet() { return prefix + "!"; }
        };
        if (!"hi!".equals(g.greet())) throw new AssertionError("anonymous override");

        Object o = new Object() {
            int val = 7;
            public String toString() { return "anon:" + val; }
        };
        if (!"anon:7".equals(o.toString())) throw new AssertionError("anonymous members");

        System.out.println("PASS");
    }
}
