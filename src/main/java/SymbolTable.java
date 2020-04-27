import java.util.*;

public class SymbolTable {

    private Map<String,Symbol> m_ClassVarTable;
    private List<Map<String,Symbol>> m_ScopeChain;
    int num_arg_defined;
    int num_var_defined;
    int num_static_defined;
    int num_field_defined;

    SymbolTable(){
        m_ClassVarTable=new HashMap<String,Symbol>();
        m_ScopeChain=new LinkedList<Map<String,Symbol>>();
        num_static_defined=0;
        num_field_defined=0;
        num_arg_defined=0;
        num_var_defined=0;
    }

    public void define(String name,String type,Kind kind){

        int varcount=VarCount(kind);
        Symbol symbol= new Symbol(name,type,varcount,kind);
        IncCount(kind);
        if(kind==kind.STATIC || kind == kind.FIELD){
            //m_ClassVarTable.add(symbol);
            m_ClassVarTable.put(name,symbol);
        } else {
            Map<String,Symbol> scopeTable=m_ScopeChain.get(m_ScopeChain.size()-1);
            scopeTable.put(name,symbol);
        }

    }

    public void IncCount(Kind kind){
        switch(kind){
            case ARG:
                num_arg_defined++;
            break;
            case VAR:
                num_var_defined++;
            break;
            case STATIC:
                num_static_defined++;
            break;
            case FIELD:
                num_field_defined++;
            break;

        }
    }

    public int VarCount(Kind kind){
        switch(kind){
            case ARG:
                return num_arg_defined;
                //break;
            case VAR:
                return num_var_defined;
                //break;
            case STATIC:
                return num_static_defined;
                //break;
            case FIELD:
                return num_field_defined;
                //break;

            default:
                return -1;

        }
    }


/*
    public int VarCount(Kind kind){

        int varcount=0;
        if(kind==kind.STATIC || kind == kind.FIELD){
            Iterator<Map.Entry<String,Symbol>> it=m_ClassVarTable.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, Symbol> pair = (Map.Entry<String, Symbol>) it.next();
                Symbol symbol=pair.getValue();
                if(symbol.m_kind==kind){
                    varcount++;
                }
            }
        } else {
            Map<String,Symbol> scopeTable=m_ScopeChain.get(m_ScopeChain.size()-1);
            Iterator<Map.Entry<String,Symbol>> it=scopeTable.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, Symbol> pair = (Map.Entry<String, Symbol>) it.next();
                Symbol symbol=pair.getValue();
                if(symbol.m_kind==kind){
                    varcount++;
                }
            }
        }
        return varcount;
    }

 */

    public Symbol findSymbol(String name){
        Symbol symbol=null;

        for(int i=m_ScopeChain.size()-1; i>=0; i--){
            Map<String,Symbol> scopeTable=m_ScopeChain.get(i);
            symbol=scopeTable.get(name);
            if(symbol !=null){
                return symbol;
            }
        }


        symbol=m_ClassVarTable.get(name);
        if(symbol !=null){
            return symbol;
        }

        return symbol;
    }

    public Kind kindOf(String name) throws Exception {
        Symbol findSymbol=findSymbol(name);
        if(findSymbol==null){
            throw new Exception("Symbol not found!");
        }

        return findSymbol.m_kind;

    }

    public String Typeof(String name) throws Exception {
        Symbol findSymbol=findSymbol(name);
        if(findSymbol==null){
            throw new Exception("Symbol not found!");
        }

        return findSymbol.m_type;
    }

    public int indexOf(String name) throws Exception {

        Symbol findSymbol=findSymbol(name);
        if(findSymbol==null){
            throw new Exception("Symbol not found!");
        }

        return findSymbol.m_varindex;
    }

    public void StartSubRoutine(){
        num_arg_defined=0;
        num_var_defined=0;

        Map<String,Symbol> symbolTable=new HashMap<String,Symbol>();
        m_ScopeChain.add(symbolTable);
    }

    public void LeaveSubRoutine() {
        m_ScopeChain.remove(m_ScopeChain.size() - 1);

        int numOfVar = 0;
        int numOfArg = 0;

        if (m_ScopeChain.size() != 0) {

            Map<String, Symbol> scopeTable = m_ScopeChain.get(m_ScopeChain.size() - 1);
            Iterator<Map.Entry<String, Symbol>> it = scopeTable.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Symbol> pair = (Map.Entry<String, Symbol>) it.next();
                Symbol symbol = pair.getValue();
                if (symbol.m_kind == Kind.VAR) {
                    numOfVar++;
                }

                if (symbol.m_kind == Kind.ARG) {
                    numOfArg++;
                }
            }
            num_arg_defined = numOfArg;
            num_var_defined = numOfVar;

        } else {
            num_arg_defined = 0;
            num_var_defined = 0;
        }
    }
}
