import javax.swing.*;
import java.awt.*;

public class TelaCadastro extends JFrame {

    private JTextField campoNome;
    private JTextField campoEmail;
    private JPasswordField campoSenha;
    private JPasswordField campoConfirmarSenha;

    public TelaCadastro() {

        // Configuração da janela
        setTitle("Sistema de Vendas - Cadastro");
        setSize(380, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Painel principal
        JPanel painel = new JPanel();
        painel.setLayout(null);
        painel.setBackground(Color.WHITE);

        // Título
        JLabel titulo = new JLabel("CADASTRO");
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        titulo.setBounds(115, 15, 150, 30);
        painel.add(titulo);

        // Nome
        JLabel labelNome = new JLabel("Nome completo:");
        labelNome.setBounds(30, 55, 150, 25);
        painel.add(labelNome);

        campoNome = new JTextField();
        campoNome.setBounds(30, 80, 305, 30);
        painel.add(campoNome);

        // E-mail
        JLabel labelEmail = new JLabel("E-mail:");
        labelEmail.setBounds(30, 115, 100, 25);
        painel.add(labelEmail);

        campoEmail = new JTextField();
        campoEmail.setBounds(30, 140, 305, 30);
        painel.add(campoEmail);

        // Senha
        JLabel labelSenha = new JLabel("Senha:");
        labelSenha.setBounds(30, 175, 100, 25);
        painel.add(labelSenha);

        campoSenha = new JPasswordField();
        campoSenha.setBounds(30, 200, 305, 30);
        painel.add(campoSenha);

        // Confirmar senha
        JLabel labelConfirmar = new JLabel("Confirmar senha:");
        labelConfirmar.setBounds(30, 235, 150, 25);
        painel.add(labelConfirmar);

        campoConfirmarSenha = new JPasswordField();
        campoConfirmarSenha.setBounds(30, 260, 305, 30);
        painel.add(campoConfirmarSenha);

        // Botão cadastrar
        JButton botaoCadastrar = new JButton("Cadastrar");
        botaoCadastrar.setBounds(30, 320, 145, 35);
        painel.add(botaoCadastrar);

        // Botão voltar
        JButton botaoVoltar = new JButton("Voltar ao login");
        botaoVoltar.setBounds(190, 320, 145, 35);
        painel.add(botaoVoltar);

        // Evento do botão cadastrar
        botaoCadastrar.addActionListener(e -> cadastrar());

        // Evento do botão voltar
        botaoVoltar.addActionListener(e -> {
            new TelaLogin().setVisible(true);
            dispose();
        });

        add(painel);
    }

    private void cadastrar() {

        String nome = campoNome.getText().trim();
        String email = campoEmail.getText().trim();

        char[] senha = campoSenha.getPassword();
        char[] confirmarSenha = campoConfirmarSenha.getPassword();

        if (nome.isEmpty() || email.isEmpty()
                || senha.length == 0
                || confirmarSenha.length == 0) {

            JOptionPane.showMessageDialog(
                this,
                "Preencha todos os campos!"
            );

            return;
        }

        if (!email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            JOptionPane.showMessageDialog(
                this,
                "Digite um e-mail válido!"
            );

            return;
        }

        if (!java.util.Arrays.equals(senha, confirmarSenha)) {

            JOptionPane.showMessageDialog(
                this,
                "As senhas não coincidem!"
            );

            return;
        }

        JOptionPane.showMessageDialog(
            this,
            "Cadastro validado com sucesso!\n"
            + "A conexão com o banco será implementada depois."
        );
    }
}
