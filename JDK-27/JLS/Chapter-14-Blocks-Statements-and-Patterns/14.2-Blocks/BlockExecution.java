/*
 * @test
 * @summary JLS 14.2: a block executes its statements in order; if one completes
 *          abruptly the block completes abruptly for the same reason.
 * @run main BlockExecution
 */

public class BlockExecution {
    static StringBuilder log = new StringBuilder();

    public static void main(String[] args) {
        {
            log.append('a');
            log.append('b');
            log.append('c');
        }
        if (!"abc".equals(log.toString()))
            throw new AssertionError("statements not executed in order: " + log);

        log.setLength(0);
        try {
            {
                log.append('1');
                throw new RuntimeException("stop");
                // unreachable
            }
        } catch (RuntimeException e) {
            if (!"stop".equals(e.getMessage()))
                throw new AssertionError("reason not preserved");
        }
        // The statement after the throw in the block did not execute.
        if (!"1".equals(log.toString()))
            throw new AssertionError("statement after abrupt completion ran: " + log);

        System.out.println("PASS");
    }
}
