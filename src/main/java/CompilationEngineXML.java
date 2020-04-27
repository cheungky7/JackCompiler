import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngineXML {

    private JackTokenizer m_Tokenizer;
    private FileWriter m_FileWriter;
    private String m_outfileName;
    private BufferedWriter m_writer;

    public CompilationEngineXML(JackTokenizer tokenizer) throws IOException {

        m_Tokenizer=tokenizer;
        m_outfileName=tokenizer.getSrcPath()+"/"+tokenizer.getSrcFileName()+".xml";
        m_FileWriter=new FileWriter(m_outfileName, false);
        m_writer= new BufferedWriter(m_FileWriter);

    }

    public void CompileClass() throws Exception {

        m_writer.write("<class>\n");

        m_Tokenizer.advance();
        if(m_Tokenizer.getTokenType() ==tokenType.KEYWORD &&
                m_Tokenizer.getKeyWord().equals("class") ==true){
            writeTokenInXML(m_Tokenizer,m_writer); // write class
            m_Tokenizer.advance();
            writeTokenInXML(m_Tokenizer,m_writer); // write class name
            m_Tokenizer.advance();
            writeTokenInXML(m_Tokenizer,m_writer); // write {
            m_Tokenizer.advance();

            while(m_Tokenizer.getTokenType()==tokenType.KEYWORD) {
                if(m_Tokenizer.getKeyWord().equals("static")||m_Tokenizer.getKeyWord().equals("field")) {
                    CompileClassVarDec();
                }else{
                    CompileSubroutine();
                }
            }

        }else {
            throw new Exception("invalid token");
        }
        m_Tokenizer.advance();
        writeTokenInXML(m_Tokenizer,m_writer); // write }
        m_writer.write("</class>\n");
    }

    public void CompileClassVarDec() throws Exception {

        m_writer.write("<classVarDec>\n");

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance();  // write field or static

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance();  // write primitive data type or class type

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance();  // write var name

        while(m_Tokenizer.getTokenType()==tokenType.SYMBOL && m_Tokenizer.getSymbol()==',' ){

            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance();  // write ","

            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance();  // write identifier

        }

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance();  // write ";"



        //m_Tokenizer.advance();
        m_writer.write("</classVarDec>\n");
    }

    public void CompileSubroutine() throws IOException {

        m_writer.write(" <subroutineDec>\n");

        // write 4 token
        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance();

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance();

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance();

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance();

        compileParameterList();
        compileRoutineBody();
        // end
        m_writer.write(" </subroutineDec>\n");
    }

    public void compileParameterList() throws IOException {
        m_writer.write("<parameterList>\n");
        while(true){

            if (m_Tokenizer.getTokenType()==tokenType.SYMBOL && (m_Tokenizer.getSymbol() ==')' )){
                break; //loop until it reach keyword which is not field or static
            }

            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance();
        }


        m_writer.write("</parameterList>\n");
        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance();
    }

    public void compileRoutineBody() throws IOException {

        m_writer.write("<subroutineBody>\n");
        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write {
        while(true){
            if (m_Tokenizer.getTokenType()==tokenType.KEYWORD && (m_Tokenizer.getKeyWord().equals("var") !=true )){
                break; //loop until it reach keyword which is not field or static
            }
            compileVarDec();
        }
        compileStatement();

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write }

        m_writer.write("</subroutineBody>\n");
    }



    public void CompileExpression() throws IOException {

        m_writer.write("<expression>\n");
        CompileTerm();

        while(m_Tokenizer.getTokenType()==tokenType.SYMBOL && (m_Tokenizer.getSymbol()=='+' ||
                m_Tokenizer.getSymbol()=='-' || m_Tokenizer.getSymbol()=='*' || m_Tokenizer.getSymbol()=='/'||
                 m_Tokenizer.getSymbol()=='&' || m_Tokenizer.getSymbol()=='|' || m_Tokenizer.getSymbol()=='<'
                || m_Tokenizer.getSymbol()=='>' ||  m_Tokenizer.getSymbol()=='=')){

            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance(); // write Symbol
            CompileTerm();
        }
        m_writer.write("</expression>\n");
    }

    public void CompileTerm() throws IOException {
        m_writer.write("<term>\n");

       tokenType type=m_Tokenizer.getTokenType();
       if(type==tokenType.INT_CONST){
           writeTokenInXML(m_Tokenizer,m_writer);
           m_Tokenizer.advance();

       } else if(type==tokenType.STRING_CONST){
           writeTokenInXML(m_Tokenizer,m_writer);
           m_Tokenizer.advance();

       } else if(type==tokenType.KEYWORD){
           writeTokenInXML(m_Tokenizer,m_writer);
           m_Tokenizer.advance();
       } else if(type==tokenType.SYMBOL){
           if(m_Tokenizer.getSymbol()=='('){
               writeTokenInXML(m_Tokenizer,m_writer); //write '('
               m_Tokenizer.advance();
               CompileExpression();
               writeTokenInXML(m_Tokenizer,m_writer);
               m_Tokenizer.advance(); // write ')'
           } else if(m_Tokenizer.getSymbol()=='-' || m_Tokenizer.getSymbol()=='~'){
               writeTokenInXML(m_Tokenizer,m_writer); //write '('
               m_Tokenizer.advance();
               CompileTerm();
           }
       } else if(type==tokenType.IDENTIFIER){
           writeTokenInXML(m_Tokenizer,m_writer);
           m_Tokenizer.advance();
           if(m_Tokenizer.getSymbol()=='.'){ // subroutine call
               writeTokenInXML(m_Tokenizer,m_writer);
               m_Tokenizer.advance(); // write "."
               writeTokenInXML(m_Tokenizer,m_writer);
               m_Tokenizer.advance(); // write subroutine name
               writeTokenInXML(m_Tokenizer,m_writer);
               m_Tokenizer.advance(); // write "("
               CompileExpressionList();
               writeTokenInXML(m_Tokenizer,m_writer);
               m_Tokenizer.advance(); //  write ")"

           } else if(m_Tokenizer.getSymbol()=='('){
               writeTokenInXML(m_Tokenizer,m_writer);
               m_Tokenizer.advance(); // write "("
               CompileExpressionList();
               writeTokenInXML(m_Tokenizer,m_writer);
               m_Tokenizer.advance(); // write ")"

           } else if(m_Tokenizer.getSymbol()=='['){
               writeTokenInXML(m_Tokenizer,m_writer);
               m_Tokenizer.advance(); // write "["
             //  CompileExpressionList();
               CompileExpression();
               writeTokenInXML(m_Tokenizer,m_writer);
               m_Tokenizer.advance(); // write "]"

           }
       }


        m_writer.write("</term>\n");

    }

    public void CompileExpressionList() throws IOException {

        m_writer.write("<expressionList>\n");
        //while(m_Tokenizer.getTokenType() !=tokenType.SYMBOL ||  (m_Tokenizer.getSymbol() !=')')){
        while(true){
            if(m_Tokenizer.getTokenType() ==tokenType.SYMBOL && (m_Tokenizer.getSymbol() ==')' )){
                break;
            }
            CompileExpression();
            while(m_Tokenizer.getTokenType()==tokenType.SYMBOL && (m_Tokenizer.getSymbol()==',')){
                writeTokenInXML(m_Tokenizer,m_writer);
                m_Tokenizer.advance();
                CompileExpression();
            }
        }


        m_writer.write("</expressionList>\n");

    }

    public void compileDo() throws IOException {
        m_writer.write("<doStatement>\n");

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write do

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write the class name

        if(m_Tokenizer.getSymbol()=='.'){
            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance(); // write "."

            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance(); // write the method or function name
        }

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write "("

        //compileParameterList();
        CompileExpressionList();

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write ")"

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write ";"

        m_writer.write("</doStatement>\n");

    }

    public void compileLet() throws IOException {

        m_writer.write("<letStatement>\n");
        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write let

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write varName
        if(m_Tokenizer.getTokenType()==tokenType.SYMBOL && m_Tokenizer.getSymbol()=='[' ){

            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance(); // write "["

            // write expression
            CompileExpression();

            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance(); // write "]"
        }

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write "="
        CompileExpression();
        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write ";"
        m_writer.write("</letStatement>\n");
    }

    public void compileWhile() throws IOException {

        m_writer.write("<whileStatement>\n");

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write while

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write "("

        CompileExpression();

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write ")"


        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write {

        compileStatement();

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write }

       // writeTokenInXML(m_Tokenizer,m_writer);
      //  m_Tokenizer.advance(); // write ";"



        m_writer.write("</whileStatement>\n");

    }

    public void compileReturn() throws IOException {

        m_writer.write("<returnStatement>\n");
        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write return

        if(m_Tokenizer.getTokenType() !=tokenType.SYMBOL){
            CompileExpression();
        }

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write ";"

        m_writer.write("</returnStatement>\n");

    }

    public void compileIf() throws IOException {

        m_writer.write("<ifStatement>\n");

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write if

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write "("

        CompileExpression();

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write ")"


        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write {

        compileStatement();

        writeTokenInXML(m_Tokenizer,m_writer);
        m_Tokenizer.advance(); // write }

        if(m_Tokenizer.getTokenType()==tokenType.KEYWORD && m_Tokenizer.getKeyWord().equals("else")==true){
            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance(); // write else
            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance(); // write {

            compileStatement();

            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance(); // write }
        }

       // writeTokenInXML(m_Tokenizer,m_writer);
       // m_Tokenizer.advance(); // write ";"



        m_writer.write("</ifStatement>\n");

    }

    public void compileStatement() throws IOException {

        m_writer.write("<statements>\n");
        while(true){

            if(m_Tokenizer.getTokenType()==tokenType.SYMBOL && m_Tokenizer.getSymbol()=='}'){
                break;
            }

            if(m_Tokenizer.getTokenType()==tokenType.KEYWORD && m_Tokenizer.getKeyWord().equals("let")==true){
                compileLet();
                continue;
            }

            if(m_Tokenizer.getTokenType()==tokenType.KEYWORD && m_Tokenizer.getKeyWord().equals("do")==true){
                compileDo();
                continue;
            }

            if(m_Tokenizer.getTokenType()==tokenType.KEYWORD && m_Tokenizer.getKeyWord().equals("while")==true){
                compileWhile();
                continue;
            }

            if(m_Tokenizer.getTokenType()==tokenType.KEYWORD && m_Tokenizer.getKeyWord().equals("return")==true){
                compileReturn();
                continue;
            }

            if(m_Tokenizer.getTokenType()==tokenType.KEYWORD && m_Tokenizer.getKeyWord().equals("if")==true){
                compileIf();
                continue;
            }

        }

        m_writer.write("</statements>\n");

    }

    public void compileVarDec() throws IOException {
        m_writer.write("<varDec>\n");
        while(true){

            if (m_Tokenizer.getTokenType()==tokenType.SYMBOL && (m_Tokenizer.getSymbol() ==';' )){

                writeTokenInXML(m_Tokenizer,m_writer);
                m_Tokenizer.advance(); // write ;

                break; //loop until it reach keyword which is not field or static
            }

            writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance();
        }
        m_writer.write("</varDec>\n");

    }


    public static void writeTokenInXML(JackTokenizer tokenizer,BufferedWriter writer) throws IOException {

        tokenType type=tokenizer.getTokenType();

        if(type==tokenType.KEYWORD){
            String keyword=tokenizer.getKeyWord();
            writer.write("<keyword>"+keyword+"</keyword>\n");
        }

        if(type==tokenType.IDENTIFIER){
            String identifier=tokenizer.getIdentifier();
            writer.write("<identifier>"+identifier+"</identifier>\n");
        }

        if(type==tokenType.SYMBOL){
            String symbol=JA_Uitilty.handleSpecialChar(tokenizer.getSymbol());
            writer.write("<symbol>"+symbol+"</symbol>\n");
        }

        if(type==tokenType.INT_CONST){
            int value=tokenizer.getIntVal();
            writer.write("<integerConstant>"+String.valueOf(value)+"</integerConstant>\n");
        }

        if(type==tokenType.STRING_CONST){
            String stringconst=tokenizer.getStringVal();
            writer.write("<stringConstant>"+stringconst+"</stringConstant>\n");
        }

    }

    public void close() throws IOException {
        m_writer.close();
    }




}
