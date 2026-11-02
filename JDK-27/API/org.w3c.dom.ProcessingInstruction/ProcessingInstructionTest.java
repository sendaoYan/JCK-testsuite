/*
 * @test
 * @summary org.w3c.dom.ProcessingInstruction conformance: interface extends Node.
 * @run main ProcessingInstructionTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

public class ProcessingInstructionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(ProcessingInstruction.class.isInterface(),
              "ProcessingInstruction is an interface");
        check(Node.class.isAssignableFrom(ProcessingInstruction.class), "extends Node");
        check(ProcessingInstruction.class.getMethod("getTarget") != null, "getTarget()");
        check(ProcessingInstruction.class.getMethod("getData") != null, "getData()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
