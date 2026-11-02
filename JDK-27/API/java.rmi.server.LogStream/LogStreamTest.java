/*
 * @test
 * @summary java.rmi.server.LogStream conformance: default stream and level parsing.
 * @run main LogStreamTest
 */

import java.rmi.server.LogStream;

public class LogStreamTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(LogStream.getDefaultStream() != null, "default stream");
        LogStream ls = LogStream.log("conformance");
        check(ls != null, "log stream");
        check(LogStream.parseLevel("SILENT") == LogStream.SILENT, "parse SILENT");
        check(LogStream.parseLevel("BRIEF") == LogStream.BRIEF, "parse BRIEF");
        check(LogStream.parseLevel("VERBOSE") == LogStream.VERBOSE, "parse VERBOSE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
