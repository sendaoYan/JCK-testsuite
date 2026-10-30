import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Child {
    public static void main(String[] args) throws Exception {
        File f = new File(args[0]);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (FileWriter w = new FileWriter(f)) {
                w.write("hook-ran");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        System.exit(0);
    }
}
