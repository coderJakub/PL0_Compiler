//import java.io.*;

public class Parser extends Lexer{
    Lexer lexer = new Lexer();
    public class Arc{
        int next;
        int alt;
        int action(){
            return -1; //-1->funtion doesnt exist
        }
        public Arc(){
            next=alt=0;
        }
        public Arc(int n, int a){
            next=n;
            alt=a;
        }
    }
    public class ArcNil extends Arc{
    }
    
    public class ArcSymbol extends Arc{
        int sym;
        public ArcSymbol(int s, int n, int a){
            super(n,a);
            sym=s;
        }
    }
    public class ArcToken extends Arc{
        Token token;
        public ArcToken(Token t, int n, int a){
            super(n,a);
            token=t;
        }
    }
    public class ArcGraph extends Arc{
        Arc[] graph;
        public ArcGraph(Arc[] g, int n, int a){
            super(n,a);
            graph=g;
        }
    }
    public class ArcEnd extends Arc{
        public ArcEnd(int n, int a){
            super(n,a);
        }
    }
    Arc[] block;
    Arc[] program;
    Arc[] statement;
    Arc[] expression;
    Arc[] term;
    Arc[] factor;
    Arc[] condition;
    
    public Parser(){
        block =new Arc[]{
        /*0*/    new ArcSymbol(133, 1, 6),
        /*1*/    new ArcToken(lexer.new Token(3),2,0),
        /*2*/    new ArcSymbol('=', 3, 0),
        /*3*/    new ArcToken(lexer.new Token(2), 4,0),
        /*4*/    new ArcSymbol(',', 1, 5),
        /*5*/    new ArcSymbol(';', 7, 0),
        /*6*/    new ArcNil(),
        /*7*/    new ArcSymbol(140, 8,11),
        /*8*/    new ArcToken(lexer.new Token(3), 9,0),
        /*9*/    new ArcSymbol(',', 8, 10),
        /*10*/   new ArcSymbol(';', 12, 0),
        /*11*/   new ArcNil(),
        /*12*/   new ArcSymbol(138, 13, 17),
        /*13*/   new ArcToken(lexer.new Token(3), 14, 0),
        /*14*/   new ArcSymbol(';', 15, 0),
        /*15*/   new ArcGraph(block, 16, 0),
        /*16*/   new ArcSymbol(';', 12, 0),
        /*17*/   new ArcNil(),
        /*18*/   new ArcGraph(statement, 19, 0),
        /*E*/    new ArcEnd(0,0)
        };
        program=new Arc[]{
        /*0*/    new ArcGraph(block, 1, 0),
        /*1*/    new ArcSymbol('.',  2, 0),
        /*E*/    new ArcEnd(0,0)
        };
        statement =new Arc[]{
        /*0*/    new ArcToken(lexer.new Token(3), 1, 3),
        /*1*/    new ArcSymbol(128, 2, 0),
        /*2*/    new ArcGraph(expression, 22, 0),
        /*3*/    new ArcSymbol(136, 4, 7),
        /*4*/    new ArcGraph(condition, 5, 0),
        /*5*/    new ArcSymbol(139, 6, 0),
        /*6*/    new ArcGraph(statement, 22, 0),
        /*7*/    new ArcSymbol(141, 8, 11),
        /*8*/    new ArcGraph(condition, 9, 0),
        /*9*/    new ArcSymbol(134, 10, 0),
        /*10*/   new ArcGraph(statement, 22, 0),
        /*11*/   new ArcSymbol(131, 12, 15),
        /*12*/   new ArcGraph(statement, 13, 0),
        /*13*/   new ArcSymbol(';', 12, 14),
        /*14*/   new ArcSymbol(135, 22, 0),
        /*15*/   new ArcSymbol(131,16,17),
        /*16*/   new ArcToken(lexer.new Token(3), 22, 0),
        /*17*/   new ArcSymbol('?', 18, 19),
        /*18*/   new ArcToken(lexer.new Token(3), 22, 0),
        /*19*/   new ArcSymbol('!', 20, 21),
        /*20*/   new ArcGraph(expression, 22, 21),
        /*21*/   new ArcNil(),
        /*E*/    new ArcEnd(0, 0)
        };
        expression =new Arc[]{
        /*0*/    new ArcSymbol('-', 1, 2),
        /*1*/    new ArcGraph(term, 3, 0),
        /*2*/    new ArcGraph(term, 3, 0),
        /*3*/    new ArcNil(),
        /*4*/    new ArcSymbol('+', 6, 5),
        /*5*/    new ArcSymbol('-', 7, 8),
        /*6*/    new ArcGraph(term, 3, 0),
        /*7*/    new ArcGraph(term, 3, 0),
        /*8*/    new ArcNil(),
        /*E*/    new ArcEnd(0, 0),
        };
        term =new Arc[]{
        /*0*/    new ArcGraph(factor, 1, 0),
        /*1*/    new ArcNil(),
        /*2*/    new ArcSymbol('*', 3, 4),
        /*3*/    new ArcGraph(factor, 1, 0),
        /*4*/    new ArcSymbol('/', 5, 6),
        /*5*/    new ArcGraph(factor, 1, 0),
        /*6*/    new ArcNil(),
        /*E*/    new ArcEnd(0,0)
        };
        factor =new Arc[]{
        /*0*/    new ArcToken(lexer.new Token(2), 5, 1),
        /*1*/    new ArcSymbol('(', 2, 4),
        /*2*/    new ArcGraph(expression, 3, 0),
        /*3*/    new ArcSymbol(')', 5, 0),
        /*4*/    new ArcToken(lexer.new Token(3), 5, 0),
        /*E*/    new ArcEnd(0, 0),
        };
        condition = new Arc[]{
        /*0*/    new ArcSymbol(137, 1, 2),
        /*1*/    new ArcGraph(expression, 10, 0),
        /*2*/    new ArcGraph(expression, 3, 0),
        /*3*/    new ArcSymbol('=', 9, 4),
        /*4*/    new ArcSymbol('#', 9, 5),
        /*5*/    new ArcSymbol('<', 9, 6),
        /*6*/    new ArcSymbol('>', 9, 7),
        /*7*/    new ArcSymbol(129, 9, 8),
        /*8*/    new ArcSymbol(130, 9, 0),
        /*9*/    new ArcGraph(expression, 10, 0),
        /*10*/   new ArcEnd(0, 0)
        };
    }
    
    public static void main(String args[]){

    }
}


