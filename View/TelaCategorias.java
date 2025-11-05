package View;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import Config.ConexaoMongo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TelaCategorias extends JFrame {
    private MongoCollection<Document> colecaoCategorias;
    private DefaultTableModel modeloTabela;
    private JTable tabela;
    private JTextField txtNome;
    private JButton btnNovo, btnSalvar, btnExcluir, btnPesquisar;
    private List<Document> listaCategorias = new ArrayList<>();
    private int indiceSelecionado = -1;

    public TelaCategorias() {
        setTitle("Manutenção de Categorias");
        setSize(600, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Obter coleção de categorias
        colecaoCategorias = ConexaoMongo.getDatabase().getCollection("Categorias");

        initComponents();
        carregarTabela();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel de formulário
        JPanel painelForm = new JPanel(new GridLayout(2, 2, 5, 5));
        painelForm.setBorder(BorderFactory.createTitledBorder("Dados da Categoria"));

        painelForm.add(new JLabel("Nome:"));
        txtNome = new JTextField();
        painelForm.add(txtNome);

        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));

        btnNovo = new JButton("Novo");
        btnNovo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limparCampos();
            }
        });

        btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                salvarCategoria();
            }
        });

        btnExcluir = new JButton("Excluir");
        btnExcluir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirCategoria();
            }
        });

        btnPesquisar = new JButton("Pesquisar");
        btnPesquisar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pesquisarCategoria();
            }
        });

        painelBotoes.add(btnNovo);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnPesquisar);

        // Configurar tabela
        String[] colunas = {"ID", "Nome"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tabela.getSelectedRow();
                if (row >= 0) {
                    exibirCategoria(row);
                }
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabela);

        painelPrincipal.add(painelForm, BorderLayout.NORTH);
        painelPrincipal.add(scrollTabela, BorderLayout.CENTER);
        painelPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        add(painelPrincipal, BorderLayout.CENTER);
    }

    private void limparCampos() {
        txtNome.setText("");
        indiceSelecionado = -1;
        tabela.clearSelection();
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        listaCategorias.clear();

        try {
            for (Document doc : colecaoCategorias.find()) {
                listaCategorias.add(doc);
                modeloTabela.addRow(new Object[]{
                        doc.getObjectId("_id").toHexString(),
                        doc.getString("nome")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar categorias: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void exibirCategoria(int row) {
        if (row >= 0 && row < listaCategorias.size()) {
            Document categoria = listaCategorias.get(row);
            txtNome.setText(categoria.getString("nome"));
            indiceSelecionado = row;
        }
    }

    private void salvarCategoria() {
        String nome = txtNome.getText().trim();

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome da categoria é obrigatório!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Document categoria;
            if (indiceSelecionado >= 0 && indiceSelecionado < listaCategorias.size()) {
                // Editar categoria existente
                categoria = listaCategorias.get(indiceSelecionado);
                colecaoCategorias.updateOne(
                        Filters.eq("_id", categoria.getObjectId("_id")),
                        new Document("$set", new Document("nome", nome))
                );
                JOptionPane.showMessageDialog(this, "Categoria atualizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Nova categoria
                categoria = new Document("nome", nome);
                colecaoCategorias.insertOne(categoria);
                JOptionPane.showMessageDialog(this, "Categoria cadastrada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            carregarTabela();
            limparCampos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar categoria: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Método completo de exclusão conforme solicitado
    private void excluirCategoria() {
        if (indiceSelecionado < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para excluir!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir esta categoria?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION);

        if (resposta == JOptionPane.YES_OPTION) {
            try {
                Document categoria = listaCategorias.get(indiceSelecionado);
                ObjectId id = categoria.getObjectId("_id");

                // Verificar se existem produtos vinculados a esta categoria
                MongoCollection<Document> colecaoProdutos = ConexaoMongo.getDatabase().getCollection("Produtos");
                long count = colecaoProdutos.countDocuments(Filters.eq("idCategoria", id));

                if (count > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Não é possível excluir esta categoria pois existem " + count +
                                    " produto(s) vinculado(s) a ela.", "Atenção", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Excluir a categoria
                colecaoCategorias.deleteOne(Filters.eq("_id", id));
                JOptionPane.showMessageDialog(this, "Categoria excluída com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                carregarTabela();
                limparCampos();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir categoria: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void pesquisarCategoria() {
        String termo = JOptionPane.showInputDialog(this, "Digite o nome da categoria para pesquisar:");
        if (termo != null && !termo.trim().isEmpty()) {
            try {
                FindIterable<Document> resultado = colecaoCategorias.find(
                        Filters.regex("nome", termo, "i") // 'i' para case-insensitive
                );

                modeloTabela.setRowCount(0);
                listaCategorias.clear();

                for (Document doc : resultado) {
                    listaCategorias.add(doc);
                    modeloTabela.addRow(new Object[]{
                            doc.getObjectId("_id").toHexString(),
                            doc.getString("nome")
                    });
                }

                if (listaCategorias.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nenhuma categoria encontrada com o termo: " + termo, "Resultado", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao pesquisar categoria: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}