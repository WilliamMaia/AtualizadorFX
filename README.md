# AtualizadorFX
Micro aplicação para executar atualizações de executavéis javaFX

## função para pegar versão da aplicação javaFX

    public static String pegaVersao(File arquivo) {
      try {
        JarFile jar = new JarFile(arquivo);
        String versao = jar.getManifest()
          .getMainAttributes()
            .getValue("Implementation-Version").replace(".", "");
        jar.close();
        return versao;
      } catch (IOException e) {
        return "-1";
      }
    }

## Exemplo de código a ser chamado dentro de uma aplicação para executar o atualizador
    File f = new File("./NOME-DO-ARQUIVO-EXECUTAVEL.jar");
    // 'versao' é um componente qualquer, pode ser um button, label, por exemplo, que receba uma função de clique
    versao.setText(pegaVersao(f));
    versao.setOnMouseClicked(event -> {

      // alerta de confirmação para executar a atualização
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setContentText("Para atualizar é necessário fechar esta tela");
      alert.initModality(Modality.WINDOW_MODAL);
      // para que o alerta seja chamado sobre a janela que chamou este alerta,
      // é necessário setar o owner do alerta, exemplo a baixo
      //alert.initOwner(root.getScene().getWindow());
      // exibe alerta, e aguarda resposta
      Optional<ButtonType> response = alert.showAndWait();

      if (response.get().getButtonData().isDefaultButton()) {
        try {
          // executa o atualizador
          Process process = new ProcessBuilder("java","-jar","Atualizador.jar").start();
        }
        catch (IOException e) { e.printStackTrace(); }
        finally { 
          // finaliza a aplicação onde o atualizador é chamada, para evitar conflitos de atualização
          System.exit(0); 
        }
      }
    });
