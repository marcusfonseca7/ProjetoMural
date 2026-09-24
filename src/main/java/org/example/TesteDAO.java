package org.example;

import java.sql.SQLException;

public class TesteDAO {
    public static void main(String[] args) throws SQLException {
        RecadoDAO dao = new RecadoDAO();
        dao.cadastrar(new Recado(0, "Marcus", "São Paulo Futebol Clube"));
        for (Recado recado : dao.listar()) {
            System.out.println(recado.getAutor() + ": " + recado.getMensagem());
        }
    }
}
