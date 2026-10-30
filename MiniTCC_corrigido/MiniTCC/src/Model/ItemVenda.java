package Model;

import java.math.BigDecimal;

public class ItemVenda {
    private int iditem;
    private int codvenda;
    private int codprod;
    private int qtditem;
    private BigDecimal valorunitario;

    public int getIditem() { return iditem; }
    public void setIditem(int iditem) { this.iditem = iditem; }
    public int getCodvenda() { return codvenda; }
    public void setCodvenda(int codvenda) { this.codvenda = codvenda; }
    public int getCodprod() { return codprod; }
    public void setCodprod(int codprod) { this.codprod = codprod; }
    public int getQtditem() { return qtditem; }
    public void setQtditem(int qtditem) { this.qtditem = qtditem; }
    public BigDecimal getValorunitario() { return valorunitario; }
    public void setValorunitario(BigDecimal valorunitario) { this.valorunitario = valorunitario; }
}
