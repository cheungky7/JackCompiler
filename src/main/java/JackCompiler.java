import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JackCompiler {

    public static boolean checkIfIsDirectory(String path){
        File file = new File(path);
        boolean isDir= file.isDirectory();
        return isDir;

    }

    public static void searchFilesInDir(String pattern, File folder, List<String> result) {

        for (File f : folder.listFiles()) {

            if (f.isDirectory()) {
                searchFilesInDir(pattern, f, result);
            }

            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                    result.add(f.getAbsolutePath());
                }
            }

        }
    }

    public static void generateXMLfileForSingleFile(String sourceFileName) throws IOException {

        JackTokenizer tokenizer=new JackTokenizer(sourceFileName);
      //  TestJackTokenizer tester=new TestJackTokenizer(tokenizer);
        CompilationEngine compileEngine= new CompilationEngine(tokenizer);
        try{

            //tester.writeToken();
            compileEngine.CompileClass();

        }catch(Exception e){
            System.out.printf("Exception:%s\n",  e.getMessage());
            e.printStackTrace();
        } finally {
           // tester.close();
            compileEngine.close();
            tokenizer.close();
        }

    }

    public static void main(String[] args){

        String sourceFileName = args[0];
        System.out.printf("The input file is %s\n",sourceFileName);
        System.out.print("The input file is directory:"+checkIfIsDirectory(sourceFileName)+"\n");

        try {
            if (checkIfIsDirectory(sourceFileName) == true) {

                List<String> result = new ArrayList<>();
                File folder = new File(sourceFileName);
                searchFilesInDir(".*\\.jack", folder, result);
                for (String s : result) {
                    System.out.println("Read: " + s);
                    //writeToASMFile(CodeWriter coder,String sourceFileName,Integer lineNo);
                    generateXMLfileForSingleFile(s);
                }

            } else {
                generateXMLfileForSingleFile(sourceFileName);
            }
        }catch(Exception e){
            System.out.printf("Exception:%s\n",  e.getMessage());
            e.printStackTrace();
        }

    }

}
