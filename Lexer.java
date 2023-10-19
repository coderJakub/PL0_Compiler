import java.io.*;

public class Lexer{

    File f;
    FileInputStream fis;
    char x;
    state z;
    String buf;
    Token t;

    abstract public class Token{
        int len;
        abstract int getType();
        public Token(){
            len=0;
        }
    }
    //Nicht möglich da TokenTyp erst später bekannt wird :) LÖSUNG??
    public class NumToken extends Token{
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
    }

    public Lexer(String fileName)throws Exception{
        if(!fileName.contains(".pl0"))fileName+=".pl0";

        f=new File(fileName);
        if (!f.exists()|| !f.canRead())
        {
            System.out.println("Can't read "+f);
            return;
        }
        fis=new FileInputStream(f);
        x = (char)fis.read();
    }

    Token Lex(){
        z.nextS=0;
        state zx;
        //t=new Token(); ->Fehler untersuchen
        do{
            zx=automat[z.nextS][signClass[x]];
            zx.func();
        }while(z.nextS!=9);
        return t;
    }

    void l()throws Exception{
        x = (char)fis.read();
    }
    void sl()throws Exception{
        buf+=x;
        l();
    }
    // void b(){
    //     int i,j;
    //     switch(z.nextS){
    //         case 3:
    //         case 4:
    //         case 5:
    //         case 0:
    //             t.
    //     }
    // }

    abstract public class state{
        int nextS;
        public state(int state){
            nextS=state;
        }
        abstract void func();
    }
    public class stateSL extends state{
        public stateSL(int state){
            super(state);
        }
        void func(){
        }
    }
    public class stateSGL extends state{
        public stateSGL(int state){
            super(state);
        }
        void func(){

        }
    }
    public class stateL extends state{
        public stateL(int state){
            super(state);
        }
        void func(){

        }
    }
    public class stateB extends state{
        public stateB(int state){
            super(state);
        }
        void func(){

        }
    }
    public class stateSLB extends state{
        public stateSLB(int state){
            super(state);
        }
        void func(){

        }
    }
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

    //func: 0: keine, 1: sl, 2:sgl, 3:l, 4:b, 5:slb
    state[][] automat={
        /*z    So,                  Zif,                Bu,                    :,                   =,                    <,                   >,                   Steur   */
        /*------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
        /*0*/{new stateSLB(9),new stateSL(1),new stateSGL(2),new stateSL(3),new stateSLB(9),new stateSL(4),new stateSL(5),new stateB(9)},
        /*1*/{  new stateB(9),new stateSL(1),  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateB(9),new stateB(9)},
        /*2*/{  new stateB(9),new stateSL(2),new stateSGL(2), new stateB(9),  new stateB(9), new stateB(9), new stateB(9),new stateB(9)},
        /*3*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateSL(6), new stateB(9), new stateB(9),new stateB(9)},
        /*4*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateSL(7), new stateB(9), new stateB(9),new stateB(9)},
        /*5*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateSL(8), new stateB(9), new stateB(9),new stateB(9)},
        /*6*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateB(9),new stateB(9)},
        /*7*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateB(9),new stateB(9)},
        /*8*/{  new stateB(9), new stateB(9),  new stateB(9), new stateB(9),  new stateB(9), new stateB(9), new stateB(9),new stateB(9)}
    };

}