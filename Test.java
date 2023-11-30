
public class Test {
    static String code= "hallo";

    public static String replaceAt(String s, int pos, String c) {
        return s.substring(0, pos) + c + s.substring(pos + c.length());
    }
    static void func(int pos, String c){
        code = replaceAt(code, pos, c);
    }
    public static void main(String args[]){
        String a = "as";
        switch(a){
            case "as": System.out.println("lol"); break;
            default: System.out.println("s");break;
        }
        func(2, "as");
        System.out.println(code);
    }
}