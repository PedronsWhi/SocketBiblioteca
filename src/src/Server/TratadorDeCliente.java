package Server;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class TratadorDeCliente implements Runnable {
    private final Socket socketCliente;
    private final List<Livro> listaLivros;

    public TratadorDeCliente(Socket socket, List<Livro> listaLivros) {
        this.socketCliente = socket;
        this.listaLivros = listaLivros;
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
            Servidor.salvarLivrosNoArquivo();
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

        Servidor.salvarLivrosNoArquivo();
        saidaObjeto.writeObject("Livro devolvido com sucesso.");
    }

    private void cadastrarLivro(ObjectInputStream entradaObjeto) throws IOException, ClassNotFoundException {
        Object objetoRecebido = entradaObjeto.readObject();
        if (objetoRecebido instanceof Livro) {
            Livro novoLivro = (Livro) objetoRecebido;
            listaLivros.add(novoLivro);
            Servidor.salvarLivrosNoArquivo();
            System.out.println("Novo livro cadastrado: " + novoLivro.getTitulo());
        } else {
            System.out.println("Dados inválidos para cadastro de livro.");
        }
    }
}
