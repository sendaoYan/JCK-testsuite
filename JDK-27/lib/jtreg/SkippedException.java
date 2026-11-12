package jtreg;

/**
 * Thrown by a test to report that it could not run because the environment
 * lacks a required capability (for example: no privilege to create symbolic
 * links on a non-elevated Windows host, no C compiler for a native test, or
 * no audio/MIDI device for a sound test). jtreg's agent recognizes this class
 * by name and records the test as "Skipped" rather than Passed or Failed.
 *
 * <p>This source ships inside the suite because the bundled jtreg builds used
 * for this project detect {@code jtreg.SkippedException} in the agent but do
 * not place the class on the test compile classpath. Tests that need it add
 * {@code @library /lib} and throw it directly; jtreg compiles it on demand.
 */
public class SkippedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SkippedException() {
        super();
    }

    public SkippedException(String message) {
        super(message);
    }

    public SkippedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SkippedException(Throwable cause) {
        super(cause);
    }
}
