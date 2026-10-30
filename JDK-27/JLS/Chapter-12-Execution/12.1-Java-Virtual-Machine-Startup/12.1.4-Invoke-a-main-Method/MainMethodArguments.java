/*
 * @test
 * @summary JLS 12.1.4: the JVM invokes a candidate main method, passing an array
 *          of strings built from the command line arguments.
 * @run main MainMethodArguments foo bar baz
 */

public class MainMethodArguments {
    public static void main(String[] args) {
        if (args.length != 3)
            throw new AssertionError("argument count: " + args.length);
        if (!"foo".equals(args[0]) || !"bar".equals(args[1]) || !"baz".equals(args[2]))
            throw new AssertionError("argument values");
        System.out.println("PASS");
    }
}
