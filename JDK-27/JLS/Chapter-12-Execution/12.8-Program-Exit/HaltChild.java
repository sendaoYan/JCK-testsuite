import java.io.File;
import java.io.FileWriter;

public class HaltChild {
    public static void main(String[] args) throws Exception {
        File started = new File(args[0]);
        File hookRan = new File(args[1]);
        try (FileWriter w = new FileWriter(started)) {
            w.write("started");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (FileWriter w = new FileWriter(hookRan)) {
                w.write("hook-ran");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        Runtime.getRuntime().halt(0);
    }
}
