/*
 * @test
 * @summary javax.print.attribute.standard.DialogOwner conformance: dialog owner attribute.
 * @run main DialogOwnerTest
 */

import java.lang.reflect.*;
import javax.print.attribute.standard.*;

public class DialogOwnerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        DialogOwner v = new DialogOwner();
        check(v.getOwner() == null, "getOwner null");
        check(v.getCategory() == DialogOwner.class, "getCategory");
        check(v.getName() != null && v.getName().length() > 0, "getName non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
