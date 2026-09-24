package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.imageio.event.IIOWriteProgressListener;import java.io.IO;import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;import java.net.InetSocketAddress;
import java.net.URLDecoder;import java.nio.charset.StandardCharsets;import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;import java.util.List;import java.util.Map;

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

    private static void listar(HttpExchange troca) throws IOException, SQLException {
        List<Recado> recados = DAO.listar();

        //StringBuilder: classe para construir e alterar textos sem criar uma nova
        //String a cada mudança, melhor para trabalhar com textos em vez da String ficar mapeando a cada mudança
        StringBuilder json = new StringBuilder("[");

        for (int i=0; i < recados.size(); i++) {
            if (i > 0) {
                json.append(",");
            } //append acrescenta conteudo no StringBuilder
                json.append(recados.get(i).paraJson());
        }
        json.append("]");
        responder(troca, 200, json.toString());
    }

    private static Map<String, String> lerFormulario(HttpExchange troca) throws IOException {
        String corpo = new String(
                troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8
        );
        Map<String, String> dados = new HashMap<>();

        for (String campo : corpo.split("&")){
            String[] partes = campo.split("=", 2);
            String nome = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length == 2
                    ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
            dados.put(nome, valor);
        }
        return dados;
    }

    private static void abrirPagina(HttpExchange troca) throws IOException {
        if (!troca.getRequestMethod().equals("GET")) {
            responder(troca, 405, "Método não permitido", "text/plain");
            return;
        }
        try (InputStream arquivo = Main.class.getResourceAsStream("/public/index.html")) {
            if (arquivo == null) {
                responder(troca, 404, "Página nao encontrada.", "text/plain");
                return;
            }

            byte[] pagina = arquivo.readAllBytes();
            troca.getResponseHeaders().set("Context-Type", "text/html; charset=UTF-8");
            troca.sendResponseHeaders(200, pagina.length);
            troca.getResponseBody().write(pagina);
            troca.close();
        }
    }

    private static void responder(HttpExchange troca, int status, String conteudo) throws IOException {responder(troca, status, conteudo, "application/json");
    }
    private static void responder(
            HttpExchange troca, int status, String conteudo, String tipo
    ) throws IOException {
        byte[] resposta = conteudo.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Context-Type", tipo + "; charset=UTF-8");
        troca.sendResponseHeaders(status, resposta.length);
        troca.getResponseBody().write(resposta);
        troca.close();
    }


}