/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2000 Gerwin Klein <lsf@jflex.de>                          *
 * All rights reserved.                                                    *
 *                                                                         *
 * Thanks to Larry Bell and Bob Jamison for suggestions and comments.      *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/*
 * Title - CMPSC470P3
 * Date - 2/10/2022
 * Authors - Kody Backenstoes, Christopher Roberts
 */

%%

%class Lexer
%byaccj
%int

%{

  public Parser   parser;
  public int      lineno;
  public int      column;

  public Lexer(java.io.Reader r, Parser parser) {
    this(r);
    this.parser = parser;
    this.lineno = 1;
    this.column = 1;
  }
%}

int         = [0-9]+
boolean     = ("true"|"false")
identifier  = [a-zA-Z_][a-zA-Z0-9_]*
newline     = \n
whitespace  = [ \t\r]+
linecomment = "//".*



%%
/** Logic in Program
    1) The method yytext() returns the string of the current lexeme
    2) We must return the object type in the transition, like Parser.PRINT or Parser.INT.
    3) If we need to pass the string or value of token, use ParserVal() constructor
 */
// TODO: Ensure correctness
"<!--"[^]*"-->"                  {
                                     String comment = yytext();
                                     //System.out.print(comment);
                                     long newLineOccur = comment.chars().filter(ch -> ch == '\n').count();
                                     int nIndex = -1;
                                     if (newLineOccur > 0) {
                                         nIndex = comment.lastIndexOf('\n');
                                     }
                                     if (nIndex != -1) {
                                         this.lineno += newLineOccur;
                                         this.column = 1 + comment.substring(nIndex+1).length();
                                     }
                                     else {
                                         this.column += comment.length();
                                     }/* skip */ }
"print"                             { parser.yylval = new ParserVal((String)yytext()          ); return Parser.PRINT   ; }
"func"                              { parser.yylval = new ParserVal((String)yytext()          ); return Parser.FUNC    ; }
"var"                               { parser.yylval = new ParserVal((String)yytext()          ); return Parser.VAR     ; }
"void"                              { parser.yylval = new ParserVal((String)yytext()          ); return Parser.VOID    ; }
"bool"                              { parser.yylval = new ParserVal((String)yytext()          ); return Parser.BOOL    ; }
"int"                               { parser.yylval = new ParserVal((String)yytext()          ); return Parser.INT     ; }
"size"                              { parser.yylval = new ParserVal((String)yytext()          ); return Parser.SIZE    ; }
"new"                               { parser.yylval = new ParserVal((String)yytext()          ); return Parser.NEW     ; }
"if"                                { parser.yylval = new ParserVal((String)yytext()          ); return Parser.IF      ; }
"then"                              { parser.yylval = new ParserVal((String)yytext()          ); return Parser.THEN    ; }
"else"                              { parser.yylval = new ParserVal((String)yytext()          ); return Parser.ELSE    ; }
"begin"                             { parser.yylval = new ParserVal((String)yytext()          ); return Parser.BEGIN   ; }
"end"                               { parser.yylval = new ParserVal((String)yytext()          ); return Parser.END     ; }
"while"                             { parser.yylval = new ParserVal((String)yytext()          ); return Parser.WHILE   ; }
"return"                            { parser.yylval = new ParserVal((String)yytext()          ); return Parser.RETURN  ; }
"("                                 { parser.yylval = new ParserVal((String)yytext()          ); return Parser.LPAREN  ; }
")"                                 { parser.yylval = new ParserVal((String)yytext()          ); return Parser.RPAREN  ; }
"["                                 { parser.yylval = new ParserVal((String)yytext()          ); return Parser.LBRACKET; }
"]"                                 { parser.yylval = new ParserVal((String)yytext()          ); return Parser.RBRACKET; }
";"                                 { parser.yylval = new ParserVal((String)yytext()          ); return Parser.SEMI    ; }
","                                 { parser.yylval = new ParserVal((String)yytext()          ); return Parser.COMMA   ; }
"."                                 { parser.yylval = new ParserVal((String)yytext()          ); return Parser.DOT     ; }
"::"                                { parser.yylval = new ParserVal((String)yytext()          ); return Parser.TYPEOF  ; }
"<-"                                { parser.yylval = new ParserVal((String)yytext()          ); return Parser.ASSIGN  ; }
"->"                                { parser.yylval = new ParserVal((String)yytext()          ); return Parser.FUNCRET ; }
"+"                                 { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.EXPROP  ; }
"-"                                 { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.EXPROP  ; }
"or"                                { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.EXPROP  ; }
"*"                                 { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.TERMOP  ; }
"/"                                 { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.TERMOP  ; }
"and"                               { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.TERMOP  ; }

"="                                 { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.RELOP   ; }
"!="                                { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.RELOP   ; }
"<="                                { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.RELOP   ; }
">="                                { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.RELOP   ; }
"<"                                 { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.RELOP   ; }
">"                                 { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.RELOP   ; }
"true"                              { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.BOOL_LIT; }
"false"                             { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.BOOL_LIT; }
"@"                                 { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.AT      ; }

{int}                               { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.INT_LIT  ; }
{identifier}                        { parser.yylval = new ParserVal((Object)yytext()          ); return Parser.IDENT    ; }
{linecomment}                       { /*System.out.print(yytext());  skip */ }
{newline}                           { //System.out.print("\n");
                                      this.lineno += 1;
                                      this.column = 1;
                                      /* skip */ }
{whitespace}                        { String space = yytext();
                                      //System.out.print(space);
                                      for (char c: space.toCharArray()) {
                                          this.column += 1;
                                      }
                                      /* skip */ }


\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { System.err.println("Error: unexpected character '"+yytext()+"'"); return -1; }
