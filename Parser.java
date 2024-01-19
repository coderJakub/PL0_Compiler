import java.io.*;

import java.util.*;

public class Parser extends Lexer{
    Lexer lexer;
    Token t;
    Arc[] block=new Arc[20];
    Arc[] program=new Arc[3];
    Arc[] statement=new Arc[27];
    Arc[] expression=new Arc[11];
    Arc[] term=new Arc[8];
    Arc[] factor=new Arc[6];
    Arc[] condition=new Arc[11];

    ArrayList<Long> constBlock; 
    static Procedure mainProc;
    Procedure currentProc;
    String nameOfLastIndent;
    int procCounter=0;
    
    byte[] code;
    int idxCode;
    ByteArrayOutputStream baos;
    DataOutputStream dos;

    File outFile;
    FileOutputStream fos;
    Stack<Short> labels = new Stack<Short>();
    int comparisonOperator;

/************************************************************************************/
/*********************Definition der Namenslisten-Klassen****************************/
/************************************************************************************/

    abstract public class Ident{
        int prozNum;  //Nummer der Prozedur zu welcher Variable gehört
        String name;  //Name der Variable
        public Ident(String s){
            prozNum =(currentProc==null)?0:currentProc.procIndex;
            name=s;
        }
        abstract short getAddress();
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
        @Override
        short getAddress() {
            return (short)address;
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
        @Override
        short getAddress() {
            return -1;
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
        @Override
        short getAddress() {
            return -1;
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
        }while(p.procIndex!=0);
        Ident i = searchIdent(mainProc, name);
        if(i!=null)return i;
        return null;
    }

/************************************************************************************/
/*************************Funktionen der Codegenerierung*****************************/
/************************************************************************************/
    public void writeShortToByteArray(short value) {
        baos.write(value & 0xFF);
        baos.write((value >> 8) & 0xFF);
    }
    public void writeIntToByteArray(int value) {
        baos.write(value & 0xFF);
        baos.write((value >> 8) & 0xFF);
        baos.write((value >> 16) & 0xFF);
        baos.write((value >> 24) & 0xFF);
    }
    public void writeStringToByteArray(String value) {
        for(int i=0; i<value.length(); i++)baos.write(value.charAt(i));
    }


    public void writeCommand(String command){
        switch (command) {
            case "puValVrLocl": baos.write(0x00); break;
            case "puValVrMain": baos.write(0x01); break;
            case "puValVrGlob": baos.write(0x02); break;
            case "puAdrVrLocl": baos.write(0x03); break;
            case "puAdrVrMain": baos.write(0x04); break;
            case "puAdrVrGlob": baos.write(0x05); break;
            case "puConst":     baos.write(0x06); break;
            case "storeVal":    baos.write(0x07); break;
            case "putVal":      baos.write(0x08); break;
            case "getVal":      baos.write(0x09); break;
            case "vzMinus":     baos.write(0x0A); break;
            case "odd":         baos.write(0x0B); break;
            case "OpAdd":       baos.write(0x0C); break;
            case "OpSub":       baos.write(0x0D); break;
            case "OpMult":      baos.write(0x0E); break;
            case "OpDiv":       baos.write(0x0F); break;
            case "cmpEQ":       baos.write(0x10); break;
            case "cmpNE":       baos.write(0x11); break;
            case "cmpLT":       baos.write(0x12); break;
            case "cmpGT":       baos.write(0x13); break;
            case "cmpLE":       baos.write(0x14); break;
            case "cmpGE":       baos.write(0x15); break;
            case "call":        baos.write(0x16); break;
            case "retProc":     baos.write(0x17); break;
            case "jmp":         baos.write(0x18); break;
            case "jnot":        baos.write(0x19); break;
            case "entryProc":   baos.write(0x1A); break;
            case "putStrg":     baos.write(0x1B); break;
            default:
                break;
        }
    }
    public void writeArg(short... arg){
        for(short i:arg){
            //System.out.print(" "+i);
            writeShortToByteArray(i);
        }  
    }
    public void replaceAt(int position, short value) {
        System.out.println("replaceAt: "+position+" "+value);
        byte[] bytes = baos.toByteArray();
        bytes[position] = (byte) (value & 0xFF);
        bytes[position + 1] = (byte) ((value >> 8) & 0xFF);
        baos.reset();
        baos.write(bytes, 0, bytes.length);
    }

    public void genCode(String command, int... args){
        if(args.length>3)System.exit(-1);
        writeCommand(command);
        //System.out.print(command);
        try{
            switch (command) {
                case "entryProc": writeArg((short)args[0],(short)args[1], (short)args[2]); break;
                case "puValVrGlob": 
                case "puAdrVrGlob": writeArg((short)args[0], (short)args[1]); break;
                case "puValVrMain": 
                case "puAdrVrMain":  
                case "puValVrLocl": 
                case "puAdrVrLocl":
                case "puConst":
                case "jmp" : 
                case "jnot":
                case "call":writeArg((short)args[0]); break;
                default: break;
            }
            //System.out.println("");
        }catch(Exception e){
            System.out.println("Fehler beim generieren des Codes: Anzahl Parameter stimmt nicht überein!");
            System.exit(-1);
        }
    }
    public void writeCodeInFile(){
        try {
            fos.write(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeProcNum(){
        try{
            RandomAccessFile raf = new RandomAccessFile(outFile.getName(), "rw");
            raf.writeByte(procCounter & 0xFF);
            raf.writeByte((procCounter >> 8) & 0xFF);
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

/************************************************************************************/
/***********************Definition der Bogen-Klassen*********************************/
/************************************************************************************/

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

/************************************************************************************/
/*************************Konstruktor mit Graphendefinition**************************/
/************************************************************************************/

    public Parser(String filename){
        currentProc = new Procedure("main");
        mainProc=currentProc; //->nur fürs debuggen
        lexer = new Lexer(filename);
        constBlock = new ArrayList<Long>();
        t=new Token();

        System.out.println(filename);
        System.out.println("src-cl0/"+filename.split("/")[filename.split("/").length-1].split(".pl0")[0]+".cl0");

        outFile = new File("src-cl0/"+filename.split("/")[filename.split("/").length-1].split(".pl0")[0]+".cl0");
        baos = new ByteArrayOutputStream();
        writeShortToByteArray((short)1);
        writeShortToByteArray((short)0);

        try{
            fos = new FileOutputStream(outFile.getName());
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        writeCodeInFile();

        statement[0] = new ArcToken(lexer.new Token(3), 1, 3){boolean action(){
            Ident i = searchIdentGlobal(t.str);
            if(i==null || !(i instanceof Variable)){
                if(i==null)System.out.println("Variable "+t.str+" nicht gefunden!");
                System.out.println("Fehler: "+t.str+" ist keine Variable!");
                return false;
            }
            if(i.prozNum==0)genCode("puAdrVrMain", ((Variable)i).address);
            else if(i.prozNum==currentProc.procIndex)genCode("puAdrVrLocl", ((Variable)i).address);
            else genCode("puAdrVrGlob", ((Variable)i).address, i.prozNum);
            return true;
        }};
        statement[1] = new ArcSymbol(128, 2, 0);
        statement[2] = new ArcGraph(expression, 22, 0){boolean action(){
            genCode("storeVal");
            return true;
        }};
        statement[3] = new ArcSymbol(136, 4, 7);
        statement[4] = new ArcGraph(condition, 5, 0){boolean action(){
            labels.push((short)(baos.size()+1));
            genCode("jnot", (short)0);
            return true;
        }};
        statement[5] = new ArcSymbol(139, 6, 0);
        statement[6] = new ArcGraph(statement, 23, 0);
        statement[7] = new ArcSymbol(141, 8, 11){boolean action(){
            labels.push((short)(baos.size()));
            return true;
        }};
        statement[8] = new ArcGraph(condition, 9, 0){boolean action(){
            labels.push((short)(baos.size()+1));
            genCode("jnot", (short)0);
            return true;
        }};
        statement[9] = new ArcSymbol(134, 10, 0);
        statement[10] = new ArcGraph(statement, 22, 0){boolean action(){
            short labelJMPN = labels.pop();
            short labelJMP = labels.pop();
            genCode("jmp", (short) labelJMP-(baos.size()+3));
            replaceAt(labelJMPN, (short)(baos.size()-labelJMPN-2));
            return true;
        }};
        statement[11] = new ArcSymbol(131, 12, 15);
        statement[12] = new ArcGraph(statement, 13, 0);
        statement[13] = new ArcSymbol(';', 12, 14);
        statement[14] = new ArcSymbol(135, 22, 0);
        statement[15] = new ArcSymbol(132, 16, 17);
        statement[16] = new ArcToken(lexer.new Token(3), 22, 0){boolean action(){
            Ident i = searchIdentGlobal(t.str);
            if(i==null || !(i instanceof Procedure)){
                if(i==null)System.out.println("Prozedur "+t.str+" nicht gefunden!");
                else System.out.println("Fehler: "+t.str+" ist keine Prozedur!");
                return false;
            }
            genCode("call", ((Procedure)i).procIndex);
            return true;
        }};
        statement[17] = new ArcSymbol('?', 18, 19);
        statement[18] = new ArcToken(lexer.new Token(3), 22, 0){boolean action(){
            Ident i = searchIdentGlobal(t.str);
            if(i==null || !(i instanceof Variable)){
                if(i==null)System.out.println("Variable "+t.str+" nicht gefunden!");
                else System.out.println("Fehler: "+t.str+" ist keine Variable!");
                return false;
            }
            
            if(i.prozNum==0)genCode("puAdrVrMain", ((Variable)i).address);
            else if(i.prozNum==currentProc.procIndex)genCode("puAdrVrLocl", ((Variable)i).address);
            else genCode("puAdrVrGlob", ((Variable)i).address, i.prozNum);
            genCode("getVal");
            return true;
        
        }};
        statement[19] = new ArcSymbol('!', 26, 21);
        statement[20] = new ArcGraph(expression, 22, 0){boolean action(){
            genCode("putVal");
            return true;
        }};
        statement[21] = new ArcNil(22);
        statement[22] = new ArcEnd();
        statement[23] = new ArcSymbol(142, 25, 24){boolean action(){
            short labelElse = labels.pop();
            labels.push((short)(baos.size()+1));
            genCode("jmp", (short)0);

            replaceAt(labelElse, (short)(baos.size()-labelElse-2));
            return true;
        }};
        statement[24] = new ArcNil(22){boolean action(){
            short labelElse = labels.pop();
            replaceAt(labelElse, (short)(baos.size()-labelElse-2));
            return true;
        }};
        statement[25] = new ArcGraph(statement, 22, 0){boolean action(){
            short labelIf = labels.pop();
            replaceAt(labelIf, (short)(baos.size()-labelIf-2));
            return true;
        }};
        statement[26] = new ArcToken(lexer.new Token(4), 22, 20){boolean action(){
            //generate String; how to put String?? -> statement 19 (next->26 (20 alternative))
            genCode("putStrg");
            writeStringToByteArray(t.str);
            return true;
        }};

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
            baos = new ByteArrayOutputStream();
            genCode("entryProc", 0, currentProc.procIndex, currentProc.varAdress);
            return true;
        }};
        block[18] = new ArcGraph(statement, 19, 0){boolean action(){
            genCode("retProc");
            replaceAt(1, (short)(baos.size()));
            writeCodeInFile();
            //currentProc.namelist.clear();
            currentProc = currentProc.parent;
            return true;
        }};
        block[19] = new ArcEnd();
        
        program[0] = new ArcGraph(block, 1, 0);
        program[1] = new ArcSymbol('.', 2, 0);
        program[2] = new ArcEnd();
        
        expression[0] = new ArcSymbol('-', 1, 2);
        expression[1] = new ArcGraph(term, 3, 0){boolean action(){
            genCode("vzMinus");
            return true;
        }};
        expression[2] = new ArcGraph(term, 3, 0);
        expression[3] = new ArcNil(4);
        expression[4] = new ArcSymbol('+', 6, 5);
        expression[5] = new ArcSymbol('-', 7, 8);
        expression[6] = new ArcGraph(term, 3, 0){boolean action(){
            genCode("OpAdd");
            return true;
        }};
        expression[7] = new ArcGraph(term, 3, 0){boolean action(){
            genCode("OpSub");
            return true;
        }};
        expression[8] = new ArcNil(9);
        expression[9] = new ArcEnd();
        
        term[0] = new ArcGraph(factor, 1, 0);
        term[1] = new ArcNil(2);
        term[2] = new ArcSymbol('*', 3, 4);
        term[3] = new ArcGraph(factor, 1, 0){boolean action(){
            genCode("OpMult");
            return true;
        }};
        term[4] = new ArcSymbol('/', 5, 6);
        term[5] = new ArcGraph(factor, 1, 0){boolean action(){
            genCode("OpDiv");
            return true;
        }};
        term[6] = new ArcNil(7);
        term[7] = new ArcEnd();
        
        factor[0] = new ArcToken(lexer.new Token(2), 5, 1){boolean action(){
            if(constBlock.indexOf(t.num)==-1)constBlock.add(t.num);
            genCode("puConst", (short)constBlock.indexOf(t.num));
            return true;
        }};
        factor[1] = new ArcSymbol('(', 2, 4);
        factor[2] = new ArcGraph(expression, 3, 0);
        factor[3] = new ArcSymbol(')', 5, 0);
        factor[4] = new ArcToken(lexer.new Token(3), 5, 0){boolean action(){
            Ident i = searchIdentGlobal(t.str);
            if(i==null || i instanceof Procedure){
                if(i==null)System.out.println("Variable "+t.str+" nicht gefunden!");
                else System.out.println("Fehler: "+t.str+" ist keine Prozedur!");
                return false;
            }
            if(i instanceof Constant)genCode("puConst", ((Constant)i).constIndex);
            else if(i.prozNum==0)genCode("puValVrMain", ((Variable)i).address);
            else if(i.prozNum==currentProc.procIndex)genCode("puValVrLocl", ((Variable)i).address);
            else genCode("puValVrGlob", ((Variable)i).address, i.prozNum);
            return true;
        }};
        factor[5] = new ArcEnd();
        
        condition[0] = new ArcSymbol(137, 1, 2);
        condition[1] = new ArcGraph(expression, 10, 0);
        condition[2] = new ArcGraph(expression, 3, 0);
        condition[3] = new ArcSymbol('=', 9, 4){boolean action(){
            comparisonOperator=t.sym;
            return true;
        }};
        condition[4] = new ArcSymbol('#', 9, 5){boolean action(){
            comparisonOperator=t.sym;
            return true;
        }};
        condition[5] = new ArcSymbol('<', 9, 6){boolean action(){
            comparisonOperator=t.sym;
            return true;
        }};
        condition[6] = new ArcSymbol('>', 9, 7){boolean action(){
            comparisonOperator=t.sym;
            return true;
        }};
        condition[7] = new ArcSymbol(129, 9, 8){boolean action(){
            comparisonOperator=t.sym;
            return true;
        }};
        condition[8] = new ArcSymbol(130, 9, 0){boolean action(){
            comparisonOperator=t.sym;
            return true;
        }};
        condition[9] = new ArcGraph(expression, 10, 0){boolean action(){
            switch (comparisonOperator) {
                case '=': genCode("cmpEQ"); break;
                case '#': genCode("cmpNE"); break;
                case '<': genCode("cmpLT"); break;
                case '>': genCode("cmpGT"); break;
                case 129: genCode("cmpLE"); break;
                case 130: genCode("cmpGE"); break;
                default:
                    break;
            }
            return true;
        }};
        condition[10] = new ArcEnd();   
    }

/************************************************************************************/
/*************************Funktionen des Parsers************************************/
/************************************************************************************/
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
    int einsatz=0;
    public void printNameList(LinkedList<Ident> list){
        for(Ident i:list){
            for(int k=0; k<einsatz; k++)System.out.print(" ");
            if(i instanceof Variable)System.out.println("Variable: "+i.name);
            else if(i instanceof Constant)System.out.println("Konstante: "+i.name);
            else if(i instanceof Procedure){
                System.out.println("Prozedur: "+i.name);
                einsatz++;
                printNameList(((Procedure)i).namelist);
                einsatz--;
            }
        }
    }
    public static void main(String args[]) throws IOException{
        Parser parser = new Parser(args[0]);

        if(parser.parse(parser.program))System.out.println("Parsen erfolgreich!");
        else {
            System.out.println("Parsen nicht erfolgreich! Fehler bei Zeile "+ parser.t.posCol+ ", Zeichen: "+ parser.t.posLine);
            parser.printNameList(mainProc.namelist);
            System.exit(-1);
        }
        parser.baos.reset();
        for(int i=0; i<parser.constBlock.size(); i++){
            parser.writeIntToByteArray(parser.constBlock.get(i).intValue());
        }
        parser.writeProcNum();
        parser.writeCodeInFile();
        parser.fos.close();
    }
}