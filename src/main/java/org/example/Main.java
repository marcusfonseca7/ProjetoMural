package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOError;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class Main {
    private static final RecadoDAO DAO = new RecadoDAO();

    public static void main(String[] args) throws Exception{
        testarConexao();

        //0.0.0.0 aceita conexoes de qualquer ip da rede (celular, outro PC)
        HttpServer servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

        servidor.createContext("/api/recados", Main::atenderRecados);
        servidor.createContext("/", Main::abrirPagina);
        servidor.start();

        System.out.println("Mural aberto em http://localhost:8080");

        System.out.println("Celulares podem acessar pelo ip da rede local na porta 8080");
    }

    private static void testarConexao() throws SQLException {
        try (Connection ignored = Conexao.abrir()){
            System.out.println("Banco de dados conectado!!!");
        }
    }

    private static void atenderRecados(HttpExchange troca) throws IOException{
        troca.getResponseHeaders().set("Acess-Control-Allow-Origin", "*");
        troca.getResponseHeaders().set("Acess-Control-Allow-Methods", "GET, POST, OPTIONS");
        troca.getResponseHeaders().set("Acess-Control-Allow-Headers", "Context-Type");

        try {
            if (troca.getRequestMethod().equals("OPTIONS")) {
                troca.sendResponseHeaders(204, -1);
                troca.close();
            } else if (troca.getRequestMethod().equals("GET")) {
                listar(troca);
            } else if (troca.getRequestMethod().equals("POST")) {
                cadastrar(troca);
            } else {
                troca.getResponseHeaders().set("Allow", "GET, POST, OPTIONS");
                responder(troca, 405,"{\"erro\":\"Método não permitido\"}");
            }
        }catch (SQLException erro) {
            erro.printStackTrace();
            responder(troca, 500, "{\"erro\":\"Erro ao acessoar o banco\"}");
        }
    }

    public static void cadastrar(HttpExchange troca) throws IOException, SQLException {
        Map<String, String> dados = lerFormulario(troca);
        String autor = dados.getOrDefault("autor", "").trim();
        String mensagem = dados.getOrDefault("mensagem", "").trim();

        if(autor.isEmpty() || mensagem.isEmpty()) {
            responder(troca, 400, "{\"erro\":\"Preencha todos os campo\"}");
            return;
        }
        DAO.cadastrar(new Recado(0, autor, mensagem));
        responder(troca, 201, "{\"mensagem\":\"Recado cadastrado\"}");
    }
}