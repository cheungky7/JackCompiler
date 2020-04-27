import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TestJackTokenizer {

    private JackTokenizer m_Tokenizer;
    private FileWriter m_FileWriter;
    private String m_outfileName;
    private BufferedWriter m_writer;

    public TestJackTokenizer(JackTokenizer tokenizer) throws IOException {
        m_outfileName=tokenizer.getSrcPath()+"/"+tokenizer.getSrcFileName()+"T.xml";
        m_Tokenizer=tokenizer;
        m_FileWriter=new FileWriter(m_outfileName, false);
        m_writer= new BufferedWriter(m_FileWriter);

    }

    public void writeToken() throws IOException {

        m_writer.write("<tokens>\n");

        while(m_Tokenizer.hasMoreCommands()){


            tokenType type=m_Tokenizer.getTokenType();

            if(type==tokenType.KEYWORD){
                String keyword=m_Tokenizer.getKeyWord();
                m_writer.write("<keyword>"+keyword+"</keyword>\n");
            }

            if(type==tokenType.IDENTIFIER){
                String identifier=m_Tokenizer.getIdentifier();
                m_writer.write("<identifier>"+identifier+"</identifier>\n");
            }

            if(type==tokenType.SYMBOL){
                String symbol=JA_Uitilty.handleSpecialChar(m_Tokenizer.getSymbol());
                m_writer.write("<symbol>"+symbol+"</symbol>\n");
            }

            if(type==tokenType.INT_CONST){
                int value=m_Tokenizer.getIntVal();
                m_writer.write("<integerConstant>"+String.valueOf(value)+"</integerConstant>\n");
            }

            if(type==tokenType.STRING_CONST){
                String stringconst=m_Tokenizer.getStringVal();
                m_writer.write("<stringConstant>"+stringconst+"</stringConstant>\n");
            }
            m_Tokenizer.advance();
        }
        m_writer.write("</tokens>");
    }

    public void close() throws IOException {
        m_writer.close();
    }



}
