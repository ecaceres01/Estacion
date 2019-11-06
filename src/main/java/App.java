import Organizador.Excepciones.RosterException;
import Organizador.ListenerManager;
import Organizador.PropertiesManager;
import Organizador.XmppClient;
import org.jivesoftware.smack.roster.Roster;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class App {
    private static File file = new File(System.getProperty("user.dir") + "/config.properties");
    private static XmppClient xmppClient = null;
    private static PropertiesManager propertiesManager = null;
    private static boolean checkOwner;

    public static void main(String[] args) {
        String domain = "binarylamp.cl";
        String user = "unab2019";
        String password = UUID.randomUUID().toString().replace("-", "");

        if (!file.exists()) {
            try {
                xmppClient = new XmppClient(domain, user, password);
                propertiesManager = new PropertiesManager(domain, user, password);

                xmppClient.initialConnection();
            } catch (Exception e) {
                System.out.println("ERROR: No se pudo realizar la conexión con el servidor");
                file.delete();
                System.out.println("Eliminando archivo de configuración");
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            try {
                if (xmppClient == null) {
                    propertiesManager = new PropertiesManager();
                    xmppClient = new XmppClient(
                            propertiesManager.getDomain(),
                            propertiesManager.getUser(),
                            propertiesManager.getPassword()
                    );

                    xmppClient.normalConnection();
                }
            } catch (Exception e) {
                System.out.println("ERROR: No se pudo realizar la conexión con el servidor");
                e.printStackTrace();
                System.exit(-1);
            }
        }

        try {
            checkOwner = propertiesManager.checkOwner();

            if (!checkOwner) {
                xmppClient.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                System.out.println("INFO: Owner no seteado. Subcription mode ACCEPT ALL");
            } else {
                xmppClient.getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);
                xmppClient.setSubscribeListener();
                System.out.println("INFO: Owner asignado. Subcription mode MANUAL");
            }
        } catch (RosterException | IOException e) {
            System.out.println("ERROR: Roster no creado");
            System.exit(-2);
            e.printStackTrace();
        }

        new ListenerManager(xmppClient, propertiesManager);

        try {
            xmppClient.setRosterListener();
            xmppClient.setIncomingChatMessageListener();
            xmppClient.setConnectionListener();
            xmppClient.setPingFailedListener();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {

        }

    }
}
