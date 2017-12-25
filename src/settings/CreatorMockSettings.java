package settings;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa <code>CreatorMockSettings</code> umożliwia stworzenie domyślnych danych, 
 * które można wykorzystać w przypadku, gdy nie posiada się dostępu do zapisanych 
 * danych np. brak pliku. 
 * @author AleksanderSklorz
 */
public class CreatorMockSettings {
    /**
     * Tworzy mapę z domyślnymi ustawieniami oraz zapisuje ją do pliku. 
     * @param keys tablica kluczy (nazw właściwości) 
     * @param values tablica wartości dla danych właściwości 
     * @param path ścieżka dla pliku 
     * @return mapa właściwości 
     */
    public static Properties createDefaultProperties(String[] keys, String[] values,
            String path) {
        Properties defaultProperties = new Properties(); 
        for(int i = 0; i < keys.length; i++)
            defaultProperties.setProperty(keys[i], values[i]);
        try(PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            defaultProperties.store(writer, null);
        } catch (IOException ex) {
            Logger.getLogger(CreatorMockSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
        return defaultProperties;
    }
}
