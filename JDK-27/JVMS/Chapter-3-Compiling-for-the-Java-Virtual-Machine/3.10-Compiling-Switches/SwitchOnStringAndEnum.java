/*
 * @test
 * @summary JVMS 3.10: a switch on String compiles to a hashCode-then-equals
 *          dispatch and a switch on an enum compiles to an ordinal dispatch;
 *          both select the matching case and fall back to default.
 * @run main SwitchOnStringAndEnum
 */

public class SwitchOnStringAndEnum {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    enum Color { RED, GREEN, BLUE }

    static int stringSwitch(String s) {
        switch (s) {
            case "aa": return 1;
            case "bb": return 2;
            default: return -1;
        }
    }

    static int enumSwitch(Color c) {
        switch (c) {
            case RED: return 100;
            case GREEN: return 200;
            default: return -1;
        }
    }

    public static void main(String[] args) {
        check(stringSwitch("aa") == 1, "string switch selects case aa");
        check(stringSwitch("bb") == 2, "string switch selects case bb");
        check(stringSwitch("cc") == -1, "string switch default for no match");

        check(enumSwitch(Color.RED) == 100, "enum switch selects RED");
        check(enumSwitch(Color.GREEN) == 200, "enum switch selects GREEN");
        check(enumSwitch(Color.BLUE) == -1, "enum switch default for no match");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
