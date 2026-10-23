package br.com.lojatecnologia;

import br.com.lojatecnologia.Models.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/** Verificações reais contra H2, sem substituir o banco por mocks. */
public final class IntegrationTest {
    private static int checks;
    private static final char[] PASSWORD="UmaSenha123!".toCharArray();
    public static void main(String[] args) throws Exception {
        Database db=new Database("jdbc:h2:mem:integration;MODE=MySQL;DB_CLOSE_DELAY=-1","sa","");
        db.initialize(); db.initialize();
        StoreService service=new StoreService(db);
        expect(IllegalStateException.class,service::customers);
        check(!service.hasUsers(),"Banco começa sem contas");
        expect(IllegalArgumentException.class,()->service.register("Admin","invalido",PASSWORD,PASSWORD));
        expect(IllegalArgumentException.class,()->service.register("Admin","admin@loja.test","123".toCharArray(),"123".toCharArray()));
        expect(IllegalArgumentException.class,()->service.register("Admin","admin@loja.test",PASSWORD,"outra senha".toCharArray()));
        service.register("Administrador","ADMIN@loja.test",PASSWORD,PASSWORD);
        service.register("Funcionário","func@loja.test",PASSWORD,PASSWORD);
        expect(IllegalArgumentException.class,()->service.register("Outro","admin@loja.test",PASSWORD,PASSWORD));
        expect(IllegalArgumentException.class,()->service.login("admin@loja.test","incorreta".toCharArray()));
        service.login(" Admin@loja.test ",PASSWORD);
        check(service.isAdmin(),"Primeira conta é administradora");
        try(Connection c=db.open();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT senhausuario FROM TBUSUARIO")) {
            r.next();String hash=r.getString(1);check(!hash.equals(new String(PASSWORD)) && hash.startsWith("pbkdf2_sha256$"),"Senha persistida como hash");
        }
        check(!Passwords.hash(PASSWORD).equals(Passwords.hash(PASSWORD)),"Hashes usam salts distintos");
        check(!Passwords.verify(PASSWORD,"texto-puro"),"Senha legada em texto é recusada");
        int customer=service.saveCustomer(new Customer(0,"Cliente teste","52998224725","11999999999","cliente@loja.test","Rua A"));
        check(service.customers().get(0).cpf().equals("529.982.247-25"),"CPF é validado e normalizado");
        expect(IllegalArgumentException.class,()->service.saveCustomer(new Customer(0,"Duplicado","529.982.247-25","","","")));
        expect(IllegalArgumentException.class,()->service.saveCustomer(new Customer(0,"CPF inválido","11111111111","","","")));
        int laptop=service.saveProduct(new Product(0,"Notebook","Computadores","Dell",5,new BigDecimal("3500.00")));
        int mouse=service.saveProduct(new Product(0,"Mouse","Periféricos","Logitech",3,new BigDecimal("150.25")));
        expect(IllegalArgumentException.class,()->service.saveProduct(new Product(0,"Negativo","","",-1,BigDecimal.ONE)));
        expect(IllegalArgumentException.class,()->service.saveProduct(new Product(0,"Precisão","","",1,new BigDecimal("1.001"))));
        int sale=service.createSale(customer,"PIX",Map.of(laptop,1,mouse,2));
        check(stock(service,laptop)==4 && stock(service,mouse)==1,"Venda baixa o estoque de todos os itens");
        check(service.sales().get(0).total().compareTo(new BigDecimal("3800.50"))==0,"Total usa valores decimais exatos");
        check(service.saleItems(sale).size()==2,"Itens da venda persistidos");
        check(service.summary().revenue().compareTo(new BigDecimal("3800.50"))==0,"Resumo soma vendas concluídas");
        expect(IllegalArgumentException.class,()->service.createSale(customer,"PIX",Map.of(laptop,1,mouse,2)));
        check(stock(service,laptop)==4 && service.sales().size()==1,"Estoque insuficiente desfaz toda a venda");
        try(Connection c=db.open();Statement s=c.createStatement()) {s.execute("ALTER TABLE TBITEMVENDA ADD CONSTRAINT TEST_REJECT_SECOND CHECK(codprod<>"+mouse+" OR qtditem<>1)");}
        try {
            expect(SQLException.class,()->service.createSale(customer,"PIX",Map.of(laptop,1,mouse,1)));
            check(stock(service,laptop)==4 && stock(service,mouse)==1 && service.sales().size()==1,"Falha após gravar o primeiro item desfaz cabeçalho, itens e estoque");
        } finally {
            try(Connection c=db.open();Statement s=c.createStatement()) {s.execute("ALTER TABLE TBITEMVENDA DROP CONSTRAINT TEST_REJECT_SECOND");}
        }
        expect(IllegalArgumentException.class,()->service.createSale(customer,"PIX",Map.of(mouse,0)));
        expect(IllegalArgumentException.class,()->service.createSale(customer,"INVALIDO",Map.of(mouse,1)));
        expect(IllegalArgumentException.class,()->service.createSale(-1,"PIX",Map.of(mouse,1)));
        expect(IllegalArgumentException.class,()->service.createSale(customer,"PIX",Map.of(mouse,1),Map.of(mouse,BigDecimal.ONE)));
        check(stock(service,mouse)==1 && service.sales().size()==1,"Preço alterado não conclui venda");
        expect(IllegalArgumentException.class,()->service.deleteProduct(mouse));
        expect(IllegalArgumentException.class,()->service.deleteCustomer(customer));
        service.saveProduct(new Product(mouse,"Mouse atualizado","Periféricos","Logitech",1,new BigDecimal("200.00")));
        check(service.saleItems(sale).get(1).price().compareTo(new BigDecimal("150.25"))==0,"Preço da venda não muda ao editar o produto");
        service.cancelSale(sale);
        check(stock(service,laptop)==5 && stock(service,mouse)==3,"Cancelamento devolve estoque");
        expect(IllegalArgumentException.class,()->service.cancelSale(sale));
        check(stock(service,mouse)==3 && service.summary().revenue().signum()==0,"Cancelamento não duplica estoque nem receita");
        int temporary=service.saveProduct(new Product(0,"Temporário","","",0,BigDecimal.ZERO));service.deleteProduct(temporary);
        service.login("func@loja.test",PASSWORD);
        check(!service.isAdmin(),"Demais contas são de funcionário");
        expect(IllegalStateException.class,()->service.saveProduct(new Product(0,"Sem permissão","","",0,BigDecimal.ZERO)));
        expect(IllegalStateException.class,()->service.deleteProduct(laptop));
        service.logout();expect(IllegalStateException.class,()->service.createSale(customer,"PIX",Map.of(mouse,1)));
        service.login("admin@loja.test",PASSWORD);
        concurrentSales(db,service,customer);
        persistence();
        System.out.println("OK: "+checks+" verificações de cadastro, autenticação, permissões, vendas, rollback, concorrência e persistência.");
    }
    private static void concurrentSales(Database db,StoreService admin,int customer) throws Exception {
        int scarce=admin.saveProduct(new Product(0,"Última unidade","","",1,new BigDecimal("10.00")));
        StoreService one=new StoreService(db),two=new StoreService(db);one.login("func@loja.test",PASSWORD);two.login("func@loja.test",PASSWORD);
        CountDownLatch ready=new CountDownLatch(1);ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> results=new ArrayList<>();
            for(StoreService svc:List.of(one,two))results.add(pool.submit(()->{ready.await();try {svc.createSale(customer,"PIX",Map.of(scarce,1));return true;}catch(IllegalArgumentException e){return false;}}));
            ready.countDown();int completed=0;for(Future<Boolean> r:results)if(r.get(10,TimeUnit.SECONDS))completed++;
            check(completed==1 && stock(admin,scarce)==0,"Vendas concorrentes não vendem a mesma unidade duas vezes");
        } finally {pool.shutdownNow();}
    }
    private static void persistence() throws Exception {
        Path folder=Files.createTempDirectory(Path.of("build"),"persistence-");
        Database db=new Database("jdbc:h2:file:"+folder.toAbsolutePath().resolve("loja").toString().replace('\\','/')+";MODE=MySQL","sa","");
        try {
            db.initialize();StoreService first=new StoreService(db);first.register("Persistente","persistente@loja.test",PASSWORD,PASSWORD);
            // Outra instância reabre o arquivo depois do fechamento de todas as conexões.
            Database reopened=new Database("jdbc:h2:file:"+folder.toAbsolutePath().resolve("loja").toString().replace('\\','/')+";MODE=MySQL","sa","");
            reopened.initialize();StoreService second=new StoreService(reopened);
            check(second.login("persistente@loja.test",PASSWORD).name().equals("Persistente"),"Contas continuam salvas ao reabrir o banco");
        } finally {
            try(var files=Files.list(folder)){for(Path f:files.toList())Files.delete(f);}Files.delete(folder);
        }
    }
    private static int stock(StoreService service,int id) throws SQLException {return service.products().stream().filter(p->p.id()==id).findFirst().orElseThrow().stock();}
    private static void check(boolean ok,String message) {checks++;if(!ok)throw new AssertionError(message);}
    @FunctionalInterface private interface Action {void run() throws Exception;}
    private static void expect(Class<? extends Throwable> type,Action action) throws Exception {
        checks++;try {action.run();}catch(Throwable e){if(type.isInstance(e))return;throw new AssertionError("Esperava "+type.getSimpleName()+", recebeu "+e,e);}throw new AssertionError("Esperava "+type.getSimpleName());
    }
}
