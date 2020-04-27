import java.io.File;

public class JA_Uitilty {

    public static String extractFilename(String path){
        //String fileName=null;
        File f = new File(path);
        String fileName=f.getName();
        int posOfExtension = fileName.indexOf(".jack");
        String fileNameWithoutExtension=fileName.substring(0, posOfExtension);
        return fileNameWithoutExtension;
    }

    public static String handleSpecialChar(char input){

        if(input=='<'){
            return "&lt;";
        }

        if(input=='>'){
            return "&gt;";
        }

        if(input=='"'){
            return "&quot;";
        }

        if(input=='&'){
            return "&amp;";
        }



        return String.valueOf(input);
    }

}
