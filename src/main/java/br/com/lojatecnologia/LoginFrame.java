package br.com.lojatecnologia;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

final class LoginFrame extends JFrame {
    private final StoreService service;
    private final Database db;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JTextField loginEmail = new JTextField();
    private final JPasswordField loginPassword = new JPasswordField();

    LoginFrame(StoreService service, Database db, boolean hasUsers) {
        this.service=service; this.db=db;
        setTitle("Loja Tecnologia • Acesso"); setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(550,700); setMinimumSize(new Dimension(500,680)); setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(0,16)); root.setBackground(Ui.BG); root.setBorder(new EmptyBorder(26,45,22,45));
        JPanel brand = Ui.column();
        JLabel logo = new JLabel("LOJA / TECNOLOGIA"); logo.setForeground(Ui.BLUE); logo.setFont(new Font("Segoe UI",Font.BOLD,15));
        brand.add(logo); brand.add(Box.createVerticalStrut(12)); brand.add(Ui.title("Seu negócio, organizado."));
        root.add(brand,BorderLayout.NORTH);
        content.setOpaque(false); content.add(loginPanel(),"login"); content.add(registerPanel(hasUsers),"register");
        root.add(content,BorderLayout.CENTER);
        JLabel footer=new JLabel(db.isLocal() ? "Seus dados ficam salvos neste computador." : "Conectado ao banco configurado.");
        footer.setForeground(Color.DARK_GRAY); root.add(footer,BorderLayout.SOUTH);
        setContentPane(root); cards.show(content,hasUsers ? "login" : "register");
    }

    private JPanel loginPanel() {
        JPanel panel = new JPanel(new BorderLayout(0,12)); panel.setOpaque(false);
        JPanel fields = Ui.form();
        Ui.field(fields,0,"E-mail",loginEmail); Ui.field(fields,1,"Senha",loginPassword);
        JPanel body = Ui.column(); body.add(Ui.title("Entrar")); body.add(Box.createVerticalStrut(20)); body.add(fields);
        JPanel actions = Ui.actions(Ui.button("Entrar",this::login),Ui.button("Criar conta",() -> cards.show(content,"register")));
        body.add(actions); body.add(Box.createVerticalGlue()); panel.add(body,BorderLayout.NORTH);
        loginPassword.addActionListener(e->login()); return panel;
    }
    private void login() {
        String email=loginEmail.getText(); char[] password=loginPassword.getPassword();
        Ui.run(this,()-> { try { return service.login(email,password); } finally { Arrays.fill(password,'\0'); } },user-> {
            loginPassword.setText(""); new MainFrame(service,db).setVisible(true); dispose();
        });
    }
    private JPanel registerPanel(boolean hasUsers) {
        JPanel panel = new JPanel(new BorderLayout(0,8)); panel.setOpaque(false);
        JPanel heading=Ui.column(); heading.add(Ui.title("Criar conta")); heading.add(Box.createVerticalStrut(8));
        JLabel accountHint=new JLabel(hasUsers ? "Novas contas têm o perfil de funcionário." : "A primeira conta será a administradora da loja.");
        heading.add(accountHint);
        panel.add(heading,BorderLayout.NORTH);
        JTextField name=new JTextField(), email=new JTextField(); JPasswordField password=new JPasswordField(), confirm=new JPasswordField();
        JPanel form=Ui.form();
        Ui.field(form,0,"Nome completo",name); Ui.field(form,1,"E-mail",email);
        Ui.field(form,2,"Senha (8 a 128 caracteres)",password); Ui.field(form,3,"Confirmar senha",confirm);
        panel.add(form,BorderLayout.CENTER);
        panel.add(Ui.actions(Ui.button("Cadastrar",()-> {
            String n=name.getText(), em=email.getText(); char[] pw=password.getPassword(), cf=confirm.getPassword();
            Ui.run(this,()-> {
                try { service.register(n,em,pw,cf); return true; }
                finally { Arrays.fill(pw,'\0'); Arrays.fill(cf,'\0'); }
            },ok->{
                name.setText(""); email.setText(""); password.setText(""); confirm.setText("");
                loginEmail.setText(em.trim()); cards.show(content,"login");
                accountHint.setText("Novas contas têm o perfil de funcionário.");
                Ui.info(this,"Conta criada. Entre com seu e-mail e senha.");
            });
        }), Ui.button("Voltar ao login",()->cards.show(content,"login"))),BorderLayout.SOUTH);
        return panel;
    }
}
