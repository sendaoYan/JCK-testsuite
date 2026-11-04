import java.io.File;
import java.io.FileWriter;

public class HaltChild {
    public static void main(String[] args) throws Exception {
        File started = new File(args[0]);
        File hookRan = new File(args[1]);
        FileWriter w = new FileWriter(started);
        w.write("started");
        w.close();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    FileWriter w2 = new FileWriter(hookRan);
                    w2.write("hook-ran");
                    w2.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }));
        Runtime.getRuntime().halt(0);
    }
}
