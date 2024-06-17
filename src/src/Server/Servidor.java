Servidor:
package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Servidor {
    private static final int PORTA = 12345;
    private static final String ARQUIVO_LIVROS = "livros.json"; //não foi possível arrumar a rota
    private static List<Livro> listaLivros = new ArrayList<>();

    public static void main(String[] args) {
        carregarLivrosDoArquivo();

        try (ServerSocket servidorSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor iniciado. Aguardando conexões...");

            while (true) {
                Socket clienteSocket = servidorSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket);

                new Thread(new TratadorDeCliente(clienteSocket, listaLivros)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void carregarLivrosDoArquivo() {
        try {
            File arquivo = new File(ARQUIVO_LIVROS);

            if (arquivo.exists()) {
                Scanner scanner = new Scanner(arquivo);

                StringBuilder conteudoJson = new StringBuilder();
                while (scanner.hasNextLine()) {
                    conteudoJson.append(scanner.nextLine());
                }
                scanner.close();

                String json = conteudoJson.toString();
                if (!json.isEmpty()) {
                    String[] livrosArray = json.split("\\},\\s*\\{");

                    for (String livroStr : livrosArray) {
                        String livroJson = livroStr.replaceAll("[{}\"]", "");
                        String[] partes = livroJson.split(",");
                        String escritor = obterValor(partes[0]);
                        String titulo = obterValor(partes[1]);
                        String categoria = obterValor(partes[2]);
                        int quantidade = Integer.parseInt(obterValor(partes[3]));

                        Livro livro = new Livro(escritor, titulo, categoria, quantidade);
                        listaLivros.add(livro);
                    }
                }
            } else {
                System.out.println("Arquivo de livros não encontrado. Criando lista vazia.");
                salvarLivrosNoArquivo();
            }

            System.out.println("Livros carregados do arquivo.");
        } catch (IOException e) {
            System.out.println("Erro ao carregar livros do arquivo. Criando lista vazia.");
            salvarLivrosNoArquivo();
        }
    }

    private static String obterValor(String parte) {
        return parte.split(":")[1].trim();
    }

    public static void salvarLivrosNoArquivo() {
        try (PrintWriter escritorArquivo = new PrintWriter(new FileWriter(ARQUIVO_LIVROS))) {
            for (Livro livro : listaLivros) {
                escritorArquivo.println(String.format("{\"escritor\":\"%s\",\"titulo\":\"%s\",\"categoria\":\"%s\",\"quantidade\":%d}",
                        livro.getEscritor(), livro.getTitulo(), livro.getCategoria(), livro.getQuantidade()));
            }
            System.out.println("Livros salvos no arquivo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

Tratador de Cliente:
package Server;

import java.io.*;
import java.net.Socket;
import java.util.List;
