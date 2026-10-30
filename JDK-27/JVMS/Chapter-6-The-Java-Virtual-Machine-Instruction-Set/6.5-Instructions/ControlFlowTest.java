/*
 * @test
 * @summary JVMS 6.5: control transfer instructions if_icmpeq, goto, tableswitch,
 *          and lookupswitch transfer execution to labeled branch targets.
 * @compile ControlFlow.jasm
 * @run main ControlFlowTest
 */

public class ControlFlowTest {
    public static void main(String[] args) {
        if (ControlFlow.ifIcmpeq(3, 3) != 1) throw new AssertionError("if_icmpeq equal");
        if (ControlFlow.ifIcmpeq(3, 4) != 0) throw new AssertionError("if_icmpeq not equal");
        if (ControlFlow.gotoTest() != 7) throw new AssertionError("goto");

        if (ControlFlow.tableSwitch(0) != 10) throw new AssertionError("tableswitch 0");
        if (ControlFlow.tableSwitch(2) != 30) throw new AssertionError("tableswitch 2");
        if (ControlFlow.tableSwitch(9) != 99) throw new AssertionError("tableswitch default");

        if (ControlFlow.lookupSwitch(-100) != 1) throw new AssertionError("lookupswitch -100");
        if (ControlFlow.lookupSwitch(0) != 2) throw new AssertionError("lookupswitch 0");
        if (ControlFlow.lookupSwitch(100) != 3) throw new AssertionError("lookupswitch 100");
        if (ControlFlow.lookupSwitch(7) != 99) throw new AssertionError("lookupswitch default");

        System.out.println("PASS");
    }
}
