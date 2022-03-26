import java.sql.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class Parser
{
    //list of constant token ids
    public static final int ENDMARKER   =  0;
    public static final int LEXERROR    =  1;

    public static final int BOOL        = 10;
    public static final int INT         = 11;
    public static final int NEW         = 12;
    //public static final int PTR         = 13;
    //public static final int DO          = 14;
    public static final int WHILE       = 15;
    public static final int IF          = 16;
    public static final int THEN        = 17;
    public static final int ELSE        = 18;
    //public static final int ENDIF       = 19;
    public static final int BEGIN       = 20;
    public static final int END         = 21;
    public static final int LPAREN      = 22;
    public static final int RPAREN      = 23;
    public static final int LBRACKET    = 24;
    public static final int RBRACKET    = 25;
    public static final int ASSIGN      = 26;
    public static final int RELOP       = 27;
    public static final int EXPROP      = 28;
    public static final int TERMOP      = 29;
    public static final int SEMI        = 30;
    public static final int COMMA       = 31;
    public static final int BOOL_LIT    = 32;
    public static final int INT_LIT     = 33;
    public static final int IDENT       = 34;
    public static final int FUNC        = 35;
    public static final int TYPEOF      = 36;
    public static final int FUNCRET     = 37;
    public static final int VAR         = 38;
    public static final int PRINT       = 39;
    public static final int RETURN      = 40;
    public static final int DOT         = 41;
    public static final int SIZE        = 42;
    public static final int VOID        = 43;
    public static final int AT          = 44;

    //Token class to hold current token being viewed
    public class Token
    {
        public int       type;
        public ParserVal attr;
        public String    text;
        public Token(int type, ParserVal attr, String text) {
            this.type   = type;
            this.attr   = attr;
            this.text   = text;
        }
    }

    //global declarations
    public ParserVal yylval;
    Token _token;
    Lexer _lexer;
    List<String> test = new ArrayList<String>();
    public Parser(java.io.Reader r) throws Exception
    {
        System.out.println();
        _lexer = new Lexer(r, this);
        _token = null;
        Advance();
    }

    //progress LAH one token forward
    public void Advance() throws Exception
    {
        int token_type = _lexer.yylex();
        if(token_type ==  0)      _token = new Token(ENDMARKER , null, null  );
        else if(token_type == -1) _token = new Token(LEXERROR  , yylval, _lexer.yytext());
        else                      _token = new Token(token_type, yylval, _lexer.yytext());
    }

    //helper method to convert expected token id to token string
    private String expectedToken(int id){
        switch (id){
            case BOOL    : return "\"bool\"" ;
            case INT     : return "\"int\""  ;
            case NEW     : return "\"new\""  ;
            case WHILE   : return "\"while\"";
            case IF      : return "\"if\""   ;
            case THEN    : return "\"then\"" ;
            case ELSE    : return "\"else\"" ;
            case BEGIN   : return "\"begin\"";
            case END     : return "\"end\""  ;
            case LPAREN  : return "\"(\""    ;
            case RPAREN  : return "\")\""    ;
            case LBRACKET: return "\"[\""    ;
            case RBRACKET: return "\"]\""    ;
            case ASSIGN  : return "\"<-\""   ;
            case RELOP   : return "a relative operation";
            case EXPROP  : return "an expression operation";
            case TERMOP  : return "a term operation";
            case SEMI    : return "\";\"";
            case COMMA   : return "\",\"";
            case BOOL_LIT: return "a boolean value";
            case INT_LIT : return "an integer";
            case IDENT   : return "an identifier";
            case FUNC    : return "\"func\"" ;
            case TYPEOF  : return "\"::\"";
            case FUNCRET : return "\"->\"";
            case VAR     : return "\"var\"";
            case PRINT   : return "\"print\"";
            case RETURN  : return "\"return\"";
            case DOT     : return "\".\"";
            case SIZE    : return "\"size\"";
            case VOID    : return "\"void\"";
            case AT      : return "\"@\"";
        }
        return "UNKNOWN_TYPE";
    }

    //match current token with top of stack
    public String Match(int token_type) throws Exception
    {
        boolean match = (token_type == _token.type);
        String lexeme = _token.text;

        if(!match) {
            throw new Exception(expectedToken(token_type) + " is expected instead of \"" + lexeme + "\" at " + _lexer.lineno + ":" + _lexer.column);
        }
        if(_token.type != ENDMARKER){
            //increment col no, then move pointer forward
            _lexer.column += _token.text.length();
            Advance();
            return lexeme;
        }
        return "";
    }

    //run the parser
    public int yyparse() throws Exception
    {
        try
        {
            List<String> output = parse();

            System.out.println("Success: no syntax error is found.\n");
            System.out.println("Following is the indentation-updated source code:\n");
            for (String s: output){
                System.out.print(s);
            }
        }
        catch(Exception e)
        {
            System.out.println("Error: There is syntax error(s).");
            System.out.println(e.getMessage());
        }
        return 0;
    }

    //initialize the starting
    public List<String> parse() throws Exception
    {
        return program();
    }

    //program
    public List<String> program() throws Exception
    {
        switch(_token.type)
        {
            // program -> decl_list
            case ENDMARKER:
            case FUNC:
                List<String> temp = decl_list();
                String temp1 = Match(ENDMARKER);
                temp.add(temp1);
                return temp;
        }
        throw new Exception("Incorrect function declaration at " + _lexer.lineno + ":" + _lexer.column);
    }

    //decl_list
    public List<String> decl_list() throws Exception
    {
        switch(_token.type)
        {
            // decl_list -> decl_list'
            case FUNC:
            case ENDMARKER:
                return decl_list_();
        }
        throw new Exception("Incorrect declaration list at " + _lexer.lineno + ":" + _lexer.column);
    }

    //decl_list'
    public List<String> decl_list_() throws Exception
    {
        switch(_token.type)
        {
            // decl_list'	-> fun_decl decl_list'
            case FUNC:
                //fun_decl();
                //decl_list_();
                List<String> temp = new ArrayList<>();
                temp.addAll(fun_decl());
                temp.addAll(decl_list_());
                return temp;
            // decl_list' -> ε
            case ENDMARKER:
                return Collections.emptyList();
        }
        throw new Exception("Incorrect declaration list " + _lexer.lineno + ":" + _lexer.column);
    }

    //fun_decl
    public List<String> fun_decl() throws Exception
    {

        switch(_token.type)
        {
            // fun_decl -> FUNC IDENT TYPEOF LPAREN params RPAREN FUNCRET prim_type BEGIN local_decls stmt_list END
            case FUNC:
                List<String> temp = new ArrayList<String >();
                temp.add(Match(FUNC));
                temp.add(" ");
                temp.add(Match(IDENT));
                temp.add(Match(TYPEOF));
                temp.add(Match(LPAREN));
                temp.add(params());
                temp.add(Match(RPAREN));
                temp.add(Match(FUNCRET));
                temp.add(prim_type());
                temp.add("\n");
                temp.add(Match(BEGIN));
                temp.add("\n");
                temp.addAll(prependTab(local_decls()));
                temp.addAll(prependTab(stmt_list()));
                temp.add(Match(END));
                temp.add("\n");
                temp.add("\n");
                return temp;
        }
        throw new Exception("Incorrect function decl at " + _lexer.lineno + ":" + _lexer.column);
    }

    //params
    public String params() throws Exception
    {
        switch(_token.type){
            // params -> param_list
            case IDENT:
                return param_list();
            //params -> ε
            case RPAREN:
                return ""; //Collections.emptyList();
        }
        throw new Exception("Incorrect parameter declaration at " + _lexer.lineno + ":" + _lexer.column);
    }

    //param_list
    public String param_list() throws Exception
    {
        switch(_token.type){
            // param_list -> param param_list'
            case IDENT:
                String temp = "";
                temp += param();
                temp += param_list_();
                return temp;
        }
        throw new Exception("Incorrect parameter declaration at " + _lexer.lineno + ":" + _lexer.column);
    }

    //param_list'
    public String param_list_() throws Exception
    {
        switch(_token.type){
            // param_list' -> ε
            case RPAREN:
                return ""; //Collections.emptyList();
            // param_list' -> COMMA param param_list'
            case COMMA:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (Match(COMMA));
                temp += (" ");
                temp += (param());
                temp += (param_list_());
                return temp;
        }
        throw new Exception("Incorrect parameter declaration at " + _lexer.lineno + ":" + _lexer.column);
    }

    //param
    public String param() throws Exception
    {
        switch(_token.type){
            // param -> IDENT TYPEOF type_spec
            case IDENT:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (Match(IDENT));
                temp += (Match(TYPEOF));
                temp += (type_spec());
                return temp;
        }
        throw new Exception("Incorrect parameter declaration at " + _lexer.lineno + ":" + _lexer.column);
    }

    //type_spec
    public String type_spec() throws Exception
    {
        switch(_token.type)
        {
            // type_spec -> prim_type type_spec'
            case BOOL:
            case INT:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (prim_type());
                temp += (type_spec_());
                return temp;
        }
        throw new Exception("Incorrect type specification at " + _lexer.lineno + ":" + _lexer.column);
    }

    //type_spec'
    public String type_spec_() throws Exception
    {
        switch(_token.type){
            //type_spec' -> ε
            case RPAREN:
            case COMMA:
            case SEMI:
                return ""; //Collections.emptyList();
            // type_spec' -> LBRACKET RBRACKET
            case LBRACKET:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (Match(LBRACKET));
                temp += (Match(RBRACKET));
                return temp;
        }
        throw new Exception("Incorrect type specification at " + _lexer.lineno + ":" + _lexer.column);
    }

    //prim_type
    public String prim_type() throws Exception
    {
        switch(_token.type){
            // prim_type -> INT
            case INT:
                return(Match(INT));
            //prim_type -> BOOL
            case BOOL:
                return(Match(BOOL));
        }
        throw new Exception("Incorrect primitive type at " + _lexer.lineno + ":" + _lexer.column);
    }

    //local_decls
    public List<String> local_decls() throws Exception{
        switch(_token.type){
            // local_decls -> local_decls'
            case IDENT:
            case BEGIN:
            case END:
            case VAR:
            case PRINT:
            case RETURN:
            case IF:
            case WHILE:
                return local_decls_();
        }
        throw new Exception("Incorrect declaration of a local variable at " + _lexer.lineno + ":" + _lexer.column);
    }

    //local_decls'
    public List<String> local_decls_() throws Exception{
        switch(_token.type){
            // local_decls' -> ε
            case IDENT:
            case BEGIN:
            case END:
            case PRINT:
            case RETURN:
            case IF:
            case WHILE:
                return Collections.emptyList();
            // local_decls' -> local_decl local_decls'
            case VAR:
                List<String> temp = new ArrayList<>();
                //temp.add("    ");
                temp.add(local_decl());
                //if(temp.get(temp.size()-1).equals("    ")){ temp.add("\n");; temp.add("    "); }
                //temp.add("\n");
                temp.addAll(local_decls_());
                //temp.add("    ");
                //temp.add("\n");
                return temp;
        }
        throw new Exception("Incorrect declaration of local variable at " + _lexer.lineno + ":" + _lexer.column);
    }

    //local_decl
    public String local_decl() throws Exception{
        switch(_token.type){
            // local_decl -> VAR IDENT TYPEOF type_spec SEMI
            case VAR:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                //temp.add("    ");
                temp += (Match(VAR));
                temp += (" ");
                temp += (Match(IDENT));
                temp += (Match(TYPEOF));
                temp += (type_spec());
                temp += (Match(SEMI));
                temp += ("\n");
                return temp;
        }
        throw new Exception("Incorrect declaration of local variable at " + _lexer.lineno + ":" + _lexer.column);
    }

    //stmt_list
    public List<String> stmt_list() throws Exception{
        switch(_token.type){
            // stmt_list -> stmt_list'
            case IDENT:
            case BEGIN:
            case END:
            case PRINT:
            case RETURN:
            case IF:
            case ELSE:
            case WHILE:
                List<String> temp = new ArrayList<>();
                //temp.add("    ");
                temp.addAll(stmt_list_());
                //temp.add("\n");
                return temp;
        }
        throw new Exception("Incorrect statement at " + _lexer.lineno + ":" + _lexer.column);
    }

    //stmt_list'
    public List<String> stmt_list_() throws Exception{
        switch(_token.type){
            // stmt_list' -> stmt stmt_list'
            case IDENT:
            case BEGIN:
            case PRINT:
            case RETURN:
            case IF:
            case WHILE:
                List<String> temp = new ArrayList<>();
                //temp.add("    ");
                temp.addAll(stmt());
                //temp.add("    ");
                temp.addAll(stmt_list_());
                //temp.add("    ");
                return temp;
            // stmt_list' -> ε
            case END:
            case ELSE:
                return Collections.emptyList();
        }
        throw new Exception("Incorrect statement at " + _lexer.lineno + ":" + _lexer.column);
    }

    //stmt
    public List<String> stmt() throws Exception{
        switch(_token.type){
            // stmt -> expr_stmt
            case IDENT:
                List<String> temp = new ArrayList<>();
                //temp.add("    ");
                temp.addAll(expr_stmt());
                return temp;
            // stmt -> compound_stmt
            case BEGIN:
                List<String> temp1 = new ArrayList<>();
                //temp1.add("    ");
                temp1.addAll(compound_stmt());
                return temp1;
            // stmt -> print_stmt
            case PRINT:
                List<String> temp2 = new ArrayList<>();
                //temp2.add("    ");
                temp2.add(print_stmt());
                return temp2;
            // stmt -> return_stmt
            case RETURN:
                List<String> temp3 = new ArrayList<>();
                //temp3.add("    ");
                temp3.add(return_stmt());
                return temp3;
            // stmt -> if_stmt
            case IF:
                List<String> temp4 = new ArrayList<>();
                //temp4.add("    ");
                temp4.addAll(if_stmt());
                return temp4;
            // stmt -> while_stmt
            case WHILE:
                List<String> temp5 = new ArrayList<>();
                //temp5.add("    ");
                temp5.addAll(while_stmt());
                return temp5;
        }
        throw new Exception("Incorrect statement at " + _lexer.lineno + ":" + _lexer.column);
    }

    //expr_stmt
    public List<String> expr_stmt() throws Exception{
        switch(_token.type){
            // expr_stmt -> IDENT ASSIGN expr SEMI
            case IDENT:
                String s = "";
                List<String> temp = new ArrayList<>();
                s += (Match(IDENT));
                s += (" ");
                s += (Match(ASSIGN));
                s += (" ");
                s += (expr());
                s += (Match(SEMI));
                temp.add(s);
                temp.add("\n");
                return temp;
        }
        throw new Exception("Incorrect expression statement at " + _lexer.lineno + ":" + _lexer.column);
    }

    //print_stmt
    public String print_stmt() throws Exception{
        switch(_token.type){
            // print_stmt -> PRINT expr SEMI
            case PRINT:
                String temp = "";
                temp += (Match(PRINT));
                temp += (" ");
                temp += (expr());
                temp += (Match(SEMI));
                temp += ("\n");
                return temp;
        }
        throw new Exception("Incorrect print statement at " + _lexer.lineno + ":" + _lexer.column);
    }

    //return_stmt
    public String return_stmt() throws Exception{
        switch(_token.type){
            // return_stmt -> RETURN expr SEMI
            case RETURN:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (Match(RETURN));
                temp += (" ");
                temp += (expr());
                temp += (Match(SEMI));
                temp += ("\n");
                return temp;
        }
        throw new Exception("Incorrect return statement at " + _lexer.lineno + ":" + _lexer.column);
    }

    //if_stmt
    public List<String> if_stmt() throws Exception{
        switch(_token.type){
            // if_stmt -> IF expr THEN stmt_list ELSE stmt_list END
            case IF:
                String builder = "";
                List<String> temp = new ArrayList<>();
                builder += (Match(IF));
                builder += (" ");
                builder += (expr());
                builder += (" ");
                builder += (Match(THEN));
                builder += ("\n");
                temp.add(builder);
                temp.addAll(prependTab(stmt_list()));
                temp.add(Match(ELSE));
                temp.add("\n");
                temp.addAll(prependTab(stmt_list()));
                temp.add(Match(END));
                temp.add("\n");
                return temp;
        }
        throw new Exception("Incorrect if statement at " + _lexer.lineno + ":" + _lexer.column);
    }

    //while_stmt
    public List<String> while_stmt() throws Exception{
        switch(_token.type){
            // while_stmt -> WHILE expr BEGIN stmt_list END
            case WHILE:
                String builder = "";
                List<String> temp = new ArrayList<>();
                builder += (Match(WHILE));
                builder += (" ");
                builder += (expr());
                builder += ("\n");
                temp.add(builder);
                temp.add(Match(BEGIN));
                temp.add("\n");
                temp.addAll(prependTab(stmt_list()));
                temp.add(Match(END));
                temp.add("\n");
                return temp;
        }
        throw new Exception("Incorrect while statement at " + _lexer.lineno + ":" + _lexer.column);
    }

    //compound_stmt
    public List<String> compound_stmt() throws Exception
    {
        switch(_token.type){
            // compound_stmt -> BEGIN local_decls stmt_list END
            case BEGIN:
                List<String> temp = new ArrayList<>();
                temp.add(Match(BEGIN));
                temp.add("\n");
                temp.addAll(prependTab(local_decls()));
                temp.addAll(prependTab(stmt_list()));
                temp.add(Match(END));
                temp.add("\n");
                return temp;
        }
        throw new Exception("Incorrect compound statement at " + _lexer.lineno + ":" + _lexer.column);
    }

    //args
    public String args() throws Exception{
        switch(_token.type){
            // args -> arg_list
            case IDENT:
            case LPAREN:
            case INT_LIT:
            case BOOL_LIT:
            case NEW:
                return arg_list();
            // args -> ε
            case RPAREN:
                return "";//Collections.emptyList();
        }
        throw new Exception("Incorrect argument format at " + _lexer.lineno + ":" + _lexer.column);
    }

    //arg_list
    public String arg_list() throws Exception{
        switch(_token.type){
            // arg_list -> expr arg_list'
            case IDENT:
            case LPAREN:
            case INT_LIT:
            case BOOL_LIT:
            case NEW:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (expr());
                temp += (arg_list_());
                return temp;
        }
        throw new Exception("Incorrect argument format at " + _lexer.lineno + ":" + _lexer.column);
    }

    //arg_list'
    public String arg_list_() throws Exception{
        switch(_token.type){
            // arg_list' -> ε
            case RPAREN:
                return ""; //Collections.emptyList();
            // arg_list' -> COMMA expr arg_list'
            case COMMA:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (Match(COMMA));
                temp += (" ");
                temp += (expr());
                temp += (arg_list_());
                return temp;
        }
        throw new Exception("Incorrect argument format at " + _lexer.lineno + ":" + _lexer.column);
    }

    //expr
    public String expr() throws Exception{
        switch(_token.type){
            // expr -> term expr'
            case IDENT:
            case LPAREN:
            case INT_LIT:
            case BOOL_LIT:
            case NEW:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (term());
                temp += (expr_());
                return temp;
        }
        throw new Exception("Incorrect expression at " + _lexer.lineno + ":" + _lexer.column);
    }

    //expr'
    public String expr_() throws Exception{
        switch(_token.type){
            // expr' -> ε
            case RPAREN:
            case BEGIN:
            case COMMA:
            case RBRACKET:
            case SEMI:
            case THEN:
                return ""; //Collections.emptyList();
            // expr' -> EXPROP term expr'
            case EXPROP:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (" ");
                temp += (Match(EXPROP));
                temp += (" ");
                temp += (term());
                temp += (expr_());
                return temp;
            // expr' -> RELOP term expr'
            case RELOP:
                String temp1 = "";
                //List<String> temp1 = new ArrayList<>();
                temp1 += (" ");
                temp1 += (Match(RELOP));
                temp1 += (" ");
                temp1 += (term());
                temp1 += (expr_());
                return temp1;
        }
        throw new Exception("Incorrect expression at " + _lexer.lineno + ":" + _lexer.column);
    }

    //term
    public String term() throws Exception{
        switch(_token.type){
            // term -> factor term'
            case IDENT:
            case LPAREN:
            case INT_LIT:
            case BOOL_LIT:
            case NEW:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (factor());
                temp += (term_());
                return temp;
        }
        throw new Exception("Incorrect expression at " + _lexer.lineno + ":" + _lexer.column);
    }

    //term'
    public String term_() throws Exception{
        switch(_token.type){
            // term' -> ε
            case RPAREN:
            case BEGIN:
            case COMMA:
            case RBRACKET:
            case SEMI:
            case THEN:
            case EXPROP:
            case RELOP:
                return ""; //Collections.emptyList();
            // term' -> TERMOP factor term'
            case TERMOP:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (" ");
                temp += (Match(TERMOP));
                temp += (" ");
                temp += (factor());
                temp += (term_());
                return temp;
        }
        throw new Exception("Incorrect expression at " + _lexer.lineno + ":" + _lexer.column);
    }

    //factor
    public String factor() throws Exception{
        switch(_token.type){
            // factor -> IDENT factor'
            case IDENT:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (Match(IDENT));
                temp += (factor_());
                return temp;
            // factor -> LPAREN expr RPAREN
            case LPAREN:
                String temp1 = "";
                //List<String> temp1 = new ArrayList<>();
                temp1 += (Match(LPAREN));
                temp1 += (expr());
                temp1 += (Match(RPAREN));
                return temp1;
            // factor -> INT_LIT
            case INT_LIT:
                String temp2 = "";
                //List<String> temp2 = new ArrayList<>();
                temp2 += (Match(INT_LIT));
                return temp2;
            // factor -> BOOL_LIT
            case BOOL_LIT:
                String temp3 = "";
                //List<String> temp3 = new ArrayList<>();
                temp3 += (Match(BOOL_LIT));
                return temp3;
            // factor -> NEW prim_type LBRACKET expr RBRACKET
            case NEW:
                String temp4 = "";
                //List<String> temp4 = new ArrayList<>();
                temp4 += (Match(NEW));
                temp4 += (" ");
                temp4 += (prim_type());
                temp4 += (Match(LBRACKET));
                temp4 += (expr());
                temp4 += (Match(RBRACKET));
                ///temp4.add("\n");
                return temp4;
        }
        throw new Exception("Incorrect expression at " + _lexer.lineno + ":" + _lexer.column);
    }

    //factor'
    public String factor_() throws Exception{
        switch(_token.type){
            // factor' -> LPAREN args RPAREN
            case LPAREN:
                String temp = "";
                //List<String> temp = new ArrayList<>();
                temp += (Match(LPAREN));
                temp += (args());
                temp += (Match(RPAREN));
                return temp;
            // factor' -> ε
            case RPAREN:
            case BEGIN:
            case COMMA:
            case RBRACKET:
            case SEMI:
            case THEN:
            case EXPROP:
            case RELOP:
            case TERMOP:
                return ""; //Collections.emptyList();
            // factor' -> LBRACKET expr RBRACKET
            case LBRACKET:
                String temp1 = "";
                //List<String> temp1 = new ArrayList<>();
                temp1 += (Match(LBRACKET));
                temp1 += (expr());
                temp1 += (Match(RBRACKET));
                return temp1;
            // factor' -> DOT SIZE
            case DOT:
                String temp2 = "";
                //List<String> temp2 = new ArrayList<>();
                temp2 += (Match(DOT));
                temp2 += (Match(SIZE));
                return temp2;
        }
        throw new Exception("Incorrect expression at " + _lexer.lineno + ":" + _lexer.column);
    }

    public List<String> prependTab(List<String> lst) {
        List<String> retVals = new ArrayList<>(lst);
        for(int i = 0; i < retVals.size(); i++){
            String temp = retVals.get(i);
            if (!temp.equals("\n")){
                temp = "    " + temp;
            }
            retVals.set(i, temp);
        }
        return retVals;
    }
    public String prependTab(String s){
        if (!s.equals("\n")){
            return "    " + s;
        }
        else{ return s; }
    }
}