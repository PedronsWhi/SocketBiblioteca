package Server;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    private static final String SERVIDOR_IP = "localhost";
    private static final int PORTA = 12345;

    public static void main(String[] args) {
        try (
            Socket conexao = new Socket(SERVIDOR_IP, PORTA);
            ObjectOutputStream saidaObjeto = new ObjectOutputStream(conexao.getOutputStream());
            ObjectInputStream entradaObjeto = new ObjectInputStream(conexao.getInputStream())
        ) {
            System.out.println("Conectado ao servidor.");

            Scanner leitor = new Scanner(System.in);

            while (true) {
                exibirMenu();
                String escolha = leitor.nextLine().trim();

                switch (escolha) {
                    case "1":
                        saidaObjeto.writeObject("listar");
                        exibirResposta(entradaObjeto);
                        break;
                    case "2":
                        alugarLivro(saidaObjeto, entradaObjeto, leitor);
                        break;
                    case "3":
                        devolverLivro(saidaObjeto, leitor);
                        break;
                    case "4":
                        cadastrarLivro(saidaObjeto, leitor);
                        break;
                    case "0":
                        System.out.println("Encerrando cliente.");
                        saidaObjeto.writeObject("sair");
                        return;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                        break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void exibirMenu() {
        System.out.println("\nEscolha uma opção:");
        System.out.println("1. Listar livros");
        System.out.println("2. Alugar livro");
        System.out.println("3. Devolver livro");
        System.out.println("4. Cadastrar novo livro");
        System.out.println("0. Sair");
    }

    private static void exibirResposta(ObjectInputStream entradaObjeto) {
        try {
            Object resposta = entradaObjeto.readObject();
            if (resposta instanceof String) {
                System.out.println("\nResposta do servidor:");
                System.out.println((String) resposta);
            } else if (resposta instanceof Livro) {
                System.out.println("\nDetalhes do livro recebido:");
                Livro livro = (Livro) resposta;
                System.out.println("Nome: " + livro.getTitulo());
                System.out.println("Autor: " + livro.getEscritor());
                System.out.println("Gênero: " + livro.getCategoria());
                System.out.println("Número de exemplares: " + livro.getQuantidade());
            } else {
                System.out.println("\nResposta inválida do servidor.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("\nErro ao ler resposta do servidor.");
            e.printStackTrace();
        }
    }

    private static void alugarLivro(ObjectOutputStream saidaObjeto, ObjectInputStream entradaObjeto, Scanner leitor) {
        try {
            System.out.println("\nDigite o nome do livro que deseja alugar:");
            String nome = leitor.nextLine().trim();
            saidaObjeto.writeObject("alugar");
            saidaObjeto.writeObject(nome);
            exibirResposta(entradaObjeto);
        } catch (IOException e) {
            System.out.println("Erro ao enviar dados para o servidor.");
            e.printStackTrace();
        }
    }

    private static void devolverLivro(ObjectOutputStream saidaObjeto, Scanner leitor) {
        try {
            System.out.println("\nDigite o nome do livro:");
            String nome = leitor.nextLine().trim();
            System.out.println("Digite o autor do livro:");
            String autor = leitor.nextLine().trim();
            System.out.println("Digite o gênero do livro:");
            String genero = leitor.nextLine().trim();
            System.out.println("Digite o número de exemplares do livro:");
            int quantidade = Integer.parseInt(leitor.nextLine().trim());

            Livro livroDevolvido = new Livro(autor, nome, genero, quantidade);
            saidaObjeto.writeObject("devolver");
            saidaObjeto.writeObject(livroDevolvido);
            System.out.println("Livro devolvido com sucesso.");
        } catch (IOException e) {
            System.out.println("Erro ao enviar dados para o servidor.");
            e.printStackTrace();
        }
    }

    private static void cadastrarLivro(ObjectOutputStream saidaObjeto, Scanner leitor) {
        try {
            System.out.println("\nDigite o nome do livro:");
            String nome = leitor.nextLine().trim();
            System.out.println("Digite o autor do livro:");
            String autor = leitor.nextLine().trim();
            System.out.println("Digite o gênero do livro:");
            String genero = leitor.nextLine().trim();
            System.out.println("Digite o número de exemplares do livro:");
            int quantidade = Integer.parseInt(leitor.nextLine().trim());

            Livro novoLivro = new Livro(autor, nome, genero, quantidade);
            saidaObjeto.writeObject("cadastrar");
            saidaObjeto.writeObject(novoLivro);
            System.out.println("Livro cadastrado com sucesso.");
        } catch (IOException e) {
            System.out.println("Erro ao enviar dados para o servidor.");
            e.printStackTrace();
        }
    }
}