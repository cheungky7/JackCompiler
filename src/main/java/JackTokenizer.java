import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

enum tokenType {
    KEYWORD,
    SYMBOL,
    IDENTIFIER,
    INT_CONST,
    STRING_CONST
}

public class JackTokenizer {

    private BufferedReader m_reade;
    private boolean m_haveMoreCmd;
    private String m_current_instr;
    private boolean m_isComment;
    private tokenType m_TokenType;
    private String m_KeyWord;
    private char m_Symbol;
    private String m_Identifier;
    private int m_IntVal;
    private String m_StringVal;
    private String m_SrcFileName;
    private String m_SrcFilePath;

    public JackTokenizer(String filename) throws IOException {
        File file=new File(filename);
        m_SrcFilePath=file.getParent();
        m_reade = new BufferedReader(new FileReader(file));
        m_isComment=false;
        m_SrcFileName=JA_Uitilty.extractFilename(filename);
        m_haveMoreCmd = true;
    }

    public String getSrcPath(){
        return m_SrcFilePath;
    }

    public String getSrcFileName(){
        return m_SrcFileName;
    }

    public tokenType getTokenType(){
        return m_TokenType;
    }

    public String getKeyWord(){
        return m_KeyWord;
    }

    public String getIdentifier(){
        return m_Identifier;
    }

    public int getIntVal(){
        return m_IntVal;
    }

    public String getStringVal(){
        return m_StringVal;
    }

    public char getSymbol(){
        return m_Symbol;
    }

    public void advance() throws IOException{

        while(true) {

            if(m_current_instr == null || m_current_instr.length()==0) {
                m_current_instr = m_reade.readLine(); // read next line if current instr is null
            }

            if (m_current_instr == null) {
                //  not more command if can not get more line from file
                m_haveMoreCmd = false;
                break;
            }

            int firstPosOfCommentType1=m_current_instr.indexOf("//");
            int firstPosOfCommentType2=m_current_instr.indexOf("/*");
            int firstPosOfCommentType3=m_current_instr.indexOf("*/");

            String instrRemoveComment=m_current_instr;
            if(firstPosOfCommentType1!=-1) {
                instrRemoveComment = m_current_instr.substring(0, firstPosOfCommentType1);
            }

            if(firstPosOfCommentType2!=-1) {
                instrRemoveComment = m_current_instr.substring(0, firstPosOfCommentType2);
                m_isComment=true;
            }

            //bug may need to be fixed later, if /* */ instead a line , processing will be wrong

            if(firstPosOfCommentType3!=-1) {
                instrRemoveComment = m_current_instr.substring(firstPosOfCommentType3+"*/".length(), m_current_instr.length());
                m_isComment=false;
            }

            if(m_isComment==true) {
                m_current_instr=null;
                continue;
            }

           // m_current_instr=instrRemoveComment.replaceAll("\\s+", ""); //remove all the space
            m_current_instr=instrRemoveComment.trim();
            int firstTokenPosition=m_current_instr.length()+3;
            String tokenFound=null;

            //
            int [] pos={-1};

            String tokenReturn=findIdentifier(m_current_instr,pos);
            if(tokenReturn !=null && pos[0]<firstTokenPosition){
                m_TokenType=tokenType.IDENTIFIER;
                m_Identifier=tokenReturn;
                tokenFound=tokenReturn;
                firstTokenPosition=pos[0];
            }

            tokenReturn=findKeyWord(m_current_instr,pos);
            if(tokenReturn !=null && pos[0]<firstTokenPosition ){
                m_TokenType=tokenType.KEYWORD;
                m_KeyWord=tokenReturn;
                tokenFound=tokenReturn;
                firstTokenPosition=pos[0];
            }

            tokenReturn=findSymbol(m_current_instr,pos);
            if(tokenReturn !=null && pos[0]<firstTokenPosition){
                m_TokenType=tokenType.SYMBOL;
                m_Symbol=tokenReturn.toCharArray()[0];
                tokenFound=tokenReturn;
                firstTokenPosition=pos[0];
            }

            tokenReturn=findIntegerConst(m_current_instr,pos);
            if(tokenReturn !=null && pos[0]<firstTokenPosition){
                m_TokenType=tokenType.INT_CONST;
                m_IntVal=Integer.parseInt(tokenReturn);
                tokenFound=tokenReturn;
                firstTokenPosition=pos[0];
            }

            tokenReturn=findStringConst(m_current_instr,pos);
            if(tokenReturn !=null && pos[0]<firstTokenPosition){
                m_TokenType=tokenType.STRING_CONST;
                m_StringVal=tokenReturn;
                tokenFound=tokenReturn;
                firstTokenPosition=pos[0];
            }





            if(tokenFound !=null) {
                if(m_TokenType !=tokenType.STRING_CONST) {
                    m_current_instr = m_current_instr.substring(firstTokenPosition + tokenFound.length(), m_current_instr.length());
                }
                else {
                    m_current_instr = m_current_instr.substring(firstTokenPosition + tokenFound.length()+2, m_current_instr.length());
                    // include "" if it is string constant
                }
                break;
            }

        }
    }

