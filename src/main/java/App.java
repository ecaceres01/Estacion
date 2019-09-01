import org.jivesoftware.smack.ConnectionListener;
import java.io.File;
import java.io.IOException;

public class App {

    private static File file = new File(System.getProperty("user.dir") + "/config.properties"); //instancia de archivo de propiedades.
    private static XmppClient xmppClient = null; //instancia de la clase de métodos XMPP.
    private static PropertiesManager propertiesManager = null; //instancia de la clase de manejo de propiedades.
    private static boolean checkOwner = false;

    public static void main(String[] args) {
        //datos en duro para la conexión al servidor XMPP
        String domain = "10.40.5.9";
        String user = "estacion";
        String password = "estacion";

        if (!file.exists()){
            try {
                xmppClient = new XmppClient(domain, user, password);
                propertiesManager = new PropertiesManager(domain, user, password);
            } catch (Exception e) {
                file.delete();
                System.out.println("ERROR: No se pudo generar conexión XMPP por primera vez");
                e.printStackTrace();
                System.exit(-1);
            }

            try {
                xmppClient.generateAccount();
            } catch (Exception e) {
                file.delete();
                System.out.println("ERROR: No se pudo generar la cuenta en el servidor");
                e.printStackTrace();
                System.exit(-1);
            }

        } else {
            try {
                propertiesManager = new PropertiesManager();
            } catch (IOException e) {
                System.out.println("ERROR: Error al cargar archivo de configuración");
                e.printStackTrace();
                System.exit(-1);
            }

            try {
                xmppClient = new XmppClient(
                        propertiesManager.getDomain(),
                        propertiesManager.getUser(),
                        propertiesManager.getPassword()
                );
            } catch (Exception e) {
                System.out.println("ERROR: No se pudo conectar a al servidor XMPP");
                e.printStackTrace();
                System.exit(-1);
            }
        }

        checkOwner = propertiesManager.checkOwner();
        xmppClient.connectionListener(checkOwner);



    }
}
