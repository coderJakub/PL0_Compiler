import java.io.*;

import java.util.*;

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

    ArrayList<Long> constBlock; 
    static Procedure mainProc;
    Procedure currentProc;
    String nameOfLastIndent;
    int procCounter=0;
    
    String code;

    File outFile;

    abstract public class Ident{
        int prozNum;  //Nummer der Prozedur zu welcher Variable gehört
        String name;  //Name der Variable
        public Ident(String s){
            prozNum =(currentProc==null)?0:currentProc.procIndex;
            name=s;
        }
        abstract LinkedList<Ident> getNameList(); //->nur fürs debuggen
    }

    public class Variable extends Ident{
        int address; //Relativaddresse der Variable bzgl. Prozedur
        public Variable(String s){
            super(s);
            address = currentProc.varAdress;
            currentProc.varAdress+=4;
            currentProc.namelist.add(this);
        }
        LinkedList<Ident> getNameList(){
            return null;
        }
    }

    public class Constant extends Ident{
        int constIndex; //Index der Konstante im Konstantenarray
        public Constant(long value, String s){
            super(s);
            if(constBlock.contains(value))
                constIndex = constBlock.indexOf(value);
            else{
                constBlock.add(value);
                constIndex = constBlock.indexOf(value);
            }
            currentProc.namelist.add(this);
        }
        LinkedList<Ident> getNameList(){
            return null;
        }
    }

    public class Procedure extends Ident{
        int procIndex; //Index der Procedure
        Procedure parent; //parent Prozedur -> parent-proc.idx == procNum 
        LinkedList<Ident> namelist; //Namensliste
        int varAdress; //relativadresse für nächste Variable die hinzugefügt wird
        public Procedure(String s){
            super(s);
            namelist = new LinkedList<Ident>();
            varAdress = 0;
            if(currentProc!=null)currentProc.namelist.add(this); //Fall Erstellung main-Proc
            parent = currentProc!=null?currentProc:null;
            procIndex = procCounter++;
        }
        LinkedList<Ident> getNameList(){
            return namelist;
        }
    }

    Ident searchIdent(Procedure p, String name){
        for(Ident i:p.namelist)if(i.name.equals(name))return i;
        return null;
    }
    Ident searchIdentGlobal(String name){
        Procedure p = currentProc;
        do{
            Ident i = searchIdent(p, name);
            if(i!=null)return i;
            p=p.parent;
        }while(p.procIndex==0);
        return null;
    }
    public void writeArg(int... arg){
        for(int i:arg){
            code+=i%0x100; System.out.println(i%0x100);
            code+=i/0x100; System.out.println(i/0x100);
        }
            
    }
    public String replaceAt(String s, int pos, String c) {
        return s.substring(0, pos) + c + s.substring(pos + c.length());
    }

    public void genCode(String command, int... args){
        if(args.length>3)System.exit(-1);
        code+=command;
        try{
            switch (command) {
                case "entryProc": writeArg(args[0],args[1], args[2]); System.out.println("he");break;
                case "puValVrGlob": 
                case "puAdrVrGlob": writeArg(args[0], args[1]); break;
                case "puValVrMain": 
                case "puAdrVrMain":  
                case "puValVrLocl": 
                case "puAdrVrLocl":
                case "puConst":
                case "jmp" : 
                case "jnot":
                case "call":writeArg(args[0]); break;
                default: System.out.println(command);break;
            }
        }catch(Exception e){
            System.out.println("Fehler beim generieren des Codes: Anzahl Parameter stimmt nicht überein!");
            System.exit(-1);
        }
    }
    public void writeCodeInFile(){
        try {
            FileOutputStream fos = new FileOutputStream(outFile.getName());
            fos.write(code.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public abstract class Arc{
        int next;
        int alt;

        Token token;
        int sym;
        Arc[] graph;
        
        boolean action(){
            return true; //true->funtion doesnt exist
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
            return t.sym==sym&&action() /*&& action() eigentlich anders rum!!*/;
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
            return t.type==token.type&&action();
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
            return parse(graph)&&action();
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
            return true;
        }
        String debug(){
            return "ArcEnd";
        }
    }
    
    public Parser(String filename){
        currentProc = new Procedure("main");
        mainProc=currentProc; //->nur fürs debuggen
        lexer = new Lexer(filename);
        constBlock = new ArrayList<Long>();
        t=new Token();
        outFile = new File(filename+".o");
        // try {
        //     if (outFile.createNewFile()) {
        //         System.out.println("Datei wurde erstellt: " + outFile.getName());
        //     } else {
        //         System.out.println("Die Datei existiert bereits.");
        //     }
        // } catch (IOException e) {
        //     System.out.println("Fehler beim Erstellen der Datei: " + e.getMessage());
        // }

        statement[0] = new ArcToken(lexer.new Token(3), 1, 3);
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
        statement[22] = new ArcEnd();

        block[0] = new ArcSymbol(133, 1, 6);
        block[1] = new ArcToken(lexer.new Token(3), 2, 0){boolean action(){
            if(searchIdent(currentProc, t.str)!=null)return false;
            nameOfLastIndent=t.str;
            return true;
        }};
        block[2] = new ArcSymbol('=', 3, 0);
        block[3] = new ArcToken(lexer.new Token(2), 4, 0){boolean action(){
            new Constant(t.num, nameOfLastIndent);
            return true;
        }};
        block[4] = new ArcSymbol(',', 1, 5);
        block[5] = new ArcSymbol(';', 7, 0);
        block[6] = new ArcNil(7);
        block[7] = new ArcSymbol(140, 8, 11);
        block[8] = new ArcToken(lexer.new Token(3), 9, 0){boolean action(){
            if(searchIdent(currentProc, t.str)!=null)return false;
            new Variable(t.str);
            return true;
        }};
        block[9] = new ArcSymbol(',', 8, 10);
        block[10] = new ArcSymbol(';', 12, 0);
        block[11] = new ArcNil(12);
        block[12] = new ArcSymbol(138, 13, 17);
        block[13] = new ArcToken(lexer.new Token(3), 14, 0){boolean action(){
            if(searchIdent(currentProc, t.str)!=null)return false;
            currentProc = new Procedure(t.str);
            return true;
        }};
        block[14] = new ArcSymbol(';', 15, 0);
        block[15] = new ArcGraph(block, 16, 0);
        block[16] = new ArcSymbol(';', 12, 0);
        block[17] = new ArcNil(18){boolean action(){
            code="";
            genCode("entryProc", 0, currentProc.procIndex, currentProc.varAdress);
            return true;
        }};
        block[18] = new ArcGraph(statement, 19, 0){boolean action(){
            genCode("retProc");
            code=replaceAt(code, 9, ""+code.length());
            writeCodeInFile();
            currentProc.namelist.clear();
            currentProc = currentProc.parent;
            return true;
        }};
        block[19] = new ArcEnd();
        
        program[0] = new ArcGraph(block, 1, 0);
        program[1] = new ArcSymbol('.', 2, 0);
        program[2] = new ArcEnd();
        
        expression[0] = new ArcSymbol('-', 1, 2);
        expression[1] = new ArcGraph(term, 3, 0);
        expression[2] = new ArcGraph(term, 3, 0);
        expression[3] = new ArcNil(4);
        expression[4] = new ArcSymbol('+', 6, 5);
        expression[5] = new ArcSymbol('-', 7, 8);
        expression[6] = new ArcGraph(term, 3, 0);
        expression[7] = new ArcGraph(term, 3, 0);
        expression[8] = new ArcNil(9);
        expression[9] = new ArcEnd();
        
        term[0] = new ArcGraph(factor, 1, 0);
        term[1] = new ArcNil(2);
        term[2] = new ArcSymbol('*', 3, 4);
        term[3] = new ArcGraph(factor, 1, 0);
        term[4] = new ArcSymbol('/', 5, 6);
        term[5] = new ArcGraph(factor, 1, 0);
        term[6] = new ArcNil(7);
        term[7] = new ArcEnd();
        
        factor[0] = new ArcToken(lexer.new Token(2), 5, 1);
        factor[1] = new ArcSymbol('(', 2, 4);
        factor[2] = new ArcGraph(expression, 3, 0);
        factor[3] = new ArcSymbol(')', 5, 0);
        factor[4] = new ArcToken(lexer.new Token(3), 5, 0);
        factor[5] = new ArcEnd();
        
        condition[0] = new ArcSymbol(137, 1, 2);
        condition[1] = new ArcGraph(expression, 10, 0);
        condition[2] = new ArcGraph(expression, 3, 0);
        condition[3] = new ArcSymbol('=', 9, 4);
        condition[4] = new ArcSymbol('#', 9, 5);
        condition[5] = new ArcSymbol('<', 9, 6);
        condition[6] = new ArcSymbol('>', 9, 7);
        condition[7] = new ArcSymbol(129, 9, 8);
        condition[8] = new ArcSymbol(130, 9, 0);
        condition[9] = new ArcGraph(expression, 10, 0);
        condition[10] = new ArcEnd();   
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
    int i=0;

    void printVersatz(){
            for(int j=0; j<i; j++)System.out.print(" ");
    }
    String identClass(Ident id){
        if(id instanceof Procedure)return "Procedure";
        if(id instanceof Variable)return "Variable";
        if(id instanceof Constant)return "Constant";
        else return "Ident";

    }
    void printNamelist(LinkedList<Ident> namelist){
        int j=0;
        for(int k=0; k<namelist.size(); k++){
            printVersatz(); System.out.println(j+ ": "+ identClass(namelist.get(k))+ " - "+ namelist.get(k).name);
            if(namelist.get(k) instanceof Procedure){
                i++;
                printNamelist(namelist.get(k).getNameList());
                i--;
            }
            j++;
        }
    }

    public static void main(String args[]){
        Parser parser = new Parser(args[0]);
        if(parser.parse(parser.program))System.out.println("Parsen erfolgreich!");
        else System.out.println("Parsen nicht erfolgreich! Fehler bei Zeile "+ parser.t.posCol+ ", Zeichen: "+ parser.t.posLine);
        //parser.printNamelist(mainProc.namelist); //--> show namelists (debug)
        //for(Long i:parser.constBlock)System.out.println(i); --> show constBlock (debug)
    }
}