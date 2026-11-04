/*
 * @test
 * @summary javax.activation.CommandInfo conformance: command info accessors.
 * @run main CommandInfoTest
 */


public class CommandInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.activation.CommandInfo ci = new javax.activation.CommandInfo("open", "com.example.Open");
        check("open".equals(ci.getCommandName()), "command name");
        check("com.example.Open".equals(ci.getCommandClass()), "command class");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
