package Model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Venda {
    private int idvenda;
    private int codcliente;
    private Timestamp datavenda;
    private String formapagamento;
    private String statusvenda = "PENDENTE";
    private BigDecimal valorvenda = BigDecimal.ZERO;

    public int getIdvenda() { return idvenda; }
    public void setIdvenda(int idvenda) { this.idvenda = idvenda; }
    public int getCodcliente() { return codcliente; }
    public void setCodcliente(int codcliente) { this.codcliente = codcliente; }
    public Timestamp getDatavenda() { return datavenda; }
    public void setDatavenda(Timestamp datavenda) { this.datavenda = datavenda; }
    public String getFormapagamento() { return formapagamento; }
    public void setFormapagamento(String formapagamento) { this.formapagamento = formapagamento; }
    public String getStatusvenda() { return statusvenda; }
    public void setStatusvenda(String statusvenda) { this.statusvenda = statusvenda; }
    public BigDecimal getValorvenda() { return valorvenda; }
    public void setValorvenda(BigDecimal valorvenda) { this.valorvenda = valorvenda; }
}
