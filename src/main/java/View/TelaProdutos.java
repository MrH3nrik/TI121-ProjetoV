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

public class TelaProdutos extends JFrame {
    private MongoCollection<Document> colecaoProdutos;
    private MongoCollection<Document> colecaoCategorias;
    private DefaultTableModel modeloTabela;
    private JTable tabela;
    private JTextField txtNome, txtPreco, txtEstoque; // Adicionado campo de estoque
    private JComboBox<String> comboCategorias;
    private JButton btnNovo, btnSalvar, btnExcluir, btnPesquisar;
    private List<Document> listaProdutos = new ArrayList<>();
    private List<Document> listaCategorias = new ArrayList<>();
    private int indiceSelecionado = -1;

    public TelaProdutos() {
        setTitle("Manutenção de Produtos");
        setSize(750, 500); // Aumentado um pouco o tamanho para acomodar o novo campo
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Obter coleções
        colecaoProdutos = ConexaoMongo.getDatabase().getCollection("Produtos");
        colecaoCategorias = ConexaoMongo.getDatabase().getCollection("Categorias");

        initComponents();
        carregarCategoriasNoCombo();
        carregarTabela();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel de formulário
        JPanel painelForm = new JPanel(new GridLayout(4, 2, 5, 5)); // Aumentado para 4 linhas
        painelForm.setBorder(BorderFactory.createTitledBorder("Dados do Produto"));

        painelForm.add(new JLabel("Nome:"));
        txtNome = new JTextField();
        painelForm.add(txtNome);

        painelForm.add(new JLabel("Preço:"));
        txtPreco = new JTextField();
        painelForm.add(txtPreco);

        painelForm.add(new JLabel("Estoque:")); // Novo campo de estoque
        txtEstoque = new JTextField();
        painelForm.add(txtEstoque);

        painelForm.add(new JLabel("Categoria:"));
        comboCategorias = new JComboBox<>();
        painelForm.add(comboCategorias);

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
                salvarProduto();
            }
        });

        btnExcluir = new JButton("Excluir");
        btnExcluir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excluirProduto();
            }
        });

        btnPesquisar = new JButton("Pesquisar");
        btnPesquisar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pesquisarProduto();
            }
        });

        painelBotoes.add(btnNovo);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnPesquisar);

        // Configurar tabela - adicionado coluna de estoque
        String[] colunas = {"ID", "Nome", "Preço", "Estoque", "Categoria"};
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
                    exibirProduto(row);
                }
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tabela);

        painelPrincipal.add(painelForm, BorderLayout.NORTH);
        painelPrincipal.add(scrollTabela, BorderLayout.CENTER);
        painelPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        add(painelPrincipal, BorderLayout.CENTER);
    }

    private void carregarCategoriasNoCombo() {
        comboCategorias.removeAllItems();
        listaCategorias.clear();

        try {
            for (Document categoria : colecaoCategorias.find()) {
                listaCategorias.add(categoria);
                String item = categoria.getObjectId("_id").toHexString() + " - " + categoria.getString("nome");
                comboCategorias.addItem(item);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar categorias: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtPreco.setText("");
        txtEstoque.setText(""); // Limpar campo de estoque
        comboCategorias.setSelectedIndex(-1);
        indiceSelecionado = -1;
        tabela.clearSelection();
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        listaProdutos.clear();

        try {
            for (Document produto : colecaoProdutos.find()) {
                listaProdutos.add(produto);

                // Obter nome da categoria
                String nomeCategoria = "Sem categoria";
                if (produto.containsKey("idCategoria")) {
                    ObjectId idCategoria = produto.getObjectId("idCategoria");
                    Document categoria = colecaoCategorias.find(Filters.eq("_id", idCategoria)).first();
                    if (categoria != null) {
                        nomeCategoria = categoria.getString("nome");
                    }
                }

                // Obter estoque (com valor padrão caso não exista)
                int estoque = 0;
                if (produto.containsKey("estoque")) {
                    estoque = produto.getInteger("estoque");
                }

                modeloTabela.addRow(new Object[]{
                        produto.getObjectId("_id").toHexString(),
                        produto.getString("nome"),
                        produto.getDouble("preco"),
                        estoque, // Adicionado estoque na tabela
                        nomeCategoria
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void exibirProduto(int row) {
        if (row >= 0 && row < listaProdutos.size()) {
            Document produto = listaProdutos.get(row);
            txtNome.setText(produto.getString("nome"));
            txtPreco.setText(String.valueOf(produto.getDouble("preco")));

            // Exibir estoque - valor padrão 0 se não existir
            int estoque = produto.containsKey("estoque") ? produto.getInteger("estoque") : 0;
            txtEstoque.setText(String.valueOf(estoque));

            // Selecionar categoria no combo
            if (produto.containsKey("idCategoria")) {
                ObjectId idCategoria = produto.getObjectId("idCategoria");
                String idStr = idCategoria.toHexString();

                for (int i = 0; i < comboCategorias.getItemCount(); i++) {
                    String item = (String) comboCategorias.getItemAt(i);
                    if (item.startsWith(idStr + " - ")) {
                        comboCategorias.setSelectedIndex(i);
                        break;
                    }
                }
            }

            indiceSelecionado = row;
        }
    }

    private void salvarProduto() {
        String nome = txtNome.getText().trim();
        String precoStr = txtPreco.getText().trim();
        String estoqueStr = txtEstoque.getText().trim(); // Obter valor do estoque
        String categoriaSelecionada = (String) comboCategorias.getSelectedItem();

        // Validações
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome do produto é obrigatório!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (precoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O preço do produto é obrigatório!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (estoqueStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O estoque do produto é obrigatório!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoStr.replace(',', '.'));
            if (preco <= 0) {
                JOptionPane.showMessageDialog(this, "O preço deve ser maior que zero!", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Preço inválido! Use números com . ou , para decimais.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int estoque;
        try {
            estoque = Integer.parseInt(estoqueStr);
            if (estoque < 0) {
                JOptionPane.showMessageDialog(this, "O estoque não pode ser negativo!", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Estoque inválido! Digite um número inteiro.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (categoriaSelecionada == null || categoriaSelecionada.equals("")) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para o produto!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Extrair ID da categoria
            String[] partes = categoriaSelecionada.split(" - ", 2);
            if (partes.length < 2) {
                JOptionPane.showMessageDialog(this, "Categoria inválida!", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String idCategoriaStr = partes[0];
            ObjectId idCategoria;

            try {
                idCategoria = new ObjectId(idCategoriaStr);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "ID da categoria inválido!", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Document produto;
            if (indiceSelecionado >= 0 && indiceSelecionado < listaProdutos.size()) {
                // Editar produto existente
                produto = listaProdutos.get(indiceSelecionado);
                colecaoProdutos.updateOne(
                        Filters.eq("_id", produto.getObjectId("_id")),
                        new Document("$set", new Document("nome", nome)
                                .append("preco", preco)
                                .append("estoque", estoque) // Atualizar estoque
                                .append("idCategoria", idCategoria))
                );
                JOptionPane.showMessageDialog(this, "Produto atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Novo produto
                produto = new Document("nome", nome)
                        .append("preco", preco)
                        .append("estoque", estoque) // Adicionar estoque
                        .append("idCategoria", idCategoria);
                colecaoProdutos.insertOne(produto);
                JOptionPane.showMessageDialog(this, "Produto cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            carregarTabela();
            limparCampos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void excluirProduto() {
        if (indiceSelecionado < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para excluir!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir este produto?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION);

        if (resposta == JOptionPane.YES_OPTION) {
            try {
                Document produto = listaProdutos.get(indiceSelecionado);
                ObjectId id = produto.getObjectId("_id");

                // Excluir o produto
                colecaoProdutos.deleteOne(Filters.eq("_id", id));
                JOptionPane.showMessageDialog(this, "Produto excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                carregarTabela();
                limparCampos();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void pesquisarProduto() {
        String termo = JOptionPane.showInputDialog(this, "Digite o nome do produto para pesquisar:");
        if (termo != null && !termo.trim().isEmpty()) {
            try {
                FindIterable<Document> resultado = colecaoProdutos.find(
                        Filters.regex("nome", termo, "i") // 'i' para case-insensitive
                );

                modeloTabela.setRowCount(0);
                listaProdutos.clear();

                for (Document produto : resultado) {
                    listaProdutos.add(produto);

                    // Obter nome da categoria
                    String nomeCategoria = "Sem categoria";
                    if (produto.containsKey("idCategoria")) {
                        ObjectId idCategoria = produto.getObjectId("idCategoria");
                        Document categoria = colecaoCategorias.find(Filters.eq("_id", idCategoria)).first();
                        if (categoria != null) {
                            nomeCategoria = categoria.getString("nome");
                        }
                    }

                    // Obter estoque
                    int estoque = produto.containsKey("estoque") ? produto.getInteger("estoque") : 0;

                    modeloTabela.addRow(new Object[]{
                            produto.getObjectId("_id").toHexString(),
                            produto.getString("nome"),
                            produto.getDouble("preco"),
                            estoque, // Adicionado estoque nos resultados da pesquisa
                            nomeCategoria
                    });
                }

                if (listaProdutos.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nenhum produto encontrado com o termo: " + termo, "Resultado", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao pesquisar produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}