    private String findKeyWord(String instrInput,int[] pos){
       // String keyword=null;
        // class is the top most keyword, so exact it first
        String returnKeyword=null;
        int positionReturn=-1;
        int firstKeyWordPosition=instrInput.length()+3;
        pos[0]=-1;

        if((positionReturn=instrInput.indexOf("class")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "class";
        }
         // the below must be in order
        if((positionReturn=instrInput.indexOf("constructor")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "constructor";
        }

        if((positionReturn=instrInput.indexOf("function")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "function";
        }

        if((positionReturn=instrInput.indexOf("method")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "method";
        }

        if((positionReturn=instrInput.indexOf("field")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "field";
        }

        if((positionReturn=instrInput.indexOf("static")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "static";
        }

        if((positionReturn=instrInput.indexOf("var")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "var";
        }
        // Primitive Data type extract secondly
        if((positionReturn=instrInput.indexOf("int")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "int";
        }
        if((positionReturn=instrInput.indexOf("char")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "char";
        }
        if((positionReturn=instrInput.indexOf("boolean")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "boolean";
        }
        if((positionReturn=instrInput.indexOf("void")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "void";
        }
        // const keyword extract thirdly
        if((positionReturn=instrInput.indexOf("true")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "true";
        }
        if((positionReturn=instrInput.indexOf("false")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "false";
        }
        if((positionReturn=instrInput.indexOf("null")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "null";
        }
        // const assigement extract thirdly
        if((positionReturn=instrInput.indexOf("this")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "this";
        }
        if((positionReturn=instrInput.indexOf("let")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "let";
        }
        if((positionReturn=instrInput.indexOf("do")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "do";
        }
        // logic keyword extract thirdly
        if((positionReturn=instrInput.indexOf("if")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "if";
        }
        if((positionReturn=instrInput.indexOf("else")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "else";
        }
        if((positionReturn=instrInput.indexOf("while")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "while";
        }
        if((positionReturn=instrInput.indexOf("return")) !=-1 && (positionReturn<firstKeyWordPosition)){
            pos[0]=positionReturn;
            firstKeyWordPosition=positionReturn;
            returnKeyword= "return";
        }

        return returnKeyword;
    }

    private String findSymbol(String instrInput,int[] pos){

        String returnSymbol=null;
        int positionReturn=-1;
        int firstSymbolPosition=instrInput.length()+3;
        pos[0]=-1;

        if((positionReturn=instrInput.indexOf("(")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "(";
        }

        if((positionReturn=instrInput.indexOf(")") ) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= ")";
        }

        if((positionReturn=instrInput.indexOf("{")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "{";
        }

        if((positionReturn=instrInput.indexOf("}")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "}";
        }

        if((positionReturn=instrInput.indexOf("[")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "[";
        }

        if((positionReturn=instrInput.indexOf("]")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "]";
        }

        if((positionReturn=instrInput.indexOf(".")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= ".";
        }

        if((positionReturn=instrInput.indexOf(",")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= ",";
        }

        if((positionReturn=instrInput.indexOf(";")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= ";";
        }

        if((positionReturn=instrInput.indexOf("+")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "+";
        }

        if((positionReturn=instrInput.indexOf("*")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "*";
        }

        if((positionReturn=instrInput.indexOf("/")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "/";
        }

        if((positionReturn=instrInput.indexOf("&")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "&";
        }

        if((positionReturn=instrInput.indexOf("|")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "|";
        }

        if((positionReturn=instrInput.indexOf("<")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "<";
        }

        if((positionReturn=instrInput.indexOf(">")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= ">";
        }

        if((positionReturn=instrInput.indexOf("=")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "=";
        }

        if((positionReturn=instrInput.indexOf("-")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "-";
        }


        if((positionReturn=instrInput.indexOf("~")) !=-1 && (positionReturn<firstSymbolPosition)){
            pos[0]=positionReturn;
            firstSymbolPosition=positionReturn;
            returnSymbol= "~";
        }

        return returnSymbol;

    }

    private String findIntegerConst(String instrInput,int[] pos){

        boolean foundDigits=false;
        pos[0]=-1;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < instrInput.length(); i++) {
            char c = instrInput.charAt(i);
            if (Character.isDigit(c)) {
                if( pos[0]==-1){
                    pos[0]=i;
                }
                foundDigits=true;
                builder.append(c);
            } else {
                if(foundDigits==true){
                    break;
                }
            }
        }
        if(foundDigits==true) {

            return builder.toString();
        }
        return null;
    }

    private String findStringConst(String instrInput,int[] pos){

        StringBuilder builder = new StringBuilder();
        boolean isCloseQuotationMarkFound=false;
        pos[0]=-1;

        if(instrInput !=null && instrInput.length()>1){
            if(instrInput.charAt(0)!='"'){
                return null;
            }

            for(int i=1; i<instrInput.length();i++){
                if(instrInput.charAt(i) !='"') {
                    builder.append(instrInput.charAt(i));
                } else{
                    isCloseQuotationMarkFound=true;
                    break;
                }

            }
        }

        if(isCloseQuotationMarkFound==true){

            pos[0]=0;
            return builder.toString();
        }

        return null;
    }

    private String findIdentifier(String instrInput,int[] pos){

        int [] symbolpos={-1};

        if(instrInput !=null && instrInput.length()>0) {

            if (Character.isLetter(instrInput.charAt(0)) != true && instrInput.charAt(0) != '-') {
                return null;
            }




            symbolpos[0]=0;
            pos[0]=0;
            //check if it is a keyword


            int positionOfSpace=instrInput.indexOf(" ");
            int positionOfStringConst=instrInput.indexOf('\"');
            String symbolFound=findSymbol(instrInput, symbolpos);

            ArrayList<Integer> positionList = new ArrayList<Integer>();

            if(positionOfStringConst !=-1){
                positionList.add(positionOfStringConst);
            }

            if(positionOfSpace !=-1){
                positionList.add(positionOfSpace);
            }

            if(symbolpos[0]!=-1){
                positionList.add(symbolpos[0]);
            }

            Collections.sort(positionList);
            if(positionList.size()>0){
                String indentifierFound= instrInput.substring(0, positionList.get(0));
                int [] keywordpos={-1};
                String keyWordFound=findKeyWord(instrInput,keywordpos);
                if(keyWordFound !=null && keyWordFound.equals(indentifierFound)){
                    // if indentifierFound is a keyword
                    return null;
                }

                if(indentifierFound.equals("")){
                    return null; // identifier is not found
                }

                return indentifierFound;
            }

            return instrInput;
        }

        return null;
    }




    public boolean hasMoreCommands(){
        return this.m_haveMoreCmd;
    }

    public void close() throws IOException{
        m_reade.close();
    }



}
