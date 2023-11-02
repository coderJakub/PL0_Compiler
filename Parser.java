import java.io.*;

public class Parser extends Lexer{
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
        Arc graph;
        public ArcGraph(Arc g, int n, int a){
            super(n,a);
            graph=g;
        }
    }
    public class ArcEnd extends Arc{
        public ArcEnd(int n, int a){
            super(n,a);
        }
    }
    
    Arc[] program={
        new ArcGraph(new Arc(), 1, 0),
        new ArcSymbol('.',  2, 0),
        new ArcEnd(0,0)
    };

}


