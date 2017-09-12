package buildingsimulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasa <code>FilesManager</code> reprezentuje zarządcę plików. Posiada kilka 
 * przydatnych, statycznych metod. 
 * @author AleksanderSklorz
 */
public class FilesManager {
    /**
     * Zwraca z pliku wartość dla podanego klucza. 
     * @param key klucz dla którego szuka się wartości 
     * @param path ścieżka do pliku 
     * @return wczytana wartość dla podanego klucza 
     */
    public static String getValue(String key, String path){
        try(BufferedReader input = new BufferedReader(new FileReader(path))){
            String line; 
            while((line = input.readLine()) != null){
                String[] parts = line.split("=");
                if(parts[0].equals(key)) return parts[1];
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null; 
    }
    
    /**
     * Wczytuje wszystkie właściwości z pliku. Właściwości mają postać klucz=wartosc.
     * @param path ścieżka do pliku
     * @return mapa z parami klucz-wartość
     */
    public static Map<String, String> loadAllProperties(String path){
        Map<String, String> settings = new HashMap(); 
        try(BufferedReader input = new BufferedReader(new FileReader(path))){
            String line; 
            while((line = input.readLine()) != null){
                String[] parts = line.split("=");
                settings.put(parts[0], parts[1]);
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return settings; 
    }
}
