package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/** Acesso à tabela TBVENDA. */
public class ConTBVENDA {

    /** Cadastra a venda e devolve o id gerado (ou -1 se der erro). */
    public int Cadastrar(Venda v) {
        String sql = "INSERT INTO TBVENDA (codcliente, formapagamento, statusvenda, valorvenda) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return -1;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, v.getCodcliente());
                ps.setString(2, v.getFormapagamento());
                ps.setString(3, v.getStatusvenda());
                ps.setBigDecimal(4, v.getValorvenda());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    return keys.next() ? keys.getInt(1) : -1;
                }
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar venda: " + erro.getMessage());
            return -1;
        }
    }

    public boolean Alterar(Venda v) {
        String sql = "UPDATE TBVENDA SET codcliente=?, formapagamento=?, statusvenda=?, "
                + "valorvenda=? WHERE idvenda=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, v.getCodcliente());
                ps.setString(2, v.getFormapagamento());
                ps.setString(3, v.getStatusvenda());
                ps.setBigDecimal(4, v.getValorvenda());
                ps.setInt(5, v.getIdvenda());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao alterar venda: " + erro.getMessage());
            return false;
        }
    }

    /** Exclui a venda junto com seus itens (por causa da chave estrangeira). */
    public boolean Excluir(int idvenda) {
        try (Connection con = new conexao().conectar()) {
            if (con == null) return false;
            con.setAutoCommit(false);
            try (PreparedStatement i = con.prepareStatement("DELETE FROM TBITEMVENDA WHERE codvenda=?");
                 PreparedStatement v = con.prepareStatement("DELETE FROM TBVENDA WHERE idvenda=?")) {
                i.setInt(1, idvenda);
                i.executeUpdate();
                v.setInt(1, idvenda);
                int n = v.executeUpdate();
                con.commit();
                return n > 0;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao excluir venda: " + erro.getMessage());
            return false;
        }
    }

    public Venda BuscarPorId(int idvenda) {
        String sql = "SELECT * FROM TBVENDA WHERE idvenda=?";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idvenda);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? montar(rs) : null;
                }
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar venda: " + erro.getMessage());
            return null;
        }
    }

    public List<Venda> Listar() {
        List<Venda> lista = new ArrayList<>();
        String sql = "SELECT * FROM TBVENDA ORDER BY datavenda DESC";
        try (Connection con = new conexao().conectar()) {
            if (con == null) return lista;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(montar(rs));
            }
        } catch (SQLException erro) {
            JOptionPane.showMessageDialog(null, "Erro ao listar vendas: " + erro.getMessage());
        }
        return lista;
    }

    private Venda montar(ResultSet rs) throws SQLException {
        Venda v = new Venda();
        v.setIdvenda(rs.getInt("idvenda"));
        v.setCodcliente(rs.getInt("codcliente"));
        v.setDatavenda(rs.getTimestamp("datavenda"));
        v.setFormapagamento(rs.getString("formapagamento"));
        v.setStatusvenda(rs.getString("statusvenda"));
        v.setValorvenda(rs.getBigDecimal("valorvenda"));
        return v;
    }
}
