package mggdevit.utils;

import mggdevit.utils.util.JavaSourceAnalyzer;

/**
 *
 * @author mgg-dev-it
 */
public class Utils {

    public Utils(String[] args) {
        init(args);
    }

    private void init(String[] args) {
        JavaSourceAnalyzer jsa = new JavaSourceAnalyzer();
        for (int i = 0; i < args.length; i++) {
            jsa.addDirectoryOrFile(args[i]);
            jsa.analyze();
        }
    }

    public static void main(String[] args) {
        new Utils(args);
    }
}
