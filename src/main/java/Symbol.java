

enum Kind {
    FIELD,
    STATIC,
    ARG,
    VAR
}


public class Symbol {

    public String m_type;
    public String m_varname;
    public int m_varindex;
    public Kind m_kind;

    Symbol(String name, String type,int varindex, Kind kind){
        m_varname=name;
        m_type=type;
        m_kind=kind;
        m_varindex=varindex;
    }

    public static Kind convertKeyWordToKind(String keyWord){
        if(keyWord.equals("field")){
            return Kind.FIELD;
        }else if(keyWord.equals("static")) {
            return Kind.STATIC;
        }else if(keyWord.equals("var")) {
            return Kind.VAR;
        }else {
            return Kind.ARG;
        }

    }
}
