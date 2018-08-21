package application;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressIndicator;

import java.io.*;
import java.net.*;

public class MainController implements Initializable{

    @FXML
    private ProgressIndicator progress;
    
    private static final int BUFFER_SIZE = 4096;
    private Map<String, String> config;
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		config = Propriedades.valores();
		if (!config.containsKey("url_aplicacao")) {
			System.err.println("Parametro necessário não foram preenchidos - url_aplicacao");
			System.exit(0);
		}
	}
    
	// true para download concluido, false para download não realizado
    public boolean atualizarVersao() {
    	String nome_pasta_nova_versao = config.getOrDefault("versao_nova","versao_nova");
    	String nome_pasta_versao_antiga = config.getOrDefault("pasta_versoes_antigas","versoes_antigas");
    	String nome_padrao_arquivo = config.getOrDefault("nome_padrao_arquivo","situacao-de-viagem.jar");
    	
    	File pasta_principal = new File("./");
    	File pasta_nova_versao = new File(nome_pasta_nova_versao);
    	File pasta_versao_antiga = new File(nome_pasta_versao_antiga);
    	File arquivo_nova_versao = new File("");
    	
    	List<File> arquivos = Stream.of(pasta_principal.listFiles())
				.filter(arquivo -> arquivo.getName().equalsIgnoreCase(nome_padrao_arquivo))
					.collect(Collectors.toList());
				
		File arquivo_versao_atual = (arquivos != null && arquivos.size() == 1) ? arquivos.get(0) : null;
    	
		if(arquivo_versao_atual == null) {
			System.out.println("Não foi possivel carregar o arquivo atual");
			System.exit(0);
		}
			
		
		if (!pasta_nova_versao.exists())
    		pasta_nova_versao.mkdir();
    	if (!pasta_versao_antiga.exists()) 
    		pasta_versao_antiga.mkdir();
    	
		try {
			downloadFile(config.get("url_aplicacao"), pasta_nova_versao.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
    	arquivo_nova_versao = pasta_nova_versao.listFiles()[0];
		
    	if (arquivo_nova_versao != null && arquivo_nova_versao.exists()) {
    		int codigo_versao_atual = Integer.valueOf(pegaVersao(arquivo_versao_atual));
        	int codigo_versao_nova = Integer.valueOf(pegaVersao(arquivo_nova_versao));
        	
        	System.out.println("versao atual : "+codigo_versao_atual);
    		System.out.println("versao nova : "+codigo_versao_nova);
    		
    		if (codigo_versao_nova == codigo_versao_atual) {
				arquivo_nova_versao.delete();
				Alert alerta = new Alert(AlertType.ERROR);
				alerta.setContentText("Está versão está atualizada \n versão atual "+
				codigo_versao_atual+" versao baixada "+ codigo_versao_nova);
				alerta.setTitle("Atualização não é necessária");
				alerta.setOnCloseRequest(event -> {
					try {
						new ProcessBuilder("java","-jar",nome_padrao_arquivo).start();
					} catch (IOException e) { e.printStackTrace(); }
					System.exit(0);
				});
				alerta.showAndWait();
			}
    		
    		if (arquivo_nova_versao.length() != 0) {
    			Alert alerta = new Alert(AlertType.ERROR);
				try {
					Socket socket = new Socket("localhost", 5858);
					socket.getOutputStream().write("".getBytes());
					
					alerta.setContentText("Feche a Aplicação antes de realizar a atualização");
					alerta.setTitle("Erro ao atualizar, tente novamente");
					alerta.showAndWait();
					socket.close();
					System.exit(0);
				} catch (IOException e) {
					System.out.println("A aplicação está fechada");
				} finally {
					alerta.hide();
				}
				
				progress.setProgress(-10);
				
				if (arquivo_versao_atual != null && arquivo_versao_atual.exists()) {
					
					arquivo_versao_atual.renameTo(new File("./"+nome_pasta_versao_antiga+"/"+codigo_versao_atual+"_"+nome_padrao_arquivo));					
					arquivo_nova_versao.renameTo(new File("./"+nome_padrao_arquivo));
					
					pasta_nova_versao.delete();
					
					Alert alertasucesso = new Alert(AlertType.CONFIRMATION);
					alertasucesso.setContentText("atualização concluida!");
					alertasucesso.setTitle("Aplicativo atualizado");
					alertasucesso.setOnCloseRequest(event -> {
						try {
							new ProcessBuilder("java","-jar",nome_padrao_arquivo).start();
						} catch (IOException e) { e.printStackTrace(); }
						System.exit(0);
					});
					alertasucesso.show();
				} else {
					System.err.println("problema com o arquivo principal");
				}
				
			} else {
				System.out.println("falha no novo arquivo");
				System.exit(0);
			}
    	}
        
    	return false;
    }
    
    public static void downloadFile(String fileURL, String saveDir)
            throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
            System.out.println(httpConn.getLastModified());
 
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }
 
            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);
 
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
 
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
    
    public String pegaVersao(File arquivo) {
    	try {
			JarFile jar = new JarFile(arquivo);
			String versao = jar.getManifest().getMainAttributes().getValue("Implementation-Version").replace(".", "");
			jar.close();
			return versao;
		} catch (IOException e) {
			e.printStackTrace();
			return "-1";
		}
    }
}
