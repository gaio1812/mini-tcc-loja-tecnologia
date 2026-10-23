package br.com.lojatecnologia;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;

final class Ui {
    static final Color INK = new Color(23, 40, 60);
    static final Color BLUE = new Color(24, 94, 170);
    static final Color BG = new Color(244, 247, 251);
    static final Locale PT = Locale.forLanguageTag("pt-BR");
    private static final Logger LOG = Logger.getLogger("loja");
    private Ui() {}

    static void setup() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Nimbus")) { UIManager.setLookAndFeel(info.getClassName()); break; }
            }
        } catch (Exception ignored) { /* Mantém a aparência padrão do Swing. */ }
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("nimbusBase", BLUE);
        UIManager.put("control", BG);
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        try {
            Files.createDirectories(Path.of("logs"));
            FileHandler file = new FileHandler("logs/aplicativo-%g.log", 500_000, 3, true);
            file.setFormatter(new SimpleFormatter());
            LOG.addHandler(file);
            LOG.setUseParentHandlers(false);
        } catch (IOException | SecurityException ignored) { /* Log não impede a abertura. */ }
    }

    static JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.addActionListener(e -> action.run());
        return b;
    }
    static JLabel title(String text) {
        JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.BOLD, 26)); l.setForeground(INK); return l;
    }
    static JPanel column() { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false); return p; }
    static JPanel form() { JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false); return p; }
    static void field(JPanel form, int row, String text, JComponent input) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx=0; g.gridy=row*2; g.weightx=1; g.fill=GridBagConstraints.HORIZONTAL;
        g.insets=new Insets(7,0,3,0);
        JLabel label = new JLabel(text); label.setLabelFor(input); form.add(label,g);
        g.gridy++; g.insets=new Insets(0,0,5,0); input.setPreferredSize(new Dimension(320,36)); form.add(input,g);
    }
    static JPanel actions(JComponent... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); p.setOpaque(false);
        for (JComponent b : buttons) p.add(b); return p;
    }
    static String money(java.math.BigDecimal value) { return NumberFormat.getCurrencyInstance(PT).format(value); }
    static JTable table(String... columns) {
        DefaultTableModel model = new DefaultTableModel(columns,0) {
            @Override public boolean isCellEditable(int row,int column) { return false; }
            @Override public Class<?> getColumnClass(int column) {
                return getRowCount() == 0 || getValueAt(0,column) == null ? Object.class : getValueAt(0,column).getClass();
            }
        };
        JTable t = new JTable(model);
        t.setRowHeight(34); t.setAutoCreateRowSorter(true); t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true); t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,13));
        t.setDefaultRenderer(java.math.BigDecimal.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int col) {
                super.getTableCellRendererComponent(table,value,selected,focus,row,col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setText(value instanceof java.math.BigDecimal b ? money(b) : ""); return this;
            }
        });
        return t;
    }
    static JPanel searchable(JTable table) {
        JPanel p = new JPanel(new BorderLayout(8,8)); p.setOpaque(false);
        JTextField search = new JTextField(); search.setToolTipText("Pesquisar nesta lista");
        JPanel bar = new JPanel(new BorderLayout(10,0)); bar.setOpaque(false);
        JLabel label = new JLabel("Pesquisar"); label.setLabelFor(search);
        bar.add(label,BorderLayout.WEST); bar.add(search,BorderLayout.CENTER);
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                TableRowSorter<?> sorter = (TableRowSorter<?>) table.getRowSorter();
                sorter.setRowFilter(search.getText().isBlank() ? null : RowFilter.regexFilter("(?iu)" + java.util.regex.Pattern.quote(search.getText())));
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });
        p.add(bar,BorderLayout.NORTH); p.add(new JScrollPane(table),BorderLayout.CENTER); return p;
    }

    @FunctionalInterface interface Job<T> { T run() throws Exception; }
    static <T> void run(Window owner, Job<T> job, Consumer<T> success) {
        JDialog wait = new JDialog(owner,"Aguarde",Dialog.ModalityType.APPLICATION_MODAL);
        wait.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JPanel p = new JPanel(new BorderLayout(12,12)); p.setBorder(new EmptyBorder(22,28,22,28));
        p.add(new JLabel("Processando, aguarde…"),BorderLayout.NORTH);
        JProgressBar progress = new JProgressBar(); progress.setIndeterminate(true); p.add(progress,BorderLayout.CENTER);
        wait.setContentPane(p); wait.pack(); wait.setLocationRelativeTo(owner);
        new SwingWorker<T,Void>() {
            protected T doInBackground() throws Exception { return job.run(); }
            protected void done() {
                wait.dispose();
                try { success.accept(get()); }
                catch (ExecutionException e) { error(owner,e.getCause()); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); error(owner,e); }
                catch (RuntimeException e) { error(owner,e); }
            }
        }.execute();
        wait.setVisible(true);
    }
    static void error(Component owner, Throwable error) {
        String message;
        if (error instanceof IllegalArgumentException || error instanceof IllegalStateException) message = error.getMessage();
        else if (error instanceof SQLException sql) {
            String state = sql.getSQLState() == null ? "" : sql.getSQLState();
            if (state.startsWith("08") || state.startsWith("28")) message = "Não foi possível conectar ao banco. Confira a configuração e se o servidor está disponível.";
            else if (state.equals("40001")) message = "Outra operação alterou os mesmos dados. Atualize a lista e tente novamente.";
            else if (state.equals("90020")) message = "O banco local já está aberto. Feche a outra janela do programa e tente novamente.";
            else message = "Não foi possível concluir a operação no banco. Consulte o registro na pasta logs.";
            LOG.log(Level.SEVERE,"Falha no banco (SQLState=" + state + ")",error);
        } else {
            message = "Não foi possível concluir a operação. Confira o registro na pasta logs.";
            LOG.log(Level.SEVERE,"Falha no aplicativo",error);
        }
        JOptionPane.showMessageDialog(owner,message,"Atenção",JOptionPane.WARNING_MESSAGE);
    }
    static boolean confirm(Component owner,String text) {
        return JOptionPane.showConfirmDialog(owner,text,"Confirmar",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    static void info(Component owner,String text) { JOptionPane.showMessageDialog(owner,text,"TecnoLoja",JOptionPane.INFORMATION_MESSAGE); }

    static void export(Component owner,JTable table,String filename) {
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new File(filename + ".csv"));
        if (chooser.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) return;
        Path target = chooser.getSelectedFile().toPath();
        if (Files.exists(target) && !confirm(owner,"O arquivo já existe. Substituir?")) return;
        try (BufferedWriter out = Files.newBufferedWriter(target,StandardCharsets.UTF_8)) {
            out.write('\ufeff');
            for (int row=-1; row<table.getRowCount();row++) {
                for (int col=0; col<table.getColumnCount();col++) {
                    if (col>0) out.write(';');
                    String value = row<0 ? table.getColumnName(col) : String.valueOf(table.getValueAt(row,col));
                    // Impede que nomes digitados pelo usuário virem fórmulas ao abrir no Excel.
                    if (!value.isEmpty() && "=+-@\t\r\n".indexOf(value.charAt(0))>=0) value="'"+value;
                    out.write('"'); out.write(value.replace("\"","\"\"")); out.write('"');
                }
                out.newLine();
            }
            info(owner,"Arquivo exportado com sucesso.");
        } catch (IOException e) { error(owner,e); }
    }
}
