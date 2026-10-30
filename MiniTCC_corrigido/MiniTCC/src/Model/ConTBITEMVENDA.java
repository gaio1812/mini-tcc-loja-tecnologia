package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/** Acesso à tabela TBITEMVENDA. */
public class ConTBITEMVENDA {

    public boolean Cadastrar(ItemVenda i) {
        String sql = "INSERT INTO TBITEMVENDA (codvenda, codprod, qtditem, valorunitario) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, i.getCodvenda());
                ps.setInt(2, i.getCodprod());
                ps.setInt(3, i.getQtditem());
                ps.setBigDecimal(4, i.getValorunitario());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar item: " + erro.getMessage());
            return false;
        }
    }

    public boolean Alterar(ItemVenda i) {
        String sql = "UPDATE TBITEMVENDA SET codvenda=?, codprod=?, qtditem=?, "
                + "valorunitario=? WHERE iditem=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, i.getCodvenda());
                ps.setInt(2, i.getCodprod());
                ps.setInt(3, i.getQtditem());
                ps.setBigDecimal(4, i.getValorunitario());
                ps.setInt(5, i.getIditem());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao alterar item: " + erro.getMessage());
            return false;
        }
    }

    public boolean Excluir(int iditem) {
        String sql = "DELETE FROM TBITEMVENDA WHERE iditem=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, iditem);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir item: " + erro.getMessage());
            return false;
        }
    }

    /** Lista os itens de uma venda. */
    public List<ItemVenda> ListarPorVenda(int codvenda) {
        List<ItemVenda> lista = new ArrayList<>();
        String sql = "SELECT * FROM TBITEMVENDA WHERE codvenda=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return lista;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, codvenda);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ItemVenda i = new ItemVenda();
                        i.setIditem(rs.getInt("iditem"));
                        i.setCodvenda(rs.getInt("codvenda"));
                        i.setCodprod(rs.getInt("codprod"));
                        i.setQtditem(rs.getInt("qtditem"));
                        i.setValorunitario(rs.getBigDecimal("valorunitario"));
                        lista.add(i);
                    }
                }
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao listar itens: " + erro.getMessage());
        }
        return lista;
    }
}
