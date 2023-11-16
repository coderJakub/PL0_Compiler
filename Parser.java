//import java.io.*;

public class Parser extends Lexer{
    Lexer lexer;
    Token t;
    Arc[] block=new Arc[20];
    Arc[] program=new Arc[3];
    Arc[] statement=new Arc[23];
    Arc[] expression=new Arc[11];
    Arc[] term=new Arc[8];
    Arc[] factor=new Arc[6];
    Arc[] condition=new Arc[11];

    public abstract class Arc{
        int next;
        int alt;

        Token token;
        int sym;
        Arc[] graph;
        
        boolean action(){
            return true; //-1->funtion doesnt exist
        }
        public Arc(){
            next=alt=0;
        }
        public Arc(int n, int a){
            next=n;
            alt=a;
        }
        abstract boolean compareArc();
        abstract String debug();
    }
    public class ArcNil extends Arc{
        public ArcNil(int n){
            super(n,0);
        }
        boolean compareArc(){
            return action();
        }
        String debug(){
            return "ArcNil";
        }
    }
    
    public class ArcSymbol extends Arc{
        public ArcSymbol(int s, int n, int a){
            super(n,a);
            sym=s;
        }
        boolean compareArc(){
            return action()&&t.sym==sym /*&& action() eigentlich anders rum!!*/;
        }
        String debug(){
            return "ArcSymbol: "+sym;
        }
    }
    public class ArcToken extends Arc{
        public ArcToken(Token t, int n, int a){
            super(n,a);
            token=t;
        }
        boolean compareArc(){
            return action()&&t.type==token.type/*&& action() eigentlich anders rum!!*/;
        }
        String debug(){
            return "ArcToken: "+token.type;
        }
    }
    public class ArcGraph extends Arc{
        public ArcGraph(Arc[] g, int n, int a){
            super(n,a);
            graph=g;
        }
        boolean compareArc(){
            return action() && parse(graph) /*&& action() eigentlich anders rum!!*/;
        }
        String debug(){
            return "ArcGraph";
        }
    }
    public class ArcEnd extends Arc{
        public ArcEnd(){
            super(0,0);
        }
        boolean compareArc(){
            return action() /*Eigentlich nur true!! */;
        }
        String debug(){
            return "ArcEnd";
        }
    }
    
