package dsd.dsdvansales.global;

import java.util.HashMap;
import java.util.Map;

public class EncodingConverter {

    private static final Map<Character, Byte> unicodeToCp864Map = new HashMap<>();

    static {
        // Add mappings for Arabic characters from Unicode to CP864
        unicodeToCp864Map.put('ا', (byte) 0x80);
        unicodeToCp864Map.put('ب', (byte) 0x81);
        unicodeToCp864Map.put('ت', (byte) 0x82);
        unicodeToCp864Map.put('ث', (byte) 0x83);
        unicodeToCp864Map.put('ج', (byte) 0x84);
        unicodeToCp864Map.put('ح', (byte) 0x85);
        unicodeToCp864Map.put('خ', (byte) 0x86);
        unicodeToCp864Map.put('د', (byte) 0x87);
        unicodeToCp864Map.put('ذ', (byte) 0x88);
        unicodeToCp864Map.put('ر', (byte) 0x89);
        unicodeToCp864Map.put('ز', (byte) 0x8A);
        unicodeToCp864Map.put('س', (byte) 0x8B);
        unicodeToCp864Map.put('ش', (byte) 0x8C);
        unicodeToCp864Map.put('ص', (byte) 0x8D);
        unicodeToCp864Map.put('ض', (byte) 0x8E);
        unicodeToCp864Map.put('ط', (byte) 0x8F);
        unicodeToCp864Map.put('ظ', (byte) 0x90);
        unicodeToCp864Map.put('ع', (byte) 0x91);
        unicodeToCp864Map.put('غ', (byte) 0x92);
        unicodeToCp864Map.put('ف', (byte) 0x93);
        unicodeToCp864Map.put('ق', (byte) 0x94);
        unicodeToCp864Map.put('ك', (byte) 0x95);
        unicodeToCp864Map.put('ل', (byte) 0x96);
        unicodeToCp864Map.put('م', (byte) 0x97);
        unicodeToCp864Map.put('ن', (byte) 0x98);
        unicodeToCp864Map.put('ه', (byte) 0x99);
        unicodeToCp864Map.put('و', (byte) 0x9A);
        unicodeToCp864Map.put('ي', (byte) 0x9B);

        // Numerals, space, and other common characters
        unicodeToCp864Map.put(' ', (byte) 0x20); // Space
        unicodeToCp864Map.put('0', (byte) 0x30); // '0'
        unicodeToCp864Map.put('1', (byte) 0x31); // '1'
        unicodeToCp864Map.put('2', (byte) 0x32); // '2'
        unicodeToCp864Map.put('3', (byte) 0x33); // '3'
        unicodeToCp864Map.put('4', (byte) 0x34); // '4'
        unicodeToCp864Map.put('5', (byte) 0x35); // '5'
        unicodeToCp864Map.put('6', (byte) 0x36); // '6'
        unicodeToCp864Map.put('7', (byte) 0x37); // '7'
        unicodeToCp864Map.put('8', (byte) 0x38); // '8'
        unicodeToCp864Map.put('9', (byte) 0x39); // '9'
        unicodeToCp864Map.put('.', (byte) 0x2E); // '.'
        unicodeToCp864Map.put(',', (byte) 0x2C); // ','
        unicodeToCp864Map.put(';', (byte) 0x3B); // ';'
        unicodeToCp864Map.put(':', (byte) 0x3A); // ':'
        unicodeToCp864Map.put('?', (byte) 0x3F); // '?'
    }

    public static byte[] convertUtf8ToCp864(String utf8String) {
        byte[] cp864Bytes = new byte[utf8String.length()];
        int i = 0;

        for (char ch : utf8String.toCharArray()) {
            Byte cp864Byte = unicodeToCp864Map.get(ch);
            if (cp864Byte != null) {
                cp864Bytes[i++] = cp864Byte;
            } else {
                // Replace unmapped characters with a space or other placeholder
                cp864Bytes[i++] = (byte) ch; // using space as placeholder
            }
        }

        return cp864Bytes;
    }
}
