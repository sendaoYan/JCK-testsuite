/*
 * @test
 * @summary JLS 8.9: an enum class may implement one or more interfaces.
 * @run main EnumImplementsInterface
 */

public class EnumImplementsInterface {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Greeting {
        String greet();
    }

    enum Planet implements Greeting {
        EARTH, MARS;
        public String greet() { return "hello " + name(); }
    }

    public static void main(String[] args) {
        check(Planet.EARTH.greet().equals("hello EARTH"), "enum implements interface method");
        check(Planet.MARS.greet().equals("hello MARS"), "interface method per constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
