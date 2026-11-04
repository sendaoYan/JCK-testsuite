/*
 * @test
 * @summary JLS 11.2.2 (Java SE 8): a throw statement whose thrown expression has
 *          static type E can throw E (and only E, not a supertype), so a catch of
 *          a supertype after a catch of E is not statically unreachable.
 * @run main ThrowStatementExactType
 */

import java.io.FileNotFoundException;
import java.io.IOException;

public class ThrowStatementExactType {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        String r = null;
        try {
            throw new FileNotFoundException();
        } catch (FileNotFoundException e) {
            r = "fnf";
        } catch (IOException e) {
            r = "ioe";
        }
        check("fnf".equals(r), "most specific catch handles FileNotFoundException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
