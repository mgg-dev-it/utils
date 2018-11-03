package mggdevit.utils.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author mgg-dev-it
 */
public class JavaSourceAnalyzer {

    private ArrayList<String> alDirectoriesAndFiles;
    private ArrayList<String> alSourceFiles;
    private HashMap<String,String> hmFilesAndImports;

    public JavaSourceAnalyzer() {
        alDirectoriesAndFiles = new ArrayList<>();
        alSourceFiles = new ArrayList<>();
    }

    public void addDirectoryOrFile(String sFullName) {
        alDirectoriesAndFiles.add(sFullName);
    }

    public void analyze() {
        if (alDirectoriesAndFiles.size() < 1) {
            System.out.println("Neither directory, nor file given!");
            return;
        }
        for (int i = 0; i < alDirectoriesAndFiles.size(); i++) {
            collectSourceFiles(alDirectoriesAndFiles.get(i), alSourceFiles);
        }
        if (alSourceFiles.size() == 0) {
            System.out.println("No Java source file was found!");
            return;
        }
        System.out.println(alSourceFiles.size() + " Java source file was found!");
        //alSourceFiles.sort((c1,c2) ->(c1.compareTo(c2)));
        alSourceFiles.sort(String::compareTo);
        for (int i = 0; i < alSourceFiles.size(); i++) {
            System.out.println(alSourceFiles.get(i));
        }
    }

    private void collectSourceFiles(String sFullName, ArrayList<String> alSourceFiles) {
        File f = new File(sFullName);
        if (!f.exists()) {
            System.out.println(sFullName + " does not exist!");
            return;
        }
        if (f.isFile()) {
            if (f.getAbsolutePath().endsWith(".java")) {
                alSourceFiles.add(f.getAbsolutePath());
            } else {
                System.out.println(sFullName + " is not a Java source file!");
            }
        }
        if (f.isDirectory()) {
            Boolean booleanFound = Boolean.FALSE;
            collectSourceDirectories(f, alSourceFiles, booleanFound);
            if (!booleanFound) {
                System.out.println(sFullName + " does not contain Java source file!");
            }
        }
    }

    private void collectSourceDirectories(File fDir, ArrayList<String> alSourceFiles, Boolean booleanFound) {
        File[] fl = fDir.listFiles();
        for (int i = 0; i < fl.length; i++) {
            if (fl[i].isDirectory()) {
                collectSourceDirectories(fl[i], alSourceFiles, booleanFound);
            }
            if (fl[i].isFile()) {
                if (fl[i].getAbsolutePath().endsWith(".java")) {
                    alSourceFiles.add(fl[i].getAbsolutePath());
                    booleanFound = Boolean.TRUE;
                }
            }
        }
    }

}
