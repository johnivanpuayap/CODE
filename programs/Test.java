package programs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Test {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Interpreter <input_file>");
            System.exit(1);
        }

        String filePath = args[0];

        if (!filePath.endsWith(".code")) {
            System.err.println("Input file must be a .code file");
            System.exit(1);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            
            String input = sb.toString();

            String[] lines = input.split("\n");
            for (String l : lines) {
                System.out.println(l);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
    }
    
}
