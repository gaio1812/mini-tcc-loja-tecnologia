package br.com.lojatecnologia;

import javax.swing.SwingUtilities;

public final class Main {
    private Main() {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Ui.setup();
            Ui.run(null,()-> {
                Database db=Database.configured(); db.initialize();
                StoreService service=new StoreService(db);
                return new Object[]{db,service,service.hasUsers()};
            },result->new LoginFrame((StoreService)result[1],(Database)result[0],(Boolean)result[2]).setVisible(true));
        });
    }
}
