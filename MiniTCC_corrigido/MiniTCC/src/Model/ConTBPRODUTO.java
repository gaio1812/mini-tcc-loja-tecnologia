package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/** Acesso à tabela TBPRODUTO. */
public class ConTBPRODUTO {

    public boolean Cadastrar(Produto p) {
        String sql = "INSERT INTO TBPRODUTO "
                + "(nomeprod, categprod, marcprod, qtdprod, precoprod) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, p.getNomeprod());
                ps.setString(2, p.getCategprod());
                ps.setString(3, p.getMarcprod());
                ps.setInt(4, p.getQtdprod());
                ps.setBigDecimal(5, p.getPrecoprod());
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Produto cadastrado com sucesso!");
            return true;
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar produto: " + erro.getMessage());
            return false;
        }
    }

    public boolean Alterar(Produto p) {
        String sql = "UPDATE TBPRODUTO SET nomeprod=?, categprod=?, marcprod=?, "
                + "qtdprod=?, precoprod=? WHERE idprod=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, p.getNomeprod());
                ps.setString(2, p.getCategprod());
                ps.setString(3, p.getMarcprod());
                ps.setInt(4, p.getQtdprod());
                ps.setBigDecimal(5, p.getPrecoprod());
                ps.setInt(6, p.getIdprod());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao alterar produto: " + erro.getMessage());
            return false;
        }
    }

    public boolean Excluir(int idprod) {
        String sql = "DELETE FROM TBPRODUTO WHERE idprod=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idprod);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir produto: " + erro.getMessage());
            return false;
        }
    }

    public Produto BuscarPorId(int idprod) {
        String sql = "SELECT * FROM TBPRODUTO WHERE idprod=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idprod);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? montar(rs) : null;
                }
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar produto: " + erro.getMessage());
            return null;
        }
    }

    public List<Produto> Listar() {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT * FROM TBPRODUTO ORDER BY nomeprod";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return lista;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(montar(rs));
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao listar produtos: " + erro.getMessage());
        }
        return lista;
    }

    private Produto montar(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setIdprod(rs.getInt("idprod"));
        p.setNomeprod(rs.getString("nomeprod"));
        p.setCategprod(rs.getString("categprod"));
        p.setMarcprod(rs.getString("marcprod"));
        p.setQtdprod(rs.getInt("qtdprod"));
        p.setPrecoprod(rs.getBigDecimal("precoprod"));
        return p;
    }
}
