import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Input {
    static String input = new String();
    static String output = new String();
    static String rotor1 = new String();
    static String rotor2 = new String();
    static String rotor3 = new String();
    static String[] rotorSettings = new String[3];
    static int[] offset;
    static String reflector = new String();
    static char[] messageChar;
    static char[] rotor1_ch;
    static char[] rotor2_ch;
    static char[] rotor3_ch;

    Input() {
        offset = new int[3];
        // Modularity not implemented in this version to the file input version v0.1
        File input_message = new File("message/Input");
        File input_rotor1 = new File("Rotors/Rotor1");
        File input_rotor2 = new File("Rotors/Rotor2");
        File input_rotor3 = new File("Rotors/Rotor3");
        File input_reflector = new File("Settings/Reflector");
        File input_plug = new File("Settings/Plug");
        File input_rotorSettings = new File("Settings/RotorSettings");
        try {
            Scanner sc1 = new Scanner(input_message);
            Scanner sc2 = new Scanner(input_rotor1);
            Scanner sc3 = new Scanner(input_rotor2);
            Scanner sc4 = new Scanner(input_rotor3);
            Scanner sc5 = new Scanner(input_reflector);
            Scanner sc6 = new Scanner(input_plug);
            Scanner sc7 = new Scanner(input_rotorSettings);

            while (sc1.hasNextLine()) {
                String line = sc1.nextLine();
                input = String.valueOf(line.toCharArray(), 0, line.length());
            }
            sc1.close();

            while (sc2.hasNextLine()) {
                String line = sc2.nextLine();
                rotor1 = String.valueOf(line.toCharArray(), 0, line.length());
            }
            sc2.close();

            while (sc3.hasNextLine()) {
                String line = sc3.nextLine();
                rotor2 = String.valueOf(line.toCharArray(), 0, line.length());
            }
            sc3.close();

            while (sc4.hasNextLine()) {
                String line = sc4.nextLine();
                rotor3 = String.valueOf(line.toCharArray(), 0, line.length());
            }
            sc4.close();

            while (sc5.hasNextLine()) {
                String line = sc5.nextLine();
                reflector = String.valueOf(line.toCharArray(), 0, line.length());
            }
            sc5.close();

            int ix = 0;
            while (sc7.hasNextLine()) {
                String line = sc7.nextLine();
                // System.out.println(line.length());
                offset[ix++] = Integer.parseInt(line);
            }
            sc7.close();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        messageChar = new char[input.length()];
        rotor1_ch = new char[rotor1.length()];
        rotor2_ch = new char[rotor2.length()];
        rotor3_ch = new char[rotor3.length()];
        input.getChars(0, input.length(), messageChar, 0);

    }

    public void Encrypt() {

        ENCODE_MESSAGE();
        DECODE_MESSAGE();

        System.out.println();
        System.out.println(messageChar);

        try {
            FileWriter writeToOutput = new FileWriter("message/Output");
            writeToOutput.write(String.valueOf(messageChar, 0, messageChar.length));
            writeToOutput.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void ENCODE_MESSAGE() {
        rotor1.getChars(0, rotor1.length(), rotor1_ch, 0); // resets the rotor settings
        rotor1.getChars(0, rotor2.length(), rotor2_ch, 0);
        rotor1.getChars(0, rotor3.length(), rotor3_ch, 0);

        // Rotors are set to their notch positions as mentioned in the RotorSettings
        // file
        RotateRight(rotor1_ch, offset[2]);
        RotateRight(rotor2_ch, offset[1]);
        RotateRight(rotor3_ch, offset[0]);

        // encoding takes place step by step over the rotors
        RotorCircMapEncode(rotor1_ch, 2);
        RotorCircMapEncode(rotor2_ch, 1);
        RotorCircMapEncode(rotor3_ch, 0);
    }

    public static void DECODE_MESSAGE() {
        /*
         * This method, decodes from bottom up rotors instead of the same as the
         * ENCODER_MESSAGE() method This flaw can be rectified by implementing the
         * REFLECTOR() method
         */
        rotor1.getChars(0, rotor1.length(), rotor1_ch, 0); // resets the rotor settings
        rotor1.getChars(0, rotor2.length(), rotor2_ch, 0);
        rotor1.getChars(0, rotor3.length(), rotor3_ch, 0);

        RotateRight(rotor1_ch, offset[2]);
        RotateRight(rotor2_ch, offset[1]);
        RotateRight(rotor3_ch, offset[0]);

        // reflector here
        RotorCircMapDecode(rotor3_ch, 0);
        RotorCircMapDecode(rotor2_ch, 1);
        RotorCircMapDecode(rotor1_ch, 2);
    }

    public static void RotorCircMapDecode(char[] rotor, int count) { // this works only for decoding
        int index = -1;
        for (int i = 0; i < messageChar.length; i++) {
            if ((int) messageChar[i] != 32) {
                for (int j = 0; j < rotor.length; j++) {
                    if (messageChar[i] == rotor[j]) {
                        index = j;
                        break;
                    }
                }
                messageChar[i] = (char) (65 + index);
                RotateRight(rotor);
            }
        }
    }

    public static void RotorCircMapEncode(char[] rotor, int count) {
        /*
         * Maps the input letter to the rotor settings as per the notch
         * 
         *                      ****ALGORITHM****
         * 
         * The encryption map to be considered for each rotor is stored at a char array
         * of size 26 corresponding to the letters of English alphabet
         * 
         * The string of input text is considered by every index and the value of the
         * char(0 for 'A' and 25 for 'Z') is mapped to the matching index of the rotor
         * char array with the index corresponding to the value of the char from the
         * text stream
         * 
         * With each input text string char being mapped to the respective char from the
         * rotor, the rotor notch is shifted one position right thus avoiding repetition
         * 
         */
        for (int i = 0; i < messageChar.length; i++) {
            if ((int) messageChar[i] != 32) {
                int sum = (int) rotor[(messageChar[i] - 65)];
                messageChar[i] = (char) sum;
                RotateRight(rotor, 1);
            }

        }
    }

    public static void RotorCircMapReflector(String rotor, int count) { // under construction
        for (int i = 0; i < messageChar.length; i++) {
            if ((int) messageChar[i] != 32) {
                int sum = (int) rotor.charAt(messageChar[i] - 65);
                messageChar[i] = (char) sum;
            }
        }
    }

    public static void RotateRight(char arr[], int n) { // shifts the notch by n places
        for (int j = 0; j < n; j++) {
            int i;
            char last = arr[arr.length - 1];

            for (i = arr.length - 1; i > 0; i--) {
                arr[i] = arr[i - 1];
            }
            arr[0] = last;
        }
    }

    public static void RotateRight(char arr[]) { // shifts the notch by 1 place

        int i;
        char last = arr[arr.length - 1];

        for (i = arr.length - 1; i > 0; i--) {
            arr[i] = arr[i - 1];
        }
        arr[0] = last;
    }
}
