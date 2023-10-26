import java.io.*;

public class Lexer{
    File f;
    FileInputStream fis;
    char x;
    String buf;
    Token t;
    int state;
    String[] keywords = {"BEGIN", "CALL", "CONST", "DO", "END", "IF", "ODD", "PROCEDURE", "THEN", "VAR", "WHILE"};

    //Definieren der Zeichenklassen für kleinere Automatentabelle
    // 0:Sonderzeichen, 1: Ziffer, 2:Buchstabe 3: : 4: = 5: < 6: > 7: sonstige Steuerzeichen 
    char[] signClass={
        /*     0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F */
        /*---------------------------------------------------------*/
        /* 0*/ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, /* 0*/
        /*10*/ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, /*10*/
        /*20*/ 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, /*20*/
        /*30*/ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 0, 5, 4, 6, 0, /*30*/
        /*40*/ 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, /*40*/
        /*50*/ 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, /*50*/
        /*60*/ 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, /*60*/
        /*70*/ 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0  /*70*/
    };

    //Automatentabelle (Zustand 9: Endzustand)
    state[][] automat={
        /*z    So,                  Zif,                Bu,                    :,                   =,                    <,                   >,                   Steur   */
        /*------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
        /*0*/{new stateSLB(9),new stateSL(1),new stateSGL(2),new stateSL(3),new stateSLB(9),new stateSL(4),new stateSL(5),new stateL(0)},
        /*1*/{  new stateB(9),new stateSL(1),  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateB(9),new stateB(9)},
        /*2*/{  new stateB(9),new stateSL(2),new stateSGL(2), new stateB(9),  new stateB(9), new stateB(9), new stateB(9),new stateB(9)},
        /*3*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateSL(6), new stateB(9), new stateB(9),new stateB(9)},
        /*4*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateSL(7), new stateB(9), new stateB(9),new stateB(9)},
        /*5*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateSL(8), new stateB(9), new stateB(9),new stateB(9)},
        /*6*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateB(9),new stateB(9)},
        /*7*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateB(9),new stateB(9)},
        /*8*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateB(9),new stateB(9)}
    };

    //Hauptzustandsklasse
    abstract public class state{
        int nextS;
        public state(int state){
            nextS=state;
        }
        abstract void func();
    }

    //Unterzustandsklassen
    //Zustand mit Schreib- und lesefunktion
    public class stateSL extends state{
        public stateSL(int state){
            super(state);
        }
        void func(){
            sl();
        }
    }

    //Zustand mit Schreib- und lesefunktion (Kleinbuchstaben werden zu Großbuchstaben)
    public class stateSGL extends state{
        public stateSGL(int state){
            super(state);
        }
        void func(){
            x=Character.toUpperCase(x);  //-=(x>65&&x<90)?0:(97-65);
            sl();
        }
    }

    //Zustand mit Schreibfunktion
    public class stateL extends state{
        public stateL(int state){
            super(state);
        }
        void func(){
            l();
        }
    }

    //Zustand mit Beeendenfunktion
    public class stateB extends state{
        public stateB(int state){
            super(state);
        }
        void func(){
            b();
        }
    }

    //Zustand mit Schreib-, Lese- und Beendenfunktion
    public class stateSLB extends state{
        public stateSLB(int state){
            super(state);
        }
        void func(){
            sl(); b();
        }
    }

    //Tokenklasse
    public static class Token{ //static, da sonst nicht von Test.java aus zugreifbar -> Lösung? 
        int len;
        int type; //0->Empty, 1->Sym, 2->Num, 3->Ident, 4->Keyword
        int posLine;
        int posCol;
        //Data
        long num;
        String str;
        int sym; //:=128, <=129, >=130

        public Token(){
            len=0;
            type=0;
            posLine=0;
            posCol=0;
            num=0;
            str="";
            sym=0;
        }
    }
    //Schlecht zu implementieren, da Tokenklasse bekannt sein muss um Datentyp des Inhalts zu bestimmen -> obere Variante besser?
    /*public class NumToken extends Token{
        long num;
        int getType(){
            return 0;
        }
    }
    public class StrToken extends Token{
        String str;
        int getType(){
            return 1;
        }
    }
    public class SymToken extends Token{
        int sym;
        int getType(){
            return 2;
        }
    }*/

    //Konstruktoren
    public Lexer(){};
    public Lexer(String fileName){
        if(!fileName.contains(".pl0"))fileName+=".pl0";

        f=new File(fileName);
        if (!f.exists()|| !f.canRead())
        {
            System.out.println("Can't read "+f);
            return;
        }
        try{
            fis=new FileInputStream(f);
            x = (char)fis.read();  
        }
        catch(Exception e){
            System.out.println("Can't read "+f);
            return;
        }
    }

    //Lexfunktion
    Token Lex(){
        state zx;
        t=new Token();
        buf=""; //Aktuell nicht ein Buchstabe im Vorraus bekannt -> Puffer nicht komplett löschen?
        state=0;
        do{
            zx=automat[state][signClass[x]];
           // System.out.println(zx.getClass().getName()+state);
            zx.func();
            state= zx.nextS;
        }while(zx.nextS!=9);
        return t;
    }

    //Hilfsfunktionen für Zustandsklassen
    void l(){
        try{
            x = (char)fis.read();   
        }
        catch(Exception e){
            System.out.println("Can't read "+f);
            return;
        }
    }
    void sl(){
        buf+=x;
        //System.out.println(buf);
        l();
    }
    void b(){
        //System.out.println(buf+" "+state);
        switch(state){
            case 3:
            case 4:
            case 5:
            case 0:
                t.sym=buf.charAt(0);
                t.type=1;
                break;
            case 1:
                t.num=Long.parseLong(buf);
                t.type=2;
                break;
            case 6:
                t.sym=128;
                t.type=1;
                break;
            case 7:
                t.sym=129;
                t.type=1;
                break;
            case 8:
                t.sym=130;
                t.type=1;
                break;
            case 2:
                boolean kw =false;

                for(String s:keywords)if(s.equals(buf))kw=true;

                t.str=buf;
                t.type=(kw)?4:3;
                break;
        }
    }
}