import View.PrincipalFrame;
import Config.ConexaoMongo;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Configurar look and feel para uma interface mais moderna
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Não foi possível definir o tema do sistema: " + e.getMessage());
        }

        // Iniciar a interface em uma thread segura
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PrincipalFrame frame = new PrincipalFrame();
                frame.setVisible(true);
            }
        });

        // Adicionar shutdown hook para fechar conexão com MongoDB
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConexaoMongo.close();
        }));
    }
}