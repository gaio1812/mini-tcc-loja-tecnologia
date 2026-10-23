import javax.swing.*;
import java.awt.*;

public class TelaLogin extends JFrame {

    private JTextField campoEmail;
    private JPasswordField campoSenha;

    public TelaLogin() {

        // Configurações da janela
        setTitle("Sistema de Vendas - Login");
        setSize(350, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Painel principal
        JPanel painel = new JPanel();
        painel.setLayout(null);
        painel.setBackground(Color.WHITE);

        // Título
        JLabel titulo = new JLabel("LOGIN");
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        titulo.setBounds(125, 15, 100, 30);
        painel.add(titulo);

        // E-mail
        JLabel labelEmail = new JLabel("E-mail:");
        labelEmail.setBounds(30, 65, 100, 25);
        painel.add(labelEmail);

        campoEmail = new JTextField();
        campoEmail.setBounds(30, 90, 275, 30);
        painel.add(campoEmail);

        // Senha
        JLabel labelSenha = new JLabel("Senha:");
        labelSenha.setBounds(30, 125, 100, 25);
        painel.add(labelSenha);

        campoSenha = new JPasswordField();
        campoSenha.setBounds(30, 150, 275, 30);
        painel.add(campoSenha);

        // Botão entrar
        JButton botaoEntrar = new JButton("Entrar");
        botaoEntrar.setBounds(30, 200, 130, 30);
        painel.add(botaoEntrar);

        // Botão cadastro
        JButton botaoCadastro = new JButton("Cadastrar");
        botaoCadastro.setBounds(175, 200, 130, 30);
        painel.add(botaoCadastro);

        // Ação do botão entrar
        botaoEntrar.addActionListener(e -> {

            String email = campoEmail.getText();
            String senha = new String(campoSenha.getPassword());

            if (email.isEmpty() || senha.isEmpty()) {

                JOptionPane.showMessageDialog(
                    this,
                    "Preencha todos os campos!"
                );

            } else {

                JOptionPane.showMessageDialog(
                    this,
                    "Login ainda não conectado ao banco de dados."
                );

            }
        });

        // Ação do botão cadastro
        botaoCadastro.addActionListener(e -> {

            new TelaCadastro().setVisible(true);
            dispose();

        });

        add(painel);
    }
}