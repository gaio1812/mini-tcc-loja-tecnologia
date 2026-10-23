package br.com.lojatecnologia;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Renderiza apenas os componentes do projeto, sem capturar a tela do computador. */
public final class LayoutPreview {
    public static void main(String[] args) throws Exception {
        Database db=new Database("jdbc:h2:mem:layout;MODE=MySQL;DB_CLOSE_DELAY=-1","sa","");db.initialize();
        StoreService service=new StoreService(db);char[] password="senha-de-layout".toCharArray();
        service.register("Administrador","layout@loja.test",password,password);service.login("layout@loja.test",password);
        Files.createDirectories(Path.of("build/preview"));
        SwingUtilities.invokeAndWait(()->{
            Ui.setup();
            try {
                render(new LoginFrame(service,db,false),"cadastro");
                render(new LoginFrame(service,db,true),"login");
                MainFrame main=new MainFrame(service,db,false);render(main,"painel");
            } catch(Exception e) {throw new RuntimeException(e);}
        });
        System.out.println("OK: telas renderizadas em build/preview sem abrir janelas.");
    }
    private static void render(JFrame frame,String name) throws Exception {
        try {
            Container content=frame.getContentPane();content.setSize(frame.getWidth()-16,frame.getHeight()-39);layout(content);
            BufferedImage image=new BufferedImage(content.getWidth(),content.getHeight(),BufferedImage.TYPE_INT_RGB);
            Graphics2D g=image.createGraphics();content.printAll(g);g.dispose();
            ImageIO.write(image,"png",Path.of("build/preview/"+name+".png").toFile());
        } finally {frame.dispose();}
    }
    private static void layout(Container container) {
        container.doLayout();for(Component c:container.getComponents())if(c instanceof Container nested)layout(nested);
    }
}
