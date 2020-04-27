import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

//https://stackoverflow.com/questions/6667243/using-enum-values-as-string-literals



enum Command {
    add,
    sub,
    neg,
    eq,
    gt,
    lt,
    and,
    or,
    not,
}



public class VMWriter {

    private FileWriter m_FileWriter;
    private String m_outfileName;
    private BufferedWriter m_writer;

    VMWriter(String outfileName) throws IOException {
        m_outfileName=outfileName;
        m_FileWriter=new FileWriter(m_outfileName, false);
        m_writer= new BufferedWriter(m_FileWriter);
    }

    void writePush(String segment, int index) throws IOException {
        String command="push"+" "+segment+" "+index+"\n";
        m_writer.write(command);
    }

    void writePush(Kind kind,  int index) throws Exception {
        if(kind==Kind.FIELD){
            writePush(Segment.THIS,index);
        }else if(kind==Kind.ARG){
            writePush(Segment.ARG,index);
        }else if(kind==Kind.STATIC){
            writePush(Segment.STATIC,index);
        }else if(kind==Kind.VAR) {
            writePush(Segment.LOCAL,index);
        } else {
            throw new Exception("Do not support this type:"+kind.name());
        }
    }

    void writePop(String segment,int index) throws IOException {
        String command="pop"+" "+segment+" "+index+"\n";
        m_writer.write(command);
    }

    void writePop(Kind kind, int index) throws Exception {
        if(kind==Kind.FIELD){
            writePop(Segment.THIS,index);
        }else if(kind==Kind.ARG){
            writePop(Segment.ARG,index);
        }else if(kind==Kind.STATIC){
            writePop(Segment.STATIC,index);
        }else if(kind==Kind.VAR) {
            writePop(Segment.LOCAL,index);
        } else {
            throw new Exception("Do not support this type:"+kind.name());
        }
    }

    void writeArithmetic(Command cmd) throws IOException {
        m_writer.write(cmd.name()+"\n");
    }

    void writeCall(String name,int nArgs) throws IOException {
        m_writer.write("call"+" "+name+" "+nArgs+"\n");
    }

    void writeFunction(String name,int nLocals) throws IOException {
        m_writer.write("function"+" "+name+" "+nLocals+"\n");
    }

    void writeReturn() throws IOException {
        m_writer.write("return\n");
    }

    void writeLabel(String label) throws IOException {
        m_writer.write("label"+" "+label+"\n");
    }

    void writeIf(String label) throws IOException {
        m_writer.write("if-goto"+" "+label+"\n");
    }

    void writeGoto(String label) throws IOException {
        m_writer.write("goto"+" "+label+"\n");
    }

    void close() throws IOException {
        m_writer.close();
    }

}
