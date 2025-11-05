package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PrincipalFrame extends JFrame {
    public PrincipalFrame() {
        setTitle("Sistema Da Roça - Menu Principal");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Definir layout para garantir renderização correta
        setLayout(new BorderLayout());

        // Adicionar painel de boas-vindas para verificar renderização
        JPanel panelBoasVindas = new JPanel(new BorderLayout());
        panelBoasVindas.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel lblTitulo = new JLabel("Sistema Da Roça", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(0x2E7D32));
        panelBoasVindas.add(lblTitulo, BorderLayout.NORTH);

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Sistema de Gerenciamento de Produtos e Categorias", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 16));
        panelBoasVindas.add(lblSubtitulo, BorderLayout.CENTER);

        // Mensagem de boas-vindas
        JLabel lblMensagem = new JLabel("Selecione uma opção no menu acima para começar", SwingConstants.CENTER);
        lblMensagem.setFont(new Font("Arial", Font.ITALIC, 14));
        panelBoasVindas.add(lblMensagem, BorderLayout.SOUTH);

        add(panelBoasVindas, BorderLayout.CENTER);

        // Adicionar imagem (opcional, caso tenha uma imagem de fundo)
        // JLabel background = new JLabel(new ImageIcon("caminho/para/imagem.jpg"));
        // add(background, BorderLayout.CENTER);

        // Configurar menu
        JMenuBar menuBar = new JMenuBar();

        // Menu Manutenção
        JMenu menuManutencao = new JMenu("Manutenção");
        menuManutencao.setFont(new Font("Arial", Font.BOLD, 14));

        JMenuItem itemCategorias = new JMenuItem("Categorias");
        itemCategorias.setFont(new Font("Arial", Font.PLAIN, 12));
        itemCategorias.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaCategorias().setVisible(true);
            }
        });

        JMenuItem itemProdutos = new JMenuItem("Produtos");
        itemProdutos.setFont(new Font("Arial", Font.PLAIN, 12));
        itemProdutos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TelaProdutos().setVisible(true);
            }
        });

        menuManutencao.add(itemCategorias);
        menuManutencao.add(itemProdutos);
        menuBar.add(menuManutencao);

        // Menu Sair
        JMenu menuSair = new JMenu("Sistema");
        menuSair.setFont(new Font("Arial", Font.BOLD, 14));

        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.setFont(new Font("Arial", Font.PLAIN, 12));
        itemSair.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(PrincipalFrame.this,
                        "Tem certeza que deseja sair do sistema?", "Confirmação de Saída",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        menuSair.add(itemSair);
        menuBar.add(menuSair);

        setJMenuBar(menuBar);

        // Adicionar rodapé
        JPanel rodape = new JPanel();
        rodape.setBorder(BorderFactory.createEtchedBorder());
        JLabel lblRodape = new JLabel("Sistema Da Roça v1.0 - © 2025 | Colégio Técnico de Campinas", SwingConstants.CENTER);
        rodape.add(lblRodape);
        add(rodape, BorderLayout.SOUTH);
    }
}
