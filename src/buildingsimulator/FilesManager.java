package buildingsimulator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FilesManager {
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
