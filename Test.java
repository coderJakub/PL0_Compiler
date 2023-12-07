import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Schreibe den Befehl "entry Proc" in das ByteArrayOutputStream
        byteArrayOutputStream.write(0x1A);

        // Speichere die Position, an der die Länge des Parameters geschrieben werden soll
        int positionToOverride = 1; // Hier ist die aktuelle Größe des Arrays

        // Platzhalter für die Länge (4 Bytes für ein Integer)
        short placeholderLength = 0;

        // Schreibe den Platzhalter für die Länge
        writeShortToByteArray(placeholderLength, byteArrayOutputStream);

        // Jetzt haben wir den Befehl "entry Proc" und einen Platzhalter für die Länge im ByteArrayOutputStream

        // Annahme: Du hast die tatsächliche Länge später berechnet
        short actualLength = 0x1A; // Beispielwert für die tatsächliche Länge

        // Überschreibe den Platzhalter für die Länge an der vorherigen Position
        overwriteShortInArray(actualLength, positionToOverride, byteArrayOutputStream);

        // Beispiel, um den Inhalt des ByteArrayOutputStream auszugeben
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        for (byte b : byteArray) {
            System.out.print(b + " ");
        }
    }

    // Methode zum Schreiben von Strings in das ByteArrayOutputStream
    private static void writeToByteArray(String input, ByteArrayOutputStream byteArrayOutputStream) {
        try {
            byteArrayOutputStream.write(input.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methode zum Schreiben eines Integer-Werts in das ByteArrayOutputStream
    private static void writeShortToByteArray(int value, ByteArrayOutputStream byteArrayOutputStream) {
        byteArrayOutputStream.write((value >> 8) & 0xFF);
        byteArrayOutputStream.write(value & 0xFF);
    }

    // Methode zum Überschreiben eines Integer-Werts an einer bestimmten Position im ByteArrayOutputStream
    private static void overwriteShortInArray(short value, int position, ByteArrayOutputStream byteArrayOutputStream) {
        byte[] bytes = byteArrayOutputStream.toByteArray();
        bytes[position] = (byte) ((value >> 8) & 0xFF);
        bytes[position + 1] = (byte) (value & 0xFF);
        byteArrayOutputStream.reset();
        byteArrayOutputStream.write(bytes, 0, bytes.length);
    }
}