package p;

import api.Service;
import java.util.ServiceLoader;

public class Main {
    public static void main(String[] args) {
        boolean found = false;
        for (Service s : ServiceLoader.load(Service.class)) {
            if ("impl".equals(s.name())) found = true;
        }
        if (!found) throw new AssertionError("service provider not discoverable");
        System.out.println("PASS");
    }
}
