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
        Ano2 k = new Ano2(2){
            int func(){
                return s;
            }
        };
        System.out.println("Ergebnis: "+k.func());
        System.out.println("Klasse: "+k.getClass().getName());
    }
}