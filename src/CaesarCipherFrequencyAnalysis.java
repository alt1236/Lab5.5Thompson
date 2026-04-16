import java.util.*;

import java.util.*;
import java.io.*;
import java.net.*;

public class CaesarCipherFrequencyAnalysis {

    // Arabic letter frequencies
    private static final double[] ARABIC_FREQUENCIES = {
            11.6,4.8,3.7,1.1,2.8,2.6,1.1,3.5,1.0,4.7,
            0.9,6.5,3.0,2.9,1.5,1.7,0.7,3.9,1.0,3.0,
            2.7,3.6,5.3,3.1,7.2,2.5,6.0,6.7
    };

    // Arabic alphabet
    private static final char[] ARABIC_LETTERS = {
            'ا','ب','ت','ث','ج','ح','خ','د','ذ','ر','ز','س','ش','ص',
            'ض','ط','ظ','ع','غ','ف','ق','ك','ل','م','ن','ه','و','ي'
    };

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        // Step 1: User input
        System.out.print("Enter plaintext message: ");
        String text = scanner.nextLine();

        System.out.print("Enter Google API Key: ");
        String apiKey = scanner.nextLine();

        // Step 2: Translate to Arabic
        String translated = translateText(text, apiKey, "ar");
        System.out.println("\nTranslated (Arabic): " + translated);

        // Step 3: Caesar Cipher encryption
        int shift = 3;
        String encrypted = encryptArabic(translated, shift);
        System.out.println("Encrypted (Caesar Cipher): " + encrypted);

        // Step 4: Frequency Analysis Decryption
        String decrypted = decryptUsingFrequencyAnalysis(encrypted);
        System.out.println("Decrypted (Best Guess): " + decrypted);

        scanner.close();
    }

    // ================= GOOGLE TRANSLATE API =================
    public static String translateText(String text, String apiKey, String targetLang) throws Exception {
        String urlStr = "https://translation.googleapis.com/language/translate/v2?key=" + apiKey;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String params = "q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + targetLang;

        OutputStream os = conn.getOutputStream();
        os.write(params.getBytes());
        os.flush();

        InputStream is;

        // ✅ HANDLE ERRORS PROPERLY
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;
        StringBuilder response = new StringBuilder();

        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();

        String result = response.toString();

        // 🔍 DEBUG: print full response
        System.out.println("\nAPI RESPONSE:\n" + result);

        // Try extracting translation
        if (result.contains("translatedText")) {
            int start = result.indexOf("\"translatedText\": \"") + 19;
            int end = result.indexOf("\"", start);

            String translated = result.substring(start, end);
            return translated;
        } else {
            return "ERROR: No translation found";
        }
    }

    // ================= CAESAR CIPHER =================
    public static String encryptArabic(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            int index = getArabicIndex(c);

            if (index != -1) {
                int newIndex = (index + shift) % ARABIC_LETTERS.length;
                result.append(ARABIC_LETTERS[newIndex]);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    // ================= FREQUENCY ANALYSIS =================
    public static String decryptUsingFrequencyAnalysis(String ciphertext) {
        String bestDecryption = "";
        double lowestChiSquare = Double.MAX_VALUE;

        for (int shift = 0; shift < ARABIC_LETTERS.length; shift++) {
            String attempt = decryptArabic(ciphertext, shift);
            double chi = calculateChiSquare(attempt);

            if (chi < lowestChiSquare) {
                lowestChiSquare = chi;
                bestDecryption = attempt;
            }
        }

        return bestDecryption;
    }

    public static String decryptArabic(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            int index = getArabicIndex(c);

            if (index != -1) {
                int newIndex = (index - shift + ARABIC_LETTERS.length) % ARABIC_LETTERS.length;
                result.append(ARABIC_LETTERS[newIndex]);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    public static double calculateChiSquare(String text) {
        int[] counts = new int[ARABIC_LETTERS.length];
        int total = 0;

        for (char c : text.toCharArray()) {
            int index = getArabicIndex(c);
            if (index != -1) {
                counts[index]++;
                total++;
            }
        }

        double chiSquare = 0.0;

        for (int i = 0; i < counts.length; i++) {
            double observed = counts[i];
            double expected = total * ARABIC_FREQUENCIES[i] / 100;

            if (expected > 0) {
                chiSquare += Math.pow(observed - expected, 2) / expected;
            }
        }

        return chiSquare;
    }

    // ================= HELPER =================
    public static int getArabicIndex(char c) {
        for (int i = 0; i < ARABIC_LETTERS.length; i++) {
            if (ARABIC_LETTERS[i] == c) {
                return i;
            }
        }
        return -1;
    }
}