    public Parser(String filename){
        lexer = new Lexer(filename);
        t=new Token();
        statement[0] = new ArcToken(lexer.new Token(3), 1, 3){boolean action(){System.out.println("Enter Statement");return true;}};
        statement[1] = new ArcSymbol(128, 2, 0);
        statement[2] = new ArcGraph(expression, 22, 0);
        statement[3] = new ArcSymbol(136, 4, 7);
        statement[4] = new ArcGraph(condition, 5, 0);
        statement[5] = new ArcSymbol(139, 6, 0);
        statement[6] = new ArcGraph(statement, 22, 0);
        statement[7] = new ArcSymbol(141, 8, 11);
        statement[8] = new ArcGraph(condition, 9, 0);
        statement[9] = new ArcSymbol(134, 10, 0);
        statement[10] = new ArcGraph(statement, 22, 0);
        statement[11] = new ArcSymbol(131, 12, 15);
        statement[12] = new ArcGraph(statement, 13, 0);
        statement[13] = new ArcSymbol(';', 12, 14);
        statement[14] = new ArcSymbol(135, 22, 0);
        statement[15] = new ArcSymbol(132, 16, 17);
        statement[16] = new ArcToken(lexer.new Token(3), 22, 0);
        statement[17] = new ArcSymbol('?', 18, 19);
        statement[18] = new ArcToken(lexer.new Token(3), 22, 0);
        statement[19] = new ArcSymbol('!', 20, 21);
        statement[20] = new ArcGraph(expression, 22, 21);
        statement[21] = new ArcNil(22);
        statement[22] = new ArcEnd(){boolean action(){System.out.println("Exit Statement");return true;}};

        block[0] = new ArcSymbol(133, 1, 6) { boolean action() { System.out.println("Enter Block"); return true; } };
        block[1] = new ArcToken(lexer.new Token(3), 2, 0);
        block[2] = new ArcSymbol('=', 3, 0);
        block[3] = new ArcToken(lexer.new Token(2), 4, 0);
        block[4] = new ArcSymbol(',', 1, 5);
        block[5] = new ArcSymbol(';', 7, 0);
        block[6] = new ArcNil(7);
        block[7] = new ArcSymbol(140, 8, 11);
        block[8] = new ArcToken(lexer.new Token(3), 9, 0);
        block[9] = new ArcSymbol(',', 8, 10);
        block[10] = new ArcSymbol(';', 12, 0);
        block[11] = new ArcNil(12);
        block[12] = new ArcSymbol(138, 13, 17);
        block[13] = new ArcToken(lexer.new Token(3), 14, 0);
        block[14] = new ArcSymbol(';', 15, 0);
        block[15] = new ArcGraph(block, 16, 0);
        block[16] = new ArcSymbol(';', 12, 0);
        block[17] = new ArcNil(18);
        block[18] = new ArcGraph(statement, 19, 0);
        block[19] = new ArcEnd() { boolean action() { System.out.println("Exit Block"); return true; } };
        
        program[0] = new ArcGraph(block, 1, 0) { boolean action() { System.out.println("Enter Program"); return true; } };
        program[1] = new ArcSymbol('.', 2, 0);
        program[2] = new ArcEnd() { boolean action() { System.out.println("Exit Program"); return true; } };
        
        expression[0] = new ArcSymbol('-', 1, 2) { boolean action() { System.out.println("Enter Expression"); return true; } };
        expression[1] = new ArcGraph(term, 3, 0);
        expression[2] = new ArcGraph(term, 3, 0);
        expression[3] = new ArcNil(4);
        expression[4] = new ArcSymbol('+', 6, 5);
        expression[5] = new ArcSymbol('-', 7, 8);
        expression[6] = new ArcGraph(term, 3, 0);
        expression[7] = new ArcGraph(term, 3, 0);
        expression[8] = new ArcNil(9);
        expression[9] = new ArcEnd() { boolean action() { System.out.println("Exit Expression"); return true; } };
        
        term[0] = new ArcGraph(factor, 1, 0) { boolean action() { System.out.println("Enter Term"); return true; } };
        term[1] = new ArcNil(2);
        term[2] = new ArcSymbol('*', 3, 4);
        term[3] = new ArcGraph(factor, 1, 0);
        term[4] = new ArcSymbol('/', 5, 6);
        term[5] = new ArcGraph(factor, 1, 0);
        term[6] = new ArcNil(7);
        term[7] = new ArcEnd() { boolean action() { System.out.println("Exit Term"); return true; } };
        
        factor[0] = new ArcToken(lexer.new Token(2), 5, 1) { boolean action() { System.out.println("Enter Factor"); return true; } };
        factor[1] = new ArcSymbol('(', 2, 4);
        factor[2] = new ArcGraph(expression, 3, 0);
        factor[3] = new ArcSymbol(')', 5, 0);
        factor[4] = new ArcToken(lexer.new Token(3), 5, 0);
        factor[5] = new ArcEnd() { boolean action() { System.out.println("Exit Factor"); return true; } };
        
        condition[0] = new ArcSymbol(137, 1, 2) { boolean action() { System.out.println("Enter Condition"); return true; } };
        condition[1] = new ArcGraph(expression, 10, 0);
        condition[2] = new ArcGraph(expression, 3, 0);
        condition[3] = new ArcSymbol('=', 9, 4);
        condition[4] = new ArcSymbol('#', 9, 5);
        condition[5] = new ArcSymbol('<', 9, 6);
        condition[6] = new ArcSymbol('>', 9, 7);
        condition[7] = new ArcSymbol(129, 9, 8);
        condition[8] = new ArcSymbol(130, 9, 0);
        condition[9] = new ArcGraph(expression, 10, 0);
        condition[10] = new ArcEnd(){ boolean action() { System.out.println("Exit Condition"); return true; } };        
    }
    boolean parse(Arc graph[]){
        boolean succ=false;
        Arc bogen = graph[0];
        if(t.type==0)t=lexer.Lex();
        while(true){
            // for(int i=0; i<graph.length; i++)if(graph[i]==bogen)System.out.print("->"+i+" ");
            // System.out.println(bogen.debug());
            succ = bogen.compareArc();
            if(bogen instanceof ArcEnd)return true;
            if(!succ){
            //    System.out.println("                           Suche nach Alternativbogen");
                if(bogen.alt!=0)bogen=graph[bogen.alt];
                else return false;
            }
            else{
                if(bogen instanceof ArcSymbol || bogen instanceof ArcToken)t=lexer.Lex(); //wird bei programGraph nachdem punkt gelesen wurde noch einmal ausgeführt (aber kein Zeichen mehr vorhanden...) ->lexer wie besser lösen
                bogen=graph[bogen.next];
            }
        }
    }
    public static void main(String args[]){
        Parser parser = new Parser(args[0]);
        System.out.println(parser.parse(parser.program));
    }
}