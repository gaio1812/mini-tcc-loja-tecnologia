package Model;

import java.math.BigDecimal;

public class Produto {
    private int idprod;
    private String nomeprod;
    private String categprod;
    private String marcprod;
    private int qtdprod;
    private BigDecimal precoprod;

    public int getIdprod() { return idprod; }
    public void setIdprod(int idprod) { this.idprod = idprod; }
    public String getNomeprod() { return nomeprod; }
    public void setNomeprod(String nomeprod) { this.nomeprod = nomeprod; }
    public String getCategprod() { return categprod; }
    public void setCategprod(String categprod) { this.categprod = categprod; }
    public String getMarcprod() { return marcprod; }
    public void setMarcprod(String marcprod) { this.marcprod = marcprod; }
    public int getQtdprod() { return qtdprod; }
    public void setQtdprod(int qtdprod) { this.qtdprod = qtdprod; }
    public BigDecimal getPrecoprod() { return precoprod; }
    public void setPrecoprod(BigDecimal precoprod) { this.precoprod = precoprod; }
}
