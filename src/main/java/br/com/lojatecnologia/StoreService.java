package br.com.lojatecnologia;

import br.com.lojatecnologia.Models.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Regras de negócio e persistência. Nenhuma operação depende da interface Swing. */
public final class StoreService {
    private final Database db;
    private volatile User currentUser;
    private static final String DUMMY_HASH = Passwords.hash("senha-sem-conta".toCharArray());

    public StoreService(Database db) { this.db = db; }
    public User currentUser() { return currentUser; }
    public boolean isAdmin() { return currentUser != null && currentUser.role().equals("ADMINISTRADOR"); }
    public void logout() { currentUser = null; }

    public boolean hasUsers() throws SQLException {
        try (Connection c = db.open(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM TBUSUARIO")) {
            r.next(); return r.getInt(1) > 0;
        }
    }

    public void register(String name, String email, char[] password, char[] confirmation) throws SQLException {
        String cleanName = Validation.text(name, "o nome", 100, true);
        String cleanEmail = Validation.email(email, true);
        if (password == null || password.length < 8 || password.length > 128) {
            throw new IllegalArgumentException("A senha deve ter de 8 a 128 caracteres.");
        }
        if (!Arrays.equals(password, confirmation)) throw new IllegalArgumentException("As senhas não coincidem.");
        String hash = Passwords.hash(password);
        transaction(c -> {
            // Serializa a escolha do primeiro administrador, inclusive entre processos MySQL.
            try (Statement s = c.createStatement(); ResultSet lock = s.executeQuery("SELECT id FROM TBAPPLOCK WHERE id=1 FOR UPDATE")) { lock.next(); }
            int count;
            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM TBUSUARIO")) { r.next(); count = r.getInt(1); }
            try (PreparedStatement p = c.prepareStatement("INSERT INTO TBUSUARIO(nomeusuario,emailusuario,senhausuario,tipousuario) VALUES (?,?,?,?)")) {
                bind(p, cleanName, cleanEmail, hash, count == 0 ? "ADMINISTRADOR" : "FUNCIONARIO"); p.executeUpdate();
            } catch (SQLException e) { throw constraint(e, "Este e-mail já está cadastrado."); }
            return null;
        });
    }

    public User login(String email, char[] password) throws SQLException {
        currentUser = null;
        String cleanEmail = Validation.email(email, true);
        if (password == null || password.length == 0) throw new IllegalArgumentException("Preencha a senha.");
        if (password.length > 128) throw new IllegalArgumentException("E-mail ou senha incorretos.");
        User user = null;
        String hash = DUMMY_HASH;
        try (Connection c = db.open(); PreparedStatement p = c.prepareStatement("SELECT * FROM TBUSUARIO WHERE emailusuario=?")) {
            p.setString(1, cleanEmail);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    user = new User(r.getInt("idusuario"), r.getString("nomeusuario"), r.getString("emailusuario"), r.getString("tipousuario"));
                    hash = r.getString("senhausuario");
                }
            }
        }
        if (!Passwords.verify(password, hash) || user == null) throw new IllegalArgumentException("E-mail ou senha incorretos.");
        currentUser = user;
        return user;
    }

    private void requireLogin() {
        if (currentUser == null) throw new IllegalStateException("Entre na sua conta para continuar.");
    }
    private void requireAdmin() {
        requireLogin();
        if (!isAdmin()) throw new IllegalStateException("Somente o administrador pode alterar produtos e estoque.");
    }

    public List<Customer> customers() throws SQLException {
        requireLogin();
        return list("SELECT * FROM TBCLIENTE ORDER BY nomecliente", r -> new Customer(r.getInt("idcliente"),
                r.getString("nomecliente"),r.getString("cpfcliente"),r.getString("telcliente"),r.getString("emailcliente"),r.getString("endcliente")));
    }

    public int saveCustomer(Customer customer) throws SQLException {
        requireLogin();
        String name = Validation.text(customer.name(), "o nome", 100, true);
        String cpf = Validation.cpf(customer.cpf());
        String phone = Validation.text(customer.phone(), "o telefone", 15, false);
        String email = Validation.email(customer.email(), false);
        String address = Validation.text(customer.address(), "o endereço", 150, false);
        boolean insert = customer.id() == 0;
        try (Connection c = db.open(); PreparedStatement p = c.prepareStatement(insert
                ? "INSERT INTO TBCLIENTE(nomecliente,cpfcliente,telcliente,emailcliente,endcliente) VALUES (?,?,?,?,?)"
                : "UPDATE TBCLIENTE SET nomecliente=?,cpfcliente=?,telcliente=?,emailcliente=?,endcliente=? WHERE idcliente=?", Statement.RETURN_GENERATED_KEYS)) {
            bind(p, name, cpf, phone, email, address);
            if (!insert) p.setInt(6, customer.id());
            if (p.executeUpdate() != 1) throw new IllegalArgumentException("Cliente não encontrado. Atualize a lista.");
            return insert ? generatedId(p) : customer.id();
        } catch (SQLException e) { throw constraint(e, "Já existe um cliente com este CPF."); }
    }

    public void deleteCustomer(int id) throws SQLException {
        requireLogin();
        delete("DELETE FROM TBCLIENTE WHERE idcliente=?", id, "Este cliente possui vendas e deve ser mantido no histórico.");
    }

    public List<Product> products() throws SQLException {
        requireLogin();
        return list("SELECT * FROM TBPRODUTO ORDER BY nomeprod", StoreService::product);
    }

    public int saveProduct(Product product) throws SQLException {
        requireAdmin();
        String name = Validation.text(product.name(), "o nome do produto", 50, true);
        String category = Validation.text(product.category(), "a categoria", 40, false);
        String brand = Validation.text(product.brand(), "a marca", 40, false);
        if (product.stock() < 0) throw new IllegalArgumentException("O estoque não pode ser negativo.");
        BigDecimal price = Validation.money(product.price());
        boolean insert = product.id() == 0;
        try (Connection c = db.open(); PreparedStatement p = c.prepareStatement(insert
                ? "INSERT INTO TBPRODUTO(nomeprod,categprod,marcprod,qtdprod,precoprod) VALUES (?,?,?,?,?)"
                : "UPDATE TBPRODUTO SET nomeprod=?,categprod=?,marcprod=?,qtdprod=?,precoprod=? WHERE idprod=?", Statement.RETURN_GENERATED_KEYS)) {
            bind(p, name, category, brand, product.stock(), price);
            if (!insert) p.setInt(6, product.id());
            if (p.executeUpdate() != 1) throw new IllegalArgumentException("Produto não encontrado. Atualize a lista.");
            return insert ? generatedId(p) : product.id();
        }
    }

    public void deleteProduct(int id) throws SQLException {
        requireAdmin();
        delete("DELETE FROM TBPRODUTO WHERE idprod=?", id, "Este produto possui vendas e deve ser mantido no histórico.");
    }

    private void delete(String sql, int id, String message) throws SQLException {
        try (Connection c = db.open(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            if (p.executeUpdate() != 1) throw new IllegalArgumentException("Registro não encontrado. Atualize a lista.");
        } catch (SQLException e) { throw constraint(e, message); }
    }

    public List<Sale> sales() throws SQLException {
        requireLogin();
        return list("SELECT v.*,c.nomecliente FROM TBVENDA v JOIN TBCLIENTE c ON c.idcliente=v.codcliente ORDER BY v.idvenda DESC",
                r -> new Sale(r.getInt("idvenda"),r.getString("nomecliente"),r.getTimestamp("datavenda").toLocalDateTime(),
                        r.getString("formapagamento"),r.getString("statusvenda"),r.getBigDecimal("valorvenda")));
    }

    public List<SaleItem> saleItems(int id) throws SQLException {
        requireLogin();
        try (Connection c = db.open(); PreparedStatement p = c.prepareStatement(
                "SELECT p.nomeprod,i.qtditem,i.valorunitario FROM TBITEMVENDA i JOIN TBPRODUTO p ON p.idprod=i.codprod WHERE i.codvenda=? ORDER BY i.iditem")) {
            p.setInt(1, id);
            List<SaleItem> items = new ArrayList<>();
            try (ResultSet r = p.executeQuery()) { while (r.next()) items.add(new SaleItem(r.getString(1), r.getInt(2), r.getBigDecimal(3))); }
            return items;
        }
    }

    public int createSale(int customerId, String payment, Map<Integer,Integer> quantities) throws SQLException {
        return createSale(customerId,payment,quantities,null);
    }

    public int createSale(int customerId, String payment, Map<Integer,Integer> quantities, Map<Integer,BigDecimal> expectedPrices) throws SQLException {
        requireLogin();
        if (!Set.of("PIX", "CARTAO", "DINHEIRO").contains(payment)) throw new IllegalArgumentException("Selecione uma forma de pagamento válida.");
        if (quantities == null || quantities.isEmpty()) throw new IllegalArgumentException("Adicione pelo menos um produto à venda.");
        TreeMap<Integer,Integer> cart = new TreeMap<>(quantities);
        for (Map.Entry<Integer,Integer> item : cart.entrySet()) {
            if (item.getKey() == null || item.getValue() == null || item.getValue() <= 0) throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        return transaction(c -> {
            try (PreparedStatement p = c.prepareStatement("SELECT idcliente FROM TBCLIENTE WHERE idcliente=? FOR UPDATE")) {
                p.setInt(1, customerId);
                try (ResultSet r = p.executeQuery()) { if (!r.next()) throw new IllegalArgumentException("Selecione um cliente cadastrado."); }
            }
            Map<Integer,Product> products = new LinkedHashMap<>();
            BigDecimal total = BigDecimal.ZERO;
            // Ordem fixa reduz deadlocks em vendas concorrentes de vários produtos.
            for (Map.Entry<Integer,Integer> item : cart.entrySet()) {
                try (PreparedStatement p = c.prepareStatement("SELECT * FROM TBPRODUTO WHERE idprod=? FOR UPDATE")) {
                    p.setInt(1, item.getKey());
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new IllegalArgumentException("Um produto foi removido. Atualize a lista.");
                        Product product = product(r);
                        if (expectedPrices != null && (expectedPrices.get(product.id()) == null || expectedPrices.get(product.id()).compareTo(product.price()) != 0)) {
                            throw new IllegalArgumentException("O preço de " + product.name() + " foi alterado. Feche esta venda, atualize e tente novamente.");
                        }
                        if (product.stock() < item.getValue()) throw new IllegalArgumentException("Estoque insuficiente para " + product.name() + ". Disponível: " + product.stock());
                        products.put(product.id(), product);
                        total = total.add(product.price().multiply(BigDecimal.valueOf(item.getValue())));
                    }
                }
            }
            total = Validation.money(total);
            int saleId;
            try (PreparedStatement p = c.prepareStatement("INSERT INTO TBVENDA(codcliente,formapagamento,statusvenda,valorvenda) VALUES (?,?,'CONCLUIDA',?)", Statement.RETURN_GENERATED_KEYS)) {
                bind(p, customerId, payment, total); p.executeUpdate(); saleId = generatedId(p);
            }
            for (Map.Entry<Integer,Integer> item : cart.entrySet()) {
                try (PreparedStatement p = c.prepareStatement("INSERT INTO TBITEMVENDA(codvenda,codprod,qtditem,valorunitario) VALUES (?,?,?,?)")) {
                    bind(p, saleId, item.getKey(), item.getValue(), products.get(item.getKey()).price()); p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE TBPRODUTO SET qtdprod=qtdprod-? WHERE idprod=?")) {
                    bind(p, item.getValue(), item.getKey()); p.executeUpdate();
                }
            }
            return saleId;
        });
    }

    public void cancelSale(int saleId) throws SQLException {
        requireLogin();
        transaction(c -> {
            try (PreparedStatement p = c.prepareStatement("SELECT statusvenda FROM TBVENDA WHERE idvenda=? FOR UPDATE")) {
                p.setInt(1, saleId);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new IllegalArgumentException("Venda não encontrada.");
                    if (r.getString(1).equals("CANCELADA")) throw new IllegalArgumentException("Esta venda já foi cancelada.");
                    if (!r.getString(1).equals("CONCLUIDA")) throw new IllegalArgumentException("Somente vendas concluídas podem ser canceladas por esta versão do sistema.");
                }
            }
            // Usa a quantidade original e bloqueia os produtos na mesma ordem da criação.
            try (PreparedStatement p = c.prepareStatement("SELECT codprod,SUM(qtditem) FROM TBITEMVENDA WHERE codvenda=? GROUP BY codprod ORDER BY codprod")) {
                p.setInt(1, saleId);
                try (ResultSet r = p.executeQuery()) {
                    while (r.next()) {
                        try (PreparedStatement stock = c.prepareStatement("UPDATE TBPRODUTO SET qtdprod=qtdprod+? WHERE idprod=?")) {
                            bind(stock, r.getInt(2), r.getInt(1)); stock.executeUpdate();
                        }
                    }
                }
            }
            try (PreparedStatement p = c.prepareStatement("UPDATE TBVENDA SET statusvenda='CANCELADA' WHERE idvenda=?")) { p.setInt(1,saleId); p.executeUpdate(); }
            return null;
        });
    }

    public Summary summary() throws SQLException {
        requireLogin();
        try (Connection c = db.open(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(
                "SELECT (SELECT COUNT(*) FROM TBCLIENTE), (SELECT COUNT(*) FROM TBPRODUTO), "
                + "(SELECT COUNT(*) FROM TBPRODUTO WHERE qtdprod<=5), (SELECT COALESCE(SUM(valorvenda),0) FROM TBVENDA WHERE statusvenda='CONCLUIDA')")) {
            r.next(); return new Summary(r.getInt(1), r.getInt(2), r.getInt(3), r.getBigDecimal(4));
        }
    }

    private static Product product(ResultSet r) throws SQLException {
        return new Product(r.getInt("idprod"),r.getString("nomeprod"),r.getString("categprod"),r.getString("marcprod"),r.getInt("qtdprod"),r.getBigDecimal("precoprod"));
    }
    private interface Mapper<T> { T map(ResultSet r) throws SQLException; }
    private <T> List<T> list(String sql, Mapper<T> mapper) throws SQLException {
        List<T> values = new ArrayList<>();
        try (Connection c = db.open(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) values.add(mapper.map(r));
        }
        return values;
    }
    private interface Work<T> { T run(Connection c) throws SQLException; }
    private <T> T transaction(Work<T> work) throws SQLException {
        try (Connection c = db.open()) {
            c.setAutoCommit(false);
            try { T value = work.run(c); c.commit(); return value; }
            catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }
    private static int generatedId(PreparedStatement p) throws SQLException {
        try (ResultSet r = p.getGeneratedKeys()) { if (!r.next()) throw new SQLException("O banco não retornou o identificador."); return r.getInt(1); }
    }
    private static void bind(PreparedStatement p, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) p.setObject(i + 1, values[i]);
    }
    private static SQLException constraint(SQLException e, String message) {
        if (e.getSQLState() != null && e.getSQLState().startsWith("23")) throw new IllegalArgumentException(message);
        return e;
    }
}
