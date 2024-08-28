import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DestinationHashGenerator {

    public static void main(String[] args) {
        // Check if the correct number of arguments is passed
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN> <jsonFilePath>");
            return;
        }

        // Parse and clean the PRN number
        String prn = args[0].toLowerCase().replaceAll("\\s+", "");

        // Get the file path
        String jsonFilePath = args[1];

        try {
            // Find the destination value in the JSON file
            String destinationValue = findDestinationValue(jsonFilePath);
            if (destinationValue == null) {
                System.out.println("No destination key found in the JSON file.");
                return;
            }

            // Generate a random string
            String randomString = generateRandomString(8);

            // Concatenate PRN, destination value, and random string
            String concatenated = prn + destinationValue + randomString;

            // Generate the MD5 hash
            String md5Hash = generateMD5Hash(concatenated);

            // Print the result in the format <hash>;<random string>
            System.out.println(md5Hash + ";" + randomString);

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // Method to find the first occurrence of "destination" in the JSON
    private static String findDestinationValue(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
        return traverseJson(rootNode);
    }

    // Recursive method to traverse the JSON tree
    private static String traverseJson(JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().equals("destination")) {
                    return field.getValue().asText();
                }
                String result = traverseJson(field.getValue());
                if (result != null) return result;
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                String result = traverseJson(arrayElement);
                if (result != null) return result;
            }
        }
        return null;
    }

    // Method to generate a random alphanumeric string
    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    // Method to generate an MD5 hash from a string
    private static String generateMD5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
