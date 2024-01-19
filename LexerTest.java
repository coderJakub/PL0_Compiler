
public class LexerTest extends Lexer{
    
    public static void main(String args[]){
        if(args.length!=1)return;
        Lexer lexer = new Lexer(args[0]);
        Lexer.Token t = lexer.new Token(); //sodass auf eine none static inner class zugegriffen werden kann
        
        String[] sym= {":=", "<=", ">=", "BEGIN", "CALL", "CONST", "DO", "END", "IF", "ODD", "PROCEDURE", "THEN", "VAR", "WHILE"};
        
        while(t.type!=1 || t.sym!='.'){
            t=lexer.Lex();
            switch(t.type){
                case 1:
                    if(t.sym<128)System.out.println("Symbol : "+(char)t.sym);
                    else 
                        System.out.println("Symbol : "+sym[t.sym-128]);
                    break;
                case 2:
                    System.out.println("Number : "+t.num);
                    break;
                case 3:
                    System.out.println("Keyword: "+t.str);
                    break;
                case 4:
                    System.out.println("String : "+t.str);
                    break;
                default:
                    System.out.println("Unknown Token: "+t.type);
                    break;
            }
        }
    }











    // public static void main(String args[]) throws Exception{
    //     File f=new File(args[0]);
    //     if (!f.exists()|| !f.canRead())
    //     {
    //         System.out.println("Can't read "+f);
    //         return;
    //     }
    //     FileInputStream fis=new FileInputStream(f);
    //     for(int i=0; i<100; i++){
    //         int r = fis.read();
    //         char c= (char)r;
    //         if(c=='\n'){
    //             System.out.println("Ich habe das Ende einer Zeile gefunden!");
    //             return;
    //         }
    //         System.out.println(c);
    //         fis.close();
    //     }
    // }
}
