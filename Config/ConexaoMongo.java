package Config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class ConexaoMongo {
    // URI de conexão com o cluster MongoDB Atlas
    private static final String URI = "mongodb+srv://admin:admin@ti129m-projeto2-equipe1.wwttzbe.mongodb.net/?appName=ti129m-projeto2-equipe13";

    private static MongoClient cliente;
    private static MongoDatabase database;

    static {
        try {
            cliente = MongoClients.create(URI);
            database = cliente.getDatabase("daRoca_bd");
            System.out.println("Conexão com MongoDB estabelecida com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao conectar com o MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public static void close() {
        if (cliente != null) {
            cliente.close();
            System.out.println("Conexão com MongoDB fechada.");
        }
    }
}