import java.io.File;

public class hello {
    public static void main(String[] args) {
        System.out.println("Hello World");
        String path = System.getProperty("user.dir");
        System.out.println("Working Directory = " + path);

        File apath = new File(".");
        System.out.println(apath.getAbsolutePath());
    }
}
