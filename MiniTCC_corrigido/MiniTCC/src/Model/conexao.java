package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class conexao {

    // Nome do banco = o mesmo do "CREATE DATABASE" do loja.sql
    private static final String URL = "jdbc:mysql://localhost:3306/BDLOJATECNOLOGIA"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Sao_Paulo";
    private static final String USUARIO = "root";
    private static final String SENHA = "";   // coloque aqui a senha do seu MySQL, se tiver

    public Connection conectar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection objcon = DriverManager.getConnection(URL, USUARIO, SENHA);
            System.out.println("Conexão criada com sucesso!");
            return objcon;

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "Driver do MySQL não encontrado.\n"
                    + "Adicione lib/mysql-connector-j-26.10.0.jar às Bibliotecas do projeto.");
            return null;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao conectar no banco:\n" + ex.getMessage());
            return null;
        }
    }
}
