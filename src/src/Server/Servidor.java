package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Servidor {
    private static final int PORTA = 12345;
    private static final String ARQUIVO_LIVROS = "Server/livros.json";
    private static List<Livro> listaLivros = new ArrayList<>();

    public static void main(String[] args) {
        carregarLivrosDoArquivo();

        try (ServerSocket servidorSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor iniciado. Aguardando conexões...");

            while (true) {
                Socket clienteSocket = servidorSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket);

                new Thread(new TratadorDeCliente(clienteSocket)).start();
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

    private static void salvarLivrosNoArquivo() {
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

    private static class TratadorDeCliente implements Runnable {
        private final Socket socketCliente;

        public TratadorDeCliente(Socket socket) {
            this.socketCliente = socket;
        }

        @Override
        public void run() {
            try (
                ObjectOutputStream saidaObjeto = new ObjectOutputStream(socketCliente.getOutputStream());
                ObjectInputStream entradaObjeto = new ObjectInputStream(socketCliente.getInputStream())
            ) {
                System.out.println("Thread iniciada para o cliente: " + socketCliente);

                while (true) {
                    Object comando = entradaObjeto.readObject();
                    if (comando instanceof String) {
                        switch ((String) comando) {
                            case "listar":
                                saidaObjeto.writeObject(listarLivros());
                                break;
                            case "alugar":
                                alugarLivro(entradaObjeto, saidaObjeto);
                                break;
                            case "devolver":
                                devolverLivro(entradaObjeto, saidaObjeto);
                                break;
                            case "cadastrar":
                                cadastrarLivro(entradaObjeto);
                                break;
                            case "sair":
                                System.out.println("Cliente desconectado: " + socketCliente);
                                return;
                            default:
                                saidaObjeto.writeObject("Comando inválido.");
                                break;
                        }
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    socketCliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String listarLivros() {
            StringBuilder listaLivrosStr = new StringBuilder();
            for (Livro livro : listaLivros) {
                listaLivrosStr.append(livro.getTitulo()).append(" - ").append(livro.getEscritor()).append(" - ").append(livro.getQuantidade()).append(" exemplares\n");
            }
            return listaLivrosStr.toString();
        }

        private void alugarLivro(ObjectInputStream entradaObjeto, ObjectOutputStream saidaObjeto) throws IOException, ClassNotFoundException {
            String nomeLivro = (String) entradaObjeto.readObject();
            Livro livroParaAlugar = null;

            for (Livro livro : listaLivros) {
                if (livro.getTitulo().equalsIgnoreCase(nomeLivro)) {
                    livroParaAlugar = livro;
                    break;
                }
            }

            if (livroParaAlugar != null && livroParaAlugar.getQuantidade() > 0) {
                livroParaAlugar.setQuantidade(livroParaAlugar.getQuantidade() - 1);
                if (livroParaAlugar.getQuantidade() == 0) {
                    listaLivros.remove(livroParaAlugar);
                }
                salvarLivrosNoArquivo();
                saidaObjeto.writeObject("Livro alugado com sucesso.");
            } else {
                saidaObjeto.writeObject("Livro não disponível para aluguel.");
            }
        }

        private void devolverLivro(ObjectInputStream entradaObjeto, ObjectOutputStream saidaObjeto) throws IOException, ClassNotFoundException {
            Livro livroDevolvido = (Livro) entradaObjeto.readObject();
            boolean livroExiste = false;

            for (Livro livro : listaLivros) {
                if (livro.getTitulo().equalsIgnoreCase(livroDevolvido.getTitulo())) {
                    livro.setQuantidade(livro.getQuantidade() + 1);
                    livroExiste = true;
                    break;
                }
            }

            if (!livroExiste) {
                listaLivros.add(livroDevolvido);
            }

            salvarLivrosNoArquivo();
            saidaObjeto.writeObject("Livro devolvido com sucesso.");
        }

        private void cadastrarLivro(ObjectInputStream entradaObjeto) throws IOException, ClassNotFoundException {
            Object objetoRecebido = entradaObjeto.readObject();
            if (objetoRecebido instanceof Livro) {
                Livro novoLivro = (Livro) objetoRecebido;
                listaLivros.add(novoLivro);
                salvarLivrosNoArquivo();
                System.out.println("Novo livro cadastrado: " + novoLivro.getTitulo());
            } else {
                System.out.println("Dados inválidos para cadastro de livro.");
            }
        }
    }
}