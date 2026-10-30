/*
 * @test
 * @summary JLS 14.7-14.17 (Java SE 8): if, while, do-while, for, break, continue,
 *          and labeled break control flow.
 * @run main ControlFlowTest
 */

public class ControlFlowTest {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) continue;
            sum += i;
        }
        if (sum != 25) throw new AssertionError("for + if + continue");  // 1+3+5+7+9

        int n = 0;
        while (true) {
            n++;
            if (n >= 3) break;
        }
        if (n != 3) throw new AssertionError("while + break");

        int m = 0;
        do {
            m++;
        } while (m < 5);
        if (m != 5) throw new AssertionError("do-while");

        int count = 0;
        outer:
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) break outer;
                count++;
            }
        }
        if (count != 4) throw new AssertionError("labeled break");

        System.out.println("PASS");
    }
}
