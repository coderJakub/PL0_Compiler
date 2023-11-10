public class GraphenBesser extends Lexer{
    Lexer lexer;
    Token t;
    Arc[] block;
    Arc[] program;
    Arc[] statement;
    Arc[] expression;
    Arc[] term;
    Arc[] factor;
    Arc[] condition;

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
        statement =new Arc[]{
        /*0*/    new ArcToken(lexer.new Token(3), 1, 3){boolean action(){System.out.println("Enter Statement");return true;}},
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
        /*21*/   new ArcNil(22),
        /*E*/    new ArcEnd(){boolean action(){System.out.println("Exit Statement");return true;}}
        };
        block =new Arc[]{
        /*0*/    new ArcSymbol(133, 1, 6){boolean action(){System.out.println("Enter Block");return true;}},
        /*1*/    new ArcToken(lexer.new Token(3),2,0),
        /*2*/    new ArcSymbol('=', 3, 0),
        /*3*/    new ArcToken(lexer.new Token(2), 4,0),
        /*4*/    new ArcSymbol(',', 1, 5),
        /*5*/    new ArcSymbol(';', 7, 0),
        /*6*/    new ArcNil(7),
        /*7*/    new ArcSymbol(140, 8,11),
        /*8*/    new ArcToken(lexer.new Token(3), 9,0),
        /*9*/    new ArcSymbol(',', 8, 10),
        /*10*/   new ArcSymbol(';', 12, 0),
        /*11*/   new ArcNil(12),
        /*12*/   new ArcSymbol(138, 13, 17),
        /*13*/   new ArcToken(lexer.new Token(3), 14, 0),
        /*14*/   new ArcSymbol(';', 15, 0),
        /*15*/   new ArcGraph(block, 16, 0),
        /*16*/   new ArcSymbol(';', 12, 0),
        /*17*/   new ArcNil(18),
        /*18*/   new ArcGraph(statement, 19, 0),
        /*E*/    new ArcEnd(){boolean action(){System.out.println("Exit Block");return true;}}
        };
        program=new Arc[]{
        /*0*/    new ArcGraph(block, 1, 0){boolean action(){System.out.println("Enter Program");return true;}},
        /*1*/    new ArcSymbol('.',  2, 0),
        /*E*/    new ArcEnd(){boolean action(){System.out.println("Exit Program");return true;}}
        };
        expression =new Arc[]{
        /*0*/    new ArcSymbol('-', 1, 2){boolean action(){System.out.println("Enter Expression");return true;}},
        /*1*/    new ArcGraph(term, 3, 0),
        /*2*/    new ArcGraph(term, 3, 0),
        /*3*/    new ArcNil(4),
        /*4*/    new ArcSymbol('+', 6, 5),
        /*5*/    new ArcSymbol('-', 7, 8),
        /*6*/    new ArcGraph(term, 3, 0),
        /*7*/    new ArcGraph(term, 3, 0),
        /*8*/    new ArcNil(9),
        /*E*/    new ArcEnd(){boolean action(){System.out.println("Exit Expression");return true;}}
        };
        term =new Arc[]{
        /*0*/    new ArcGraph(factor, 1, 0){boolean action(){System.out.println("Enter Term");return true;}},
        /*1*/    new ArcNil(2),
        /*2*/    new ArcSymbol('*', 3, 4),
        /*3*/    new ArcGraph(factor, 1, 0),
        /*4*/    new ArcSymbol('/', 5, 6),
        /*5*/    new ArcGraph(factor, 1, 0),
        /*6*/    new ArcNil(7),
        /*E*/    new ArcEnd(){boolean action(){System.out.println("Exit Term");return true;}}
        };
        factor =new Arc[]{
        /*0*/    new ArcToken(lexer.new Token(2), 5, 1){boolean action(){System.out.println("Enter Factor");return true;}},
        /*1*/    new ArcSymbol('(', 2, 4),
        /*2*/    new ArcGraph(expression, 3, 0),
        /*3*/    new ArcSymbol(')', 5, 0),
        /*4*/    new ArcToken(lexer.new Token(3), 5, 0),
        /*E*/    new ArcEnd(){boolean action(){System.out.println("Exit Factor");return true;}}
        };
        condition = new Arc[]{
        /*0*/    new ArcSymbol(137, 1, 2){boolean action(){System.out.println("Enter Condition");return true;}},
        /*1*/    new ArcGraph(expression, 10, 0),
        /*2*/    new ArcGraph(expression, 3, 0),
        /*3*/    new ArcSymbol('=', 9, 4),
        /*4*/    new ArcSymbol('#', 9, 5),
        /*5*/    new ArcSymbol('<', 9, 6),
        /*6*/    new ArcSymbol('>', 9, 7),
        /*7*/    new ArcSymbol(129, 9, 8),
        /*8*/    new ArcSymbol(130, 9, 0),
        /*9*/    new ArcGraph(expression, 10, 0),
        /*10*/   new ArcEnd(){boolean action(){System.out.println("Exit Condition");return true;}}
        };
    }
    boolean parse(Arc graph[]){
        boolean succ=false;
        Arc bogen = graph[0];
        if(t.type==0)t=lexer.Lex();
        //System.out.println("Token: "+t.type);
        while(true){
            for(int i=0; i<graph.length; i++)if(graph[i]==bogen)System.out.print("->"+i+" ");
            System.out.println(bogen.debug());
            succ = bogen.compareArc();
            if(bogen instanceof ArcEnd)return true;
            if(!succ){
                System.out.println("                           Suche nach Alternativbogen");
                if(bogen.alt!=0)bogen=graph[bogen.alt];
                else return false;
            }
            else{
                if(bogen instanceof ArcSymbol || bogen instanceof ArcToken)t=lexer.Lex();
                bogen=graph[bogen.next];
            }
        }
    }
    public static void main(String args[]){
        Parser parser = new Parser(args[0]);
        parser.parse(parser.program);
    }
}
