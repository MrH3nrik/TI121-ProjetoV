import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Scanner;

import static com.mongodb.client.model.Indexes.descending;     // para ordenação de resultados de consultas

public class Main {

    static Scanner leitor = new Scanner(System.in);

    public static void main(String[] args) {


        // "mongodb+srv://nomeDoUsuario:senha@cluster0.zn5jm6v.mongodb.net/DaRoça?retryWrites=true&w=majority";
        // @cluster0.zn5jm6v.mongodb.net é um endereço de exemplo de um BD no Atlas (em nuvem)
        String uri = "mongodb+srv://admin:admin@ti129m-projeto2-equipe1.mongodb.net/daRoca_bd?retryWrites=true&w=majority";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("daRoca_bd");
            MongoCollection<Document> aColecao = database.getCollection("Produtos");     // corresponde a Tabela em SQL

            // supomos que a coleção aberta possui campos _id (inteiro), campo1 (string), campo2 (double), ... até campoN (inteiro)
            // o _id de cada documento incluído na coleção será gerado pelo código abaixo:

            System.out.print("Deseja incluir um documento da coleção aberta? ");                   // documento da coleção corresponde a registro da tabela no SQL
            String resposta = leitor.nextLine();
            if (resposta.equalsIgnoreCase("S")) {

                // fornecimento dos dados a serem armazenados no novo documento (registro)
                System.out.print("Campo 1 :");
                String campoString = leitor.nextLine();
                System.out.print("Campo 2 :");
                Double campoDouble = leitor.nextDouble();
                // ...
                System.out.print("Campo n:");
                int campoInt = leitor.nextInt();

                // Criando o próximo _id:

                // Passo 1: Buscar o maior _id atual
                Document ultimoId = aColecao.find()
                        .sort(descending("_id"))
                        .limit(1)
                        .first();

                // gera o próximo id
                int proximoId = (ultimoId != null) ? ultimoId.getInteger("_id") + 1 : 1;

                // gera o documento do novo produto:
                Document novoDocumento = new Document("_id", proximoId).append("nomeDoCampo1", campoString).append("nomeDoCampo2", campoDouble)./* ... outros appends ... */
                                                                        append("nomeDoCampoN", campoInt);

                // insere o documento na coleção
                aColecao.insertOne(novoDocumento);
            }

            System.out.print("Deseja excluir um documento? ");
            resposta = leitor.nextLine();
            if (resposta.equalsIgnoreCase("S")) {
                System.out.print("Qual o id do documento a excluir na coleção aberta? ");
                int idDocumento = leitor.nextInt();
                Document filtro = new Document("_id", idDocumento);
                DeleteResult resultado = aColecao.deleteOne(filtro);
                if (resultado.getDeletedCount() > 0)
                    System.out.println("Documento removido da coleção.");
                else
                    System.out.println("Documento não encontrado na coleção.");

            }

            System.out.print("Deseja alterar um documento? ");
            resposta = leitor.nextLine();
            if (resposta.equalsIgnoreCase("S")) {
                System.out.print("Qual o id do documento a alterar na coleção aberta? ");
                int idDocumento = leitor.nextInt();
                Document filtro = new Document("_id", idDocumento);
                FindIterable<Document> resultado = aColecao.find(filtro);
                Document oDocumentoAAlterar = resultado.first();          // acessa o 1o documento do resultado pesquisado
                if (oDocumentoAAlterar != null)
                {
                    String campoString = oDocumentoAAlterar.getString("nomeDoCampo1");
                    double campoDouble = oDocumentoAAlterar.getDouble("nomeDoCampo2");
                    /* ... solicitação de valores de outros campos */
                    int campoInt = oDocumentoAAlterar.getInteger("nomeDoCampoN");


                    System.out.println("Documento encontrado:");
                    System.out.println("ID: " + oDocumentoAAlterar.getInteger("_id"));
                    System.out.println("Campo 1: " + campoString);
                    System.out.println("Campo 2: " + campoDouble);
                    /* exibição de valores de outros campos */
                    System.out.println("Campo N: " + campoInt);


                    System.out.println("\nDigite os novos valores. Pressione [Enter] para manter o valor original\n");
                    System.out.print("Novo Campo 1: ");
                    campoString = leitor.nextLine();
                    if (campoString.isEmpty())
                        campoString = oDocumentoAAlterar.getString("NomeDoCampo1");

                    System.out.print("Novo Campo 2: ");
                    String sCampoDouble = leitor.nextLine();
                    if (!sCampoDouble.isEmpty())
                        campoDouble = Double.parseDouble(sCampoDouble);

                    /* ... digitação de novos calores dos demais campos ou [Enter] */
                    System.out.print("Nova Campo N: ");
                    String sCampoInt = leitor.nextLine();
                    if (!sCampoInt.isEmpty())
                        campoInt = Integer.parseInt(sCampoInt);

                    Document documentoAlterado = new Document().append("_id", idDocumento).append("nomeCampo1", campoString).
                                                                append("nomeCampo2", campoDouble). /* ... outros append("nome dos demais campos", novoValorDeCampo).  ... */
                                                                append("nomeCampoN", campoInt);

                    Document atualizacao = new Document("$set", documentoAlterado);

                    UpdateResult resultadoUpdate = aColecao.updateOne(filtro, atualizacao);

                    if (resultadoUpdate.getModifiedCount() > 0)
                        System.out.println("Documento com _id " + idDocumento + " atualizado com sucesso.");
                    else
                        System.out.println("Documento não encontrado na coleção.");
                }
                else
                    System.out.println("Nenhum produto encontrado com _id " + idDocumento);

                // Exibir o conteúdo modificado da coleção aberta:

                // Python
                // for doc in osProdutos.find():
                //      print(doc.toJson());

                System.out.println("Id\tNome\tValor\tTipo");
                for (Document doc : aColecao.find()) {
                    System.out.println(doc.getInteger("_id")+"\t"+doc.getString("nomeDoCampo1")+"\t"+doc.getDouble("nomeDoCampo2")+"\t"+
                            /*
                            ... outros doc.getTipo("nome de campo") + ...
                             */
                            doc.getInteger("nomeDoCampoN"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
