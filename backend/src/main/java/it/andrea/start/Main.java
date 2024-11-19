package it.andrea.start;

import java.security.SecureRandom;

public class Main {
    public static void main(String[] args) {
     // Crea un array di byte di lunghezza 32
        byte[] key = new byte[32];
        
        // Genera byte casuali utilizzando SecureRandom
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);
        
        // Stampa la chiave generata in formato esadecimale
        System.out.println("Chiave generata (esadecimale):");
        for (byte b : key) {
            System.out.printf("%02x", b);
        }
        System.out.println();
    }
}