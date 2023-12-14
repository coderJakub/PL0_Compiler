import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        writeShortToByteArray(20, byteArrayOutputStream);
        System.out.println(byteArrayOutputStream.size());
        int positionToOverride = 1; // Hier ist die aktuelle Größe des Arrays

        short placeholderLength = 0;
        writeShortToByteArray(placeholderLength, byteArrayOutputStream);
        short actualLength = 0x1A; // Beispielwert für die tatsächliche Länge

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        for (byte b : byteArray) {
            System.out.print(b + " ");
        }
        System.out.println(byteArrayOutputStream.size());
        overwriteShortInArray(actualLength, positionToOverride, byteArrayOutputStream);

    }

    private static void writeToByteArray(String input, ByteArrayOutputStream byteArrayOutputStream) {
        try {
            byteArrayOutputStream.write(input.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeShortToByteArray(int value, ByteArrayOutputStream byteArrayOutputStream) {
        byteArrayOutputStream.write((value >> 8) & 0xFF);
        byteArrayOutputStream.write(value & 0xFF);
    }

    private static void overwriteShortInArray(short value, int position, ByteArrayOutputStream byteArrayOutputStream) {
        byte[] bytes = byteArrayOutputStream.toByteArray();
        bytes[position] = (byte) ((value >> 8) & 0xFF);
        bytes[position + 1] = (byte) (value & 0xFF);
        byteArrayOutputStream.reset();
        byteArrayOutputStream.write(bytes, 0, bytes.length);
    }
}