package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/** Acesso à tabela TBCLIENTE. */
public class ConTBCLIENTE {

    public boolean Cadastrar(Cliente c) {
        String sql = "INSERT INTO TBCLIENTE "
                + "(nomecliente, cpfcliente, telcliente, emailcliente, endcliente) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, c.getNomecliente());
                ps.setString(2, c.getCpfcliente());
                ps.setString(3, c.getTelcliente());
                ps.setString(4, c.getEmailcliente());
                ps.setString(5, c.getEndcliente());
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Cliente cadastrado com sucesso!");
            return true;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar cliente: " + erro.getMessage());
            return false;
        }
    }

    public boolean Alterar(Cliente c) {
        String sql = "UPDATE TBCLIENTE SET nomecliente=?, cpfcliente=?, telcliente=?, "
                + "emailcliente=?, endcliente=? WHERE idcliente=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, c.getNomecliente());
                ps.setString(2, c.getCpfcliente());
                ps.setString(3, c.getTelcliente());
                ps.setString(4, c.getEmailcliente());
                ps.setString(5, c.getEndcliente());
                ps.setInt(6, c.getIdcliente());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao alterar cliente: " + erro.getMessage());
            return false;
        }
    }

    public boolean Excluir(int idcliente) {
        String sql = "DELETE FROM TBCLIENTE WHERE idcliente=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idcliente);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir cliente: " + erro.getMessage());
            return false;
        }
    }

    public Cliente BuscarPorId(int idcliente) {
        String sql = "SELECT * FROM TBCLIENTE WHERE idcliente=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idcliente);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? montar(rs) : null;
                }
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar cliente: " + erro.getMessage());
            return null;
        }
    }

    public List<Cliente> Listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM TBCLIENTE ORDER BY nomecliente";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return lista;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(montar(rs));
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao listar clientes: " + erro.getMessage());
        }
        return lista;
    }

    private Cliente montar(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdcliente(rs.getInt("idcliente"));
        c.setNomecliente(rs.getString("nomecliente"));
        c.setCpfcliente(rs.getString("cpfcliente"));
        c.setTelcliente(rs.getString("telcliente"));
        c.setEmailcliente(rs.getString("emailcliente"));
        c.setEndcliente(rs.getString("endcliente"));
        return c;
    }
}
