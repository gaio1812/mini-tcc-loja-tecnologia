package br.com.lojatecnologia;

import br.com.lojatecnologia.Models.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

final class MainFrame extends JFrame {
    private final StoreService service;
    private final Database db;
    private final CardLayout cards=new CardLayout();
    private final JPanel pages=new JPanel(cards);
    private final JTable customers=Ui.table("Código","Nome","CPF","Telefone","E-mail","Endereço");
    private final JTable products=Ui.table("Código","Produto","Categoria","Marca","Estoque","Preço");
    private final JTable sales=Ui.table("Código","Cliente","Data","Pagamento","Situação","Total");
    private final JLabel customerCount=new JLabel("—"), productCount=new JLabel("—"), lowStock=new JLabel("—"), revenue=new JLabel("—");
    private final JLabel status=new JLabel("Carregando dados…");
    private List<Customer> customerData=List.of();
    private List<Product> productData=List.of();
    private List<Sale> saleData=List.of();

    MainFrame(StoreService service,Database db) {
        this(service,db,true);
    }

    MainFrame(StoreService service,Database db,boolean loadData) {
        this.service=service; this.db=db;
        setTitle("Loja Tecnologia • Sistema de vendas"); setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180,780); setMinimumSize(new Dimension(980,680)); setLocationRelativeTo(null);
        JPanel root=new JPanel(new BorderLayout());
        root.add(sidebar(),BorderLayout.WEST);
        pages.setBorder(new EmptyBorder(28,28,20,28)); pages.setBackground(Ui.BG);
        pages.add(home(),"Início");
        pages.add(page("Clientes",customers,Ui.button("Novo cliente",()->customerDialog(null)),
                Ui.button("Editar",()->selected(customers,customerData,this::customerDialog)),
                Ui.button("Excluir",()->selected(customers,customerData,c->{
                    if (Ui.confirm(this,"Excluir o cliente " + c.name() + "?")) mutate(()->{service.deleteCustomer(c.id());return true;});
                }))),"Clientes");
        JButton newProduct=Ui.button("Novo produto",()->productDialog(null)); newProduct.setEnabled(service.isAdmin());
        JButton editProduct=Ui.button("Editar / estoque",()->selected(products,productData,this::productDialog)); editProduct.setEnabled(service.isAdmin());
        JButton deleteProduct=Ui.button("Excluir",()->selected(products,productData,p->{
            if (Ui.confirm(this,"Excluir o produto " + p.name() + "?")) mutate(()->{service.deleteProduct(p.id());return true;});
        })); deleteProduct.setEnabled(service.isAdmin());
        pages.add(page("Produtos e estoque",products,newProduct,editProduct,deleteProduct),"Produtos");
        pages.add(page("Vendas",sales,Ui.button("Nova venda",this::saleDialog),
                Ui.button("Ver itens",()->selected(sales,saleData,this::saleDetails)),
                Ui.button("Cancelar venda",()->selected(sales,saleData,s->{
                    if (Ui.confirm(this,"Cancelar a venda #"+s.id()+" e devolver os itens ao estoque?")) mutate(()->{service.cancelSale(s.id());return true;});
                }))),"Vendas");
        root.add(pages,BorderLayout.CENTER);
        status.setBorder(new EmptyBorder(8,16,8,16)); root.add(status,BorderLayout.SOUTH);
        setContentPane(root);
        if (loadData) SwingUtilities.invokeLater(this::refresh);
    }

    private JPanel sidebar() {
        JPanel side=new JPanel(new BorderLayout(0,25)); side.setPreferredSize(new Dimension(215,0));
        side.setBackground(Ui.INK); side.setBorder(new EmptyBorder(30,18,22,18));
        JPanel top=Ui.column();
        JLabel brand=new JLabel("LOJA TECNOLOGIA"); brand.setForeground(Color.WHITE); brand.setFont(new Font("Segoe UI",Font.BOLD,17));
        top.add(brand); top.add(Box.createVerticalStrut(8));
        JLabel subtitle=new JLabel("Gestão de vendas"); subtitle.setForeground(new Color(175,199,224)); top.add(subtitle);
        top.add(Box.createVerticalStrut(30));
        for (String name : List.of("Início","Clientes","Produtos","Vendas")) {
            JButton b=Ui.button(name,()->cards.show(pages,name)); b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(178,42)); top.add(b); top.add(Box.createVerticalStrut(10));
        }
        side.add(top,BorderLayout.NORTH);
        JPanel bottom=Ui.column();
        JLabel name=new JLabel(service.currentUser().name()); name.setForeground(Color.WHITE);
        JLabel role=new JLabel(service.isAdmin() ? "Administrador" : "Funcionário"); role.setForeground(new Color(175,199,224));
        bottom.add(name); bottom.add(Box.createVerticalStrut(5)); bottom.add(role); bottom.add(Box.createVerticalStrut(15));
        bottom.add(Ui.button("Sair da conta",()->{
            if (!Ui.confirm(this,"Sair da sua conta?")) return;
            service.logout(); new LoginFrame(service,db,true).setVisible(true); dispose();
        }));
        side.add(bottom,BorderLayout.SOUTH); return side;
    }

    private JPanel home() {
        JPanel panel=new JPanel(new BorderLayout(0,22)); panel.setOpaque(false);
        JPanel heading=Ui.column(); heading.add(Ui.title("Visão geral")); heading.add(Box.createVerticalStrut(8));
        heading.add(new JLabel("Acompanhe os cadastros, o estoque e as vendas da loja."));
        panel.add(heading,BorderLayout.NORTH);
        JPanel body=Ui.column();
        JPanel metrics=new JPanel(new GridLayout(2,2,16,16)); metrics.setOpaque(false); metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE,230));
        metrics.add(metric("Clientes cadastrados",customerCount)); metrics.add(metric("Produtos cadastrados",productCount));
        metrics.add(metric("Produtos com estoque até 5",lowStock)); metrics.add(metric("Total de vendas concluídas",revenue));
        body.add(metrics); body.add(Box.createVerticalStrut(24));
        JTextArea help=new JTextArea("COMECE POR AQUI\n\n1. Cadastre os clientes da loja.\n2. Cadastre os produtos e informe o estoque disponível.\n3. Registre uma venda escolhendo cliente, produtos e pagamento.\n\nAs vendas atualizam o estoque automaticamente. Ao cancelar, os itens voltam ao estoque e o histórico é mantido.");
        help.setEditable(false); help.setLineWrap(true); help.setWrapStyleWord(true); help.setOpaque(false);
        help.setFont(new Font("Segoe UI",Font.PLAIN,15)); help.setForeground(Ui.INK);
        body.add(help); body.add(Box.createVerticalGlue()); panel.add(body,BorderLayout.CENTER);
        panel.add(Ui.actions(Ui.button("Atualizar dados",this::refresh)),BorderLayout.SOUTH); return panel;
    }
    private JPanel metric(String title,JLabel number) {
        JPanel card=new JPanel(new BorderLayout(0,12)); card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(224,232,241)),new EmptyBorder(16,18,16,18)));
        card.add(new JLabel(title),BorderLayout.NORTH);
        number.setFont(new Font("Segoe UI",Font.BOLD,29)); number.setForeground(Ui.BLUE); card.add(number,BorderLayout.CENTER); return card;
    }
    private JPanel page(String title,JTable table,JButton... buttons) {
        JPanel panel=new JPanel(new BorderLayout(0,16)); panel.setOpaque(false);
        JPanel top=new JPanel(new BorderLayout()); top.setOpaque(false); top.add(Ui.title(title),BorderLayout.NORTH);
        JPanel actions=Ui.actions(buttons); actions.add(Ui.button("Atualizar",this::refresh));
        actions.add(Ui.button("Exportar CSV",()->Ui.export(this,table,title.toLowerCase(Locale.ROOT).replace(' ','-'))));
        top.add(actions,BorderLayout.SOUTH); panel.add(top,BorderLayout.NORTH); panel.add(Ui.searchable(table),BorderLayout.CENTER); return panel;
    }
    private record Snapshot(List<Customer> customers,List<Product> products,List<Sale> sales,Summary summary) {}
    private void refresh() {
        Ui.run(this,()->new Snapshot(service.customers(),service.products(),service.sales(),service.summary()),data->{
            customerData=data.customers(); productData=data.products(); saleData=data.sales();
            DefaultTableModel cm=(DefaultTableModel)customers.getModel(); cm.setRowCount(0);
            for (Customer c:customerData) cm.addRow(new Object[]{c.id(),c.name(),c.cpf(),c.phone(),c.email(),c.address()});
            DefaultTableModel pm=(DefaultTableModel)products.getModel(); pm.setRowCount(0);
            for (Product p:productData) pm.addRow(new Object[]{p.id(),p.name(),p.category(),p.brand(),p.stock(),p.price()});
            DefaultTableModel sm=(DefaultTableModel)sales.getModel(); sm.setRowCount(0);
            for (Sale s:saleData) sm.addRow(new Object[]{s.id(),s.customer(),s.date().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),s.payment(),s.status(),s.total()});
            Summary sum=data.summary(); customerCount.setText(""+sum.customers()); productCount.setText(""+sum.products());
            lowStock.setText(""+sum.lowStock()); revenue.setText(Ui.money(sum.revenue()));
            status.setText((db.isLocal() ? "Banco local" : "Banco configurado") + "  •  Atualizado às " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
    }
    private <T> void selected(JTable table,List<T> data,java.util.function.Consumer<T> action) {
        int row=table.getSelectedRow();
        if (row<0) { Ui.info(this,"Selecione um registro na lista."); return; }
        action.accept(data.get(table.convertRowIndexToModel(row)));
    }
    private void mutate(Ui.Job<?> work) { Ui.run(this,work,ignored->refresh()); }

    private JDialog dialog(String title,JPanel body) {
        JDialog dialog=new JDialog(this,title,true); dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        body.setBorder(new EmptyBorder(20,24,20,24)); dialog.setContentPane(body); return dialog;
    }
    private void show(JDialog dialog,int width,int height) { dialog.setSize(width,height); dialog.setLocationRelativeTo(this); dialog.setVisible(true); }

    private void customerDialog(Customer customer) {
        JPanel body=new JPanel(new BorderLayout(0,12)); JPanel form=Ui.form();
        JTextField name=new JTextField(customer==null?"":customer.name()), cpf=new JTextField(customer==null?"":customer.cpf()),
                phone=new JTextField(customer==null?"":customer.phone()), email=new JTextField(customer==null?"":customer.email()),
                address=new JTextField(customer==null?"":customer.address());
        Ui.field(form,0,"Nome *",name); Ui.field(form,1,"CPF *",cpf); Ui.field(form,2,"Telefone",phone);
        Ui.field(form,3,"E-mail",email); Ui.field(form,4,"Endereço",address);
        body.add(form,BorderLayout.CENTER); JDialog dialog=dialog(customer==null?"Novo cliente":"Editar cliente",body);
        body.add(Ui.actions(Ui.button("Salvar",()->{
            Customer value=new Customer(customer==null?0:customer.id(),name.getText(),cpf.getText(),phone.getText(),email.getText(),address.getText());
            Ui.run(dialog,()->service.saveCustomer(value),id->{dialog.dispose();refresh();});
        }),Ui.button("Cancelar",dialog::dispose)),BorderLayout.SOUTH);
        show(dialog,480,630);
    }
    private void productDialog(Product product) {
        JPanel body=new JPanel(new BorderLayout(0,12)); JPanel form=Ui.form();
        JTextField name=new JTextField(product==null?"":product.name()), category=new JTextField(product==null?"":product.category()),
                brand=new JTextField(product==null?"":product.brand()), price=new JTextField(product==null?"":product.price().toPlainString().replace('.',','));
        JSpinner stock=new JSpinner(new SpinnerNumberModel(product==null?0:product.stock(),0,Integer.MAX_VALUE,1));
        Ui.field(form,0,"Nome do produto *",name); Ui.field(form,1,"Categoria",category); Ui.field(form,2,"Marca",brand);
        Ui.field(form,3,"Quantidade em estoque *",stock); Ui.field(form,4,"Preço em reais * (ex.: 1250,90)",price);
        body.add(form,BorderLayout.CENTER); JDialog dialog=dialog(product==null?"Novo produto":"Editar produto / estoque",body);
        body.add(Ui.actions(Ui.button("Salvar",()->{
            try {
                stock.commitEdit();
                String input=price.getText().trim();
                if (!input.matches("\\d+([,.]\\d{1,2})?")) throw new IllegalArgumentException("Informe o preço sem separador de milhar. Exemplo: 1250,90.");
                Product value=new Product(product==null?0:product.id(),name.getText(),category.getText(),brand.getText(),(Integer)stock.getValue(),new BigDecimal(input.replace(',','.')));
                Ui.run(dialog,()->service.saveProduct(value),id->{dialog.dispose();refresh();});
            } catch (java.text.ParseException e) { Ui.error(dialog,new IllegalArgumentException("Informe uma quantidade inteira válida.")); }
            catch (IllegalArgumentException e) { Ui.error(dialog,e); }
        }),Ui.button("Cancelar",dialog::dispose)),BorderLayout.SOUTH);
        show(dialog,480,630);
    }

    private void saleDialog() {
        if (customerData.isEmpty() || productData.isEmpty()) { Ui.info(this,"Cadastre ao menos um cliente e um produto antes de registrar uma venda."); return; }
        JPanel body=new JPanel(new BorderLayout(0,14)); JDialog dialog=dialog("Nova venda",body);
        JComboBox<Customer> customer=new JComboBox<>(customerData.toArray(Customer[]::new));
        JComboBox<String> payment=new JComboBox<>(new String[]{"PIX","CARTAO","DINHEIRO"});
        JComboBox<Product> product=new JComboBox<>(productData.toArray(Product[]::new));
        JSpinner quantity=new JSpinner(new SpinnerNumberModel(1,1,Integer.MAX_VALUE,1));
        JPanel fields=Ui.form(); Ui.field(fields,0,"Cliente",customer); Ui.field(fields,1,"Forma de pagamento",payment);
        JPanel selection=new JPanel(new BorderLayout(8,0)); selection.setOpaque(false);
        selection.add(product,BorderLayout.CENTER); quantity.setPreferredSize(new Dimension(85,36)); selection.add(quantity,BorderLayout.EAST);
        Ui.field(fields,2,"Produto e quantidade",selection); body.add(fields,BorderLayout.NORTH);
        Map<Integer,Integer> cart=new LinkedHashMap<>(); Map<Integer,Product> chosen=new HashMap<>();
        JTable items=Ui.table("Produto","Quantidade","Valor unitário","Subtotal"); JLabel total=new JLabel("Total: R$ 0,00");
        total.setFont(new Font("Segoe UI",Font.BOLD,20));
        Runnable render=()-> {
            DefaultTableModel model=(DefaultTableModel)items.getModel(); model.setRowCount(0); BigDecimal sum=BigDecimal.ZERO;
            for (Map.Entry<Integer,Integer> entry:cart.entrySet()) {
                Product p=chosen.get(entry.getKey()); BigDecimal subtotal=p.price().multiply(BigDecimal.valueOf(entry.getValue()));
                model.addRow(new Object[]{p.name(),entry.getValue(),p.price(),subtotal}); sum=sum.add(subtotal);
            }
            total.setText("Total: "+Ui.money(sum));
        };
        JPanel center=new JPanel(new BorderLayout(0,8)); center.setOpaque(false);
        center.add(Ui.actions(Ui.button("Adicionar produto",()-> {
            try {
                quantity.commitEdit(); Product p=(Product)product.getSelectedItem(); if (p==null) return;
                int amount=Math.addExact(cart.getOrDefault(p.id(),0),(Integer)quantity.getValue());
                if (amount>p.stock()) throw new IllegalArgumentException("Estoque disponível: "+p.stock()+" unidade(s).");
                cart.put(p.id(),amount); chosen.put(p.id(),p); render.run();
            } catch (java.text.ParseException | ArithmeticException e) { Ui.error(dialog,new IllegalArgumentException("Informe uma quantidade inteira válida.")); }
            catch (IllegalArgumentException e) { Ui.error(dialog,e); }
        }), Ui.button("Remover item",()->{
            int row=items.getSelectedRow(); if(row<0) { Ui.info(dialog,"Selecione um item.");return; }
            int modelRow=items.convertRowIndexToModel(row); int key=new ArrayList<>(cart.keySet()).get(modelRow);
            cart.remove(key); chosen.remove(key);render.run();
        })),BorderLayout.NORTH);
        center.add(new JScrollPane(items),BorderLayout.CENTER); center.add(total,BorderLayout.SOUTH); body.add(center,BorderLayout.CENTER);
        body.add(Ui.actions(Ui.button("Finalizar venda",()->{
            Customer c=(Customer)customer.getSelectedItem(); if(c==null)return;
            if(cart.isEmpty()) { Ui.info(dialog,"Adicione pelo menos um produto.");return; }
            if(!Ui.confirm(dialog,"Confirmar a venda? "+total.getText()))return;
            Map<Integer,Integer> quantities=new LinkedHashMap<>(cart); Map<Integer,BigDecimal> prices=new HashMap<>();
            chosen.forEach((id,p)->prices.put(id,p.price())); String pay=(String)payment.getSelectedItem();
            Ui.run(dialog,()->service.createSale(c.id(),pay,quantities,prices),id->{dialog.dispose();Ui.info(this,"Venda #"+id+" registrada com sucesso.");refresh();});
        }), Ui.button("Fechar",()->{
            if(cart.isEmpty() || Ui.confirm(dialog,"Descartar os itens desta venda?"))dialog.dispose();
        })),BorderLayout.SOUTH);
        // A cruz também pede confirmação caso o carrinho já tenha itens.
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent event) {
                if(cart.isEmpty() || Ui.confirm(dialog,"Descartar os itens desta venda?"))dialog.dispose();
            }
        });
        show(dialog,790,730);
    }
    private void saleDetails(Sale sale) {
        Ui.run(this,()->service.saleItems(sale.id()),items->{
            JPanel body=new JPanel(new BorderLayout(0,14)); JTable table=Ui.table("Produto","Quantidade","Valor unitário","Subtotal");
            DefaultTableModel model=(DefaultTableModel)table.getModel();
            for(SaleItem item:items)model.addRow(new Object[]{item.product(),item.quantity(),item.price(),item.total()});
            body.add(new JLabel("Venda #"+sale.id()+" • "+sale.customer()+" • "+sale.status()+" • "+Ui.money(sale.total())),BorderLayout.NORTH);
            body.add(new JScrollPane(table),BorderLayout.CENTER); JDialog dialog=dialog("Itens da venda",body);
            body.add(Ui.actions(Ui.button("Exportar CSV",()->Ui.export(dialog,table,"venda-"+sale.id())),Ui.button("Fechar",dialog::dispose)),BorderLayout.SOUTH);
            show(dialog,790,450);
        });
    }
}
