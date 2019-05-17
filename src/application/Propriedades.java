package application;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class Propriedades {
	
	private static String arquivoConfiguracao = "./atualizador.properties";
	
	public static Map<String, String> valores(){
		Map<String, String> map = new Hashtable<String, String>();
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(arquivoConfiguracao));
			
			properties.forEach((key,value) -> map.put((String)key, (String)value));
		} catch (IOException e) { e.printStackTrace(); }
		
		return map;
	}
	
}
