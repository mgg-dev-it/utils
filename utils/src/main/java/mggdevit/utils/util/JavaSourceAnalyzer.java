package mggdevit.utils.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

/**
 *
 * @author mgg-dev-it
 */
public class JavaSourceAnalyzer {

    private ArrayList<String> alDirectoriesAndFiles;
    private ArrayList<String> alSourceFiles;
    private ArrayList<String> alTopFiles;
    private HashSet<String> hsImportedFiles;
    private ArrayList<String> alOtherFiles;
    private HashMap<String, String> hmFilesAndImportsOld;
    private HashMap<String, ArrayList<String>> hmFilesAndImports;

    public JavaSourceAnalyzer() {
        alDirectoriesAndFiles = new ArrayList<>();
        alSourceFiles = new ArrayList<>();
        alTopFiles = new ArrayList<>();
        hsImportedFiles = new HashSet<>();
        alOtherFiles = new ArrayList<>();
        hmFilesAndImportsOld = new HashMap<>();
        hmFilesAndImports = new HashMap<>();
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
            //System.out.println(alSourceFiles.get(i));
            processFile(alSourceFiles.get(i), hmFilesAndImports);
        }
        //System.out.println("-----");
        //System.out.println(hmFilesAndImports.size() + " top level Java source file was found!");
        //hmFilesAndImports.forEach((k, v) -> System.out.println("file: " + k + " import:" + v));
//        hmFilesAndImportsOld.forEach((k, v) -> addToTop(k, v));
        alTopFiles.sort(String::compareTo);
        hmFilesAndImports.forEach((k, v) -> alOtherFiles.add(k));
        alOtherFiles.sort(String::compareTo);
        System.out.println("-> " + alTopFiles.size() + " top level files (do not use import)");
        System.out.println("-----------------------------------------------------------------------");
//        alTopFiles.forEach(s -> System.out.println(s));
//        alTopFiles.forEach(s -> System.out.println(s + (isImportedFile(s) ? " imported" : "")));
        alTopFiles.forEach(s -> System.out.println(s + importInfo(s)));
//        alTopFiles.forEach(s -> hmFilesAndImportsOld.remove(s));
        System.out.println("");
        System.out.println("-> " + alOtherFiles.size() + " other files (use import)");
        System.out.println("-----------------------------------------------------------------------");
//        hmFilesAndImportsOld.forEach((k, v) -> System.out.println("file: " + k + " import:" + v));

        for (int i = 0; i < alOtherFiles.size(); i++) {
            ArrayList<String> als = hmFilesAndImports.get(alOtherFiles.get(i));
            als.sort(String::compareTo);
            String sImports = "";
            for (int j = 0; j < als.size(); j++) {
                sImports += (sImports.length() == 0 ? "" : ", ") + als.get(j) + (isTopLevelFile(als.get(j)) ? " top" : "");
//                System.out.println(alOtherFiles.get(i) + " " + als.get(j) + " " + (isTopLevelFile(als.get(j)) ? " top" : ""));
            }
            System.out.println(alOtherFiles.get(i) + " " + sImports);
        }
    }

    private String importInfo(String sName) {
        String sRetVal = "";
        for (int i = 0; i < alOtherFiles.size(); i++) {
            ArrayList<String> als = hmFilesAndImports.get(alOtherFiles.get(i));
            if (als.contains(sName)) {
                sRetVal += (sRetVal.length() == 0 ? "" : ", ") + alOtherFiles.get(i);
            }
        }
        return ((sRetVal.length() == 0 ? "" : " used by: [") + sRetVal + (sRetVal.length() == 0 ? "" : "]"));
    }

    private boolean isTopLevelFile(String sName) {
        return (alTopFiles.contains(sName));
    }

//    private boolean isImportedFile(String sName) {
//        return (hsImportedFiles.contains(sName));
//    }
//    private void addToTop(String sFileName, String sImport) {
//        if (sImport.length() == 0) {
//            alTopFiles.add(sFileName);
//        }
//    }
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

    private void processFile(String sSourceFileName, HashMap<String, ArrayList<String>> hmFilesAndImports) {
        String sFileName = sSourceFileName.substring(sSourceFileName.lastIndexOf(File.separator) + 1, sSourceFileName.length() - ".java".length());
        Vector<String> vLines = readTextFileIntoStringVector(sSourceFileName);
        String sPackage = "";
        int iImportCount = 0;
        ArrayList<String> alImportList = new ArrayList<>();
        for (int iLine = 0; iLine < vLines.size(); iLine++) {
            String sLine = vLines.elementAt(iLine).trim();
            if (sLine.startsWith("package ")) {
                sPackage = sLine.substring("package ".length(), sLine.length() - 1);
            }
            if (sLine.startsWith("import ")) {
                String sImport = sLine.substring("import ".length(), sLine.length() - 1);
                if (sImport.startsWith("hu")) {
//                    hmFilesAndImports.put(sPackage + "." + sFileName, sImport);
                    alImportList.add(sImport);
                    ++iImportCount;
                    hsImportedFiles.add(sImport);
                }
            }
        }
        if (iImportCount == 0) {
//            hmFilesAndImports.put(sPackage + "." + sFileName, "");
            alTopFiles.add(sPackage + "." + sFileName);
        } else {
            hmFilesAndImports.put(sPackage + "." + sFileName, alImportList);
        }
        //System.out.println(sSourceFileName + " " + sPackage + " " + sFileName);
    }

    public static Vector<String> readTextFileIntoStringVector(String sFileName) {
        File file = null;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        String sLine = null;
        Vector<String> vLines = new Vector<String>();

        try {
            file = new File(sFileName);
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            while ((sLine = br.readLine()) != null) {
                vLines.add(sLine);
            }
        } catch (FileNotFoundException fnfe) {
//            appInterface.handleError(fnfe);
        } catch (IOException ioe) {
//            appInterface.handleError(ioe);
        }
        return (vLines);
    }

}
