import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {

    private JackTokenizer m_Tokenizer;
    private String m_outfileName;
    private String m_className;
    private SymbolTable m_sysmbolTable;
    private String m_functionName;
    private String m_functionReturnType;
    private String m_functionType;
    private VMWriter m_VMWriter;
    private int ifCounter;
    private int whileCounter;

    private static final String IfLabel="IfL";
    private static final String whileLabel="whileL";

    public CompilationEngine(JackTokenizer tokenizer) throws IOException {

        m_Tokenizer=tokenizer;
        m_outfileName=tokenizer.getSrcPath()+"/"+tokenizer.getSrcFileName()+".vm";
        m_sysmbolTable=new SymbolTable();
        m_VMWriter=new VMWriter(m_outfileName);
        ifCounter=0;
        whileCounter=0;

    }

    public void CompileClass() throws Exception {

        ifCounter=0;
        whileCounter=0;
        m_Tokenizer.advance();
        if(m_Tokenizer.getTokenType() ==tokenType.KEYWORD &&
                m_Tokenizer.getKeyWord().equals("class") ==true){
            m_Tokenizer.advance();

            m_className=m_Tokenizer.getIdentifier();// get class name
            m_Tokenizer.advance(); // write {
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
    }

    public void CompileClassVarDec() throws Exception {

        Kind varkind=Symbol.convertKeyWordToKind(m_Tokenizer.getKeyWord());
        m_Tokenizer.advance();  // write field or static

        String varType=m_Tokenizer.getIdentifier();
        m_Tokenizer.advance();  // write primitive data type or class type

        String varName=m_Tokenizer.getIdentifier();
        m_Tokenizer.advance();  // write var name

        m_sysmbolTable.define(varName,varType,varkind);

        while(m_Tokenizer.getTokenType()==tokenType.SYMBOL && m_Tokenizer.getSymbol()==',' ){

            //writeTokenInXML(m_Tokenizer,m_writer);
            m_Tokenizer.advance();  // write var

           //writeTokenInXML(m_Tokenizer,m_writer);
            varName=m_Tokenizer.getIdentifier();
            m_Tokenizer.advance();  // write identifier

            m_sysmbolTable.define(varName,varType,varkind);
        }
        m_Tokenizer.advance();  // write ";"
    }

    public void CompileSubroutine() throws Exception {

        m_sysmbolTable.StartSubRoutine();
        m_functionType=m_Tokenizer.getKeyWord();
        m_Tokenizer.advance(); // write constructor|function|method

        if(m_Tokenizer.getTokenType()==tokenType.KEYWORD){
            m_functionReturnType=m_Tokenizer.getKeyWord();
        } else {
            m_functionReturnType=m_Tokenizer.getIdentifier();
        }

        m_Tokenizer.advance(); // write return type
        m_functionName=m_Tokenizer.getIdentifier();
        m_Tokenizer.advance(); // write subroutineName
        m_Tokenizer.advance(); // write (
        compileParameterList();
        compileRoutineBody();
        m_sysmbolTable.LeaveSubRoutine();
        // end
    }

    public void compileParameterList() throws IOException {
       // m_writer.write("<parameterList>\n");
        while(true){

            if (m_Tokenizer.getTokenType()==tokenType.SYMBOL && (m_Tokenizer.getSymbol() ==')' )){
                break; //loop until it reach keyword which is not field or static
            }

            //writeTokenInXML(m_Tokenizer,m_writer);
            // get argreement type
            String argType=null;

            if(m_Tokenizer.getTokenType()==tokenType.KEYWORD) {
                argType=m_Tokenizer.getKeyWord();
            }else{
                argType=m_Tokenizer.getIdentifier();
            }
            m_Tokenizer.advance();

            String argName=m_Tokenizer.getIdentifier();
            m_Tokenizer.advance();

            if (m_Tokenizer.getTokenType()==tokenType.SYMBOL && (m_Tokenizer.getSymbol() ==',' )){
                m_Tokenizer.advance();
            }

            m_sysmbolTable.define(argName,argType,Kind.ARG);
        }
        m_Tokenizer.advance(); // write ")"
    }

    public void compileRoutineBody() throws Exception {


        m_Tokenizer.advance(); // write {
        while(true){
            if (m_Tokenizer.getTokenType()==tokenType.KEYWORD && (m_Tokenizer.getKeyWord().equals("var") !=true )){
                break; //loop until it reach keyword which is not field or static
            }
            compileVarDec();
        }

        String functionName=m_className+"."+m_functionName;
        int nOfLocalVars=m_sysmbolTable.VarCount(Kind.VAR);
        m_VMWriter.writeFunction(functionName,nOfLocalVars);
        if(m_functionType.equals("constructor")==true){

            int nOfField=m_sysmbolTable.VarCount(Kind.FIELD);
            m_VMWriter.writePush(Segment.CONST,nOfField); // input number of field to memory alloc function
            m_VMWriter.writeCall("Memory.alloc",1);
            m_VMWriter.writePop(Segment.POINTER,0); // pass the object itself as 1st argument
        }

        if(m_functionType.equals("method")==true) {
            m_VMWriter.writePush(Segment.ARG,0); // push the input 1 (object) from argument to stack
            m_VMWriter.writePop(Segment.POINTER,0); // pass the object itself as 1st argument
        }

        compileStatement();
        m_Tokenizer.advance(); // write }
    }



    public void CompileExpression() throws Exception {

        CompileTerm(); // compile opand

        while(m_Tokenizer.getTokenType()==tokenType.SYMBOL && (m_Tokenizer.getSymbol()=='+' ||
                m_Tokenizer.getSymbol()=='-' || m_Tokenizer.getSymbol()=='*' || m_Tokenizer.getSymbol()=='/'||
                m_Tokenizer.getSymbol()=='&' || m_Tokenizer.getSymbol()=='|' || m_Tokenizer.getSymbol()=='<'
                || m_Tokenizer.getSymbol()=='>' ||  m_Tokenizer.getSymbol()=='=')){

            char operator=m_Tokenizer.getSymbol();
            m_Tokenizer.advance(); // write Symbol
            CompileTerm(); // compile opand

            if(operator=='+'){
                m_VMWriter.writeArithmetic(Command.add);
            }

            if(operator=='-'){
                m_VMWriter.writeArithmetic(Command.sub);
            }

            if(operator=='*'){
                m_VMWriter.writeCall("Math.multiply",2);
            }

            if(operator=='/'){
                m_VMWriter.writeCall("Math.divide",2);
            }

            if(operator=='&'){
                m_VMWriter.writeArithmetic(Command.and);
            }

            if(operator=='|'){
                m_VMWriter.writeArithmetic(Command.or);
            }

            if(operator=='<'){
                m_VMWriter.writeArithmetic(Command.lt);
            }

            if(operator=='>'){
                m_VMWriter.writeArithmetic(Command.gt);
            }

            if(operator=='='){
                m_VMWriter.writeArithmetic(Command.eq);
            }
        }
    }

    public void CompileStringConstant(String stringConstant) throws IOException {
        int stringLength=stringConstant.length();
        m_VMWriter.writePush(Segment.CONST,stringLength);
        m_VMWriter.writeCall("String.new",1);
        for(int i=0; i<stringLength; i++){
            int charAtString=stringConstant.charAt(i);
            m_VMWriter.writePush(Segment.CONST,charAtString);
            m_VMWriter.writeCall("String.appendChar",2);
        }
    }

    public void CompileTerm() throws Exception {

        tokenType type=m_Tokenizer.getTokenType();
        if(type==tokenType.INT_CONST){
           m_VMWriter.writePush(Segment.CONST,m_Tokenizer.getIntVal());
           m_Tokenizer.advance();
        } else if(type==tokenType.STRING_CONST){

            String stringConstant=m_Tokenizer.getStringVal();
            CompileStringConstant(stringConstant);
            m_Tokenizer.advance();
        } else if(type==tokenType.KEYWORD){

            String keyword=m_Tokenizer.getKeyWord();
            if(keyword.equals("true")){
                m_VMWriter.writePush(Segment.CONST,0);
                m_VMWriter.writeArithmetic(Command.not);
            }

            if(keyword.equals("false")){
                m_VMWriter.writePush(Segment.CONST,0);
            }

            if(keyword.equals("null")){
                m_VMWriter.writePush(Segment.CONST,0);
            }

            if(keyword.equals("this")){
                m_VMWriter.writePush(Segment.POINTER,0);
            }

            m_Tokenizer.advance();
        } else if(type==tokenType.SYMBOL){
            if(m_Tokenizer.getSymbol()=='('){
              //  writeTokenInXML(m_Tokenizer,m_writer); //write '('
                m_Tokenizer.advance();
                CompileExpression();
                m_Tokenizer.advance(); // write ')'
            } else if(m_Tokenizer.getSymbol()=='-'){
                m_Tokenizer.advance();
                CompileTerm();
                m_VMWriter.writeArithmetic(Command.neg);
            }else if ( m_Tokenizer.getSymbol()=='~'){
                m_Tokenizer.advance();
                CompileTerm();
                m_VMWriter.writeArithmetic(Command.not);
            }
        } else if(type==tokenType.IDENTIFIER){

            String calleeName1=m_Tokenizer.getIdentifier();
            m_Tokenizer.advance();
            if(m_Tokenizer.getSymbol()=='.'){ // subroutine call
                int numArgs=0;
                m_Tokenizer.advance(); // write "."

                String calleeName2=m_Tokenizer.getIdentifier();

                String calleeName=calleeName1+"."+calleeName2;
                Symbol symbolFound=m_sysmbolTable.findSymbol(calleeName1);
                if(symbolFound !=null){
                    numArgs=numArgs+1; // method on a object need to pass object to the function
                    m_VMWriter.writePush(symbolFound.m_kind,symbolFound.m_varindex);
                    calleeName=symbolFound.m_type+"."+calleeName2;
                } else {
                    if(calleeName2.equals("")){
                        numArgs=numArgs+1; // method on a object need to pass object to the function
                        m_VMWriter.writePush(Segment.POINTER,0);
                        calleeName = m_className + "." + calleeName1;
                    }else {
                        calleeName = calleeName1 + "." + calleeName2;
                    }
                }
                m_Tokenizer.advance(); // write subroutine name
                m_Tokenizer.advance(); // write "("
                numArgs=numArgs+CompileExpressionList();
                m_Tokenizer.advance(); //  write ")"
                m_VMWriter.writeCall(calleeName,numArgs);

            } else if(m_Tokenizer.getSymbol()=='('){

                m_Tokenizer.advance(); // write "("
                int numArgs=CompileExpressionList();

                m_Tokenizer.advance(); // write ")"
                m_VMWriter.writeCall(calleeName1,numArgs);

            } else if(m_Tokenizer.getSymbol()=='['){
                Symbol symbol=m_sysmbolTable.findSymbol(calleeName1);
                m_VMWriter.writePush(symbol.m_kind,symbol.m_varindex);
                m_Tokenizer.advance(); // write "["
                CompileExpression();
                m_Tokenizer.advance(); // write "]"
                m_VMWriter.writeArithmetic(Command.add);

                //m_VMWriter.writePop(Segment.TEMP,0);
                m_VMWriter.writePop(Segment.POINTER,1);
                //m_VMWriter.writePush(Segment.TEMP,0);
                m_VMWriter.writePush(Segment.THAT,0);

            }else{
                Symbol symbol=m_sysmbolTable.findSymbol(calleeName1);
                m_VMWriter.writePush(symbol.m_kind,symbol.m_varindex);
            }
        }
    }

    public int CompileExpressionList() throws Exception {

        int numberofargument=0;
        while(true){
            if(m_Tokenizer.getTokenType() ==tokenType.SYMBOL && (m_Tokenizer.getSymbol() ==')' )){
                break;
            }
            CompileExpression();
            numberofargument++;
            while(m_Tokenizer.getTokenType()==tokenType.SYMBOL && (m_Tokenizer.getSymbol()==',')){

                m_Tokenizer.advance();
                numberofargument++;
                CompileExpression();
            }
        }

        return numberofargument;
    }

    public void compileDo() throws Exception {

        m_Tokenizer.advance(); // write do
        String calleeName1=m_Tokenizer.getIdentifier();
        String calleeName2="";
        m_Tokenizer.advance(); // write the class name

        if(m_Tokenizer.getSymbol()=='.'){
            m_Tokenizer.advance(); // write "."
            calleeName2=m_Tokenizer.getIdentifier();
            m_Tokenizer.advance(); // write the method or function name
        }

        String calleeName=null;
        int nArgs=0;

        Symbol symbolFound=m_sysmbolTable.findSymbol(calleeName1);
        if(symbolFound !=null){
            nArgs=nArgs+1; // method on a object need to pass object to the function
            m_VMWriter.writePush(symbolFound.m_kind,symbolFound.m_varindex);
            calleeName=symbolFound.m_type+"."+calleeName2;
        }else {
            if(calleeName2.equals("")){
                nArgs=nArgs+1; // method on a object need to pass object to the function
                m_VMWriter.writePush(Segment.POINTER,0);
                calleeName = m_className + "." + calleeName1;
            }else {
                calleeName = calleeName1 + "." + calleeName2;
            }
        }

        m_Tokenizer.advance(); // write "("
        nArgs=nArgs+CompileExpressionList();
        m_Tokenizer.advance(); // write ")"
        m_Tokenizer.advance(); // write ";"


        m_VMWriter.writeCall(calleeName,nArgs);
        m_VMWriter.writePop(Segment.TEMP,0);
    }

    public void compileLet() throws Exception {

        m_Tokenizer.advance(); // write let

        String varname=m_Tokenizer.getIdentifier();
        Symbol symbol=m_sysmbolTable.findSymbol(varname);
        m_Tokenizer.advance(); // write varName
        if(m_Tokenizer.getTokenType()==tokenType.SYMBOL && m_Tokenizer.getSymbol()=='[' ){

            m_VMWriter.writePush(symbol.m_kind,symbol.m_varindex);

            m_Tokenizer.advance(); // write "["
            // write expression
            CompileExpression();
            m_Tokenizer.advance(); // write "]"

            m_VMWriter.writeArithmetic(Command.add);

            m_Tokenizer.advance(); // write "="
            CompileExpression();
            m_Tokenizer.advance(); // write ";"

            m_VMWriter.writePop(Segment.TEMP,0);
            m_VMWriter.writePop(Segment.POINTER,1);
            m_VMWriter.writePush(Segment.TEMP,0);
            m_VMWriter.writePop(Segment.THAT,0);
        }
        else {

            m_Tokenizer.advance(); // write "="
            CompileExpression();


            m_VMWriter.writePop(symbol.m_kind, symbol.m_varindex);
            m_Tokenizer.advance(); // write ";"
        }
    }

    public void compileWhile() throws Exception {

        String whileLabel1=CompilationEngine.whileLabel+whileCounter;
        whileCounter++;
        String whileLabel2=CompilationEngine.whileLabel+whileCounter;
        whileCounter++;
        m_Tokenizer.advance(); // write while
        m_Tokenizer.advance(); // write "("
        m_VMWriter.writeLabel(whileLabel1);
        CompileExpression();
        m_VMWriter.writeArithmetic(Command.not);
        m_VMWriter.writeIf(whileLabel2);
        m_Tokenizer.advance(); // write ")"
        m_Tokenizer.advance(); // write {
        compileStatement();
        m_Tokenizer.advance(); // write }
        m_VMWriter.writeGoto(whileLabel1);
        m_VMWriter.writeLabel(whileLabel2);
    }

    public void compileReturn() throws Exception {

        m_Tokenizer.advance(); // write return

        if(m_Tokenizer.getTokenType() !=tokenType.SYMBOL){
            // return some thing
            CompileExpression();
            m_VMWriter.writeReturn();
            m_Tokenizer.advance(); // write ";"
        }else {
            // return void ;
            m_VMWriter.writePush(Segment.CONST,0);
            m_VMWriter.writeReturn();
            m_Tokenizer.advance(); // write ";"
        }

    }

    public void compileIf() throws Exception {

        String ifLabel1=CompilationEngine.IfLabel+ifCounter;
        ifCounter++;
        String ifLabel2=CompilationEngine.IfLabel+ifCounter;
        ifCounter++;

        m_Tokenizer.advance(); // write if
        m_Tokenizer.advance(); // write "("
        CompileExpression();
        m_Tokenizer.advance(); // write ")"
        m_Tokenizer.advance(); // write {

        m_VMWriter.writeArithmetic(Command.not);
        m_VMWriter.writeIf(ifLabel1);
        compileStatement();
        m_Tokenizer.advance(); // write }
        m_VMWriter.writeGoto(ifLabel2);
        m_VMWriter.writeLabel(ifLabel1);

        if(m_Tokenizer.getTokenType()==tokenType.KEYWORD && m_Tokenizer.getKeyWord().equals("else")==true){
            m_Tokenizer.advance(); // write else
            m_Tokenizer.advance(); // write {
            compileStatement();
            m_Tokenizer.advance(); // write }
        }
        m_VMWriter.writeLabel(ifLabel2);
    }

    public void compileStatement() throws Exception {

       // m_writer.write("<statements>\n");
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

       // m_writer.write("</statements>\n");

    }

    public void compileVarDec() throws IOException {

        // writeTokenInXML(m_Tokenizer,m_writer);
        Kind localkind=Symbol.convertKeyWordToKind(m_Tokenizer.getKeyWord());
        m_Tokenizer.advance(); // write var
        String localVarType=null;

        if(m_Tokenizer.getTokenType()==tokenType.KEYWORD) {
            localVarType=m_Tokenizer.getKeyWord();
        }else{
            localVarType=m_Tokenizer.getIdentifier();
        }
        m_Tokenizer.advance(); // write local variable data type

        String localName=m_Tokenizer.getIdentifier();
        m_Tokenizer.advance();  // write local variable name
        m_sysmbolTable.define(localName,localVarType,localkind);

        while(m_Tokenizer.getTokenType()==tokenType.SYMBOL && m_Tokenizer.getSymbol()==','){
            m_Tokenizer.advance(); // write ","
            localName=m_Tokenizer.getIdentifier();
            m_Tokenizer.advance(); // write "variable name"
            m_sysmbolTable.define(localName,localVarType,localkind);
        }


        m_Tokenizer.advance(); // write ;

    }

    public void close() throws IOException {
        m_VMWriter.close();
    }

}
