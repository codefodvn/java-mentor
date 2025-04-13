package codefod.com.iodemo.filebasic;

import java.io.File;

public class ListFromFolder {

    public static void main(String[] args) {
        File folder = new File("/Users/hagiakinh/Data/Code/Java/java-mentor");

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            assert files != null;
            for (File file : files) {
                System.out.println(file.getName());
            }
        }
    }
}



