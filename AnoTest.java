import java.io.*;

public class AnoTest{
    public static class Ano{
        int func(){
            return -1; //->function doesnt exsist
        }
    }
    public static class Ano2 extends Ano{
        int s;
        public Ano2(int k){
            s=k;
        }
    }

    public static void main(String[] args){
        AnoTest t =new AnoTest();
        Ano2 k = new Ano2(2);
        System.out.println("Ergebnis: "+k.func());
    }
}