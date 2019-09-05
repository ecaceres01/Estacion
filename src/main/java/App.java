import ClientManager.*;

import org.jivesoftware.smack.roster.Roster;

import java.io.File;

public class App {
    private static File file = new File(System.getProperty("user.dir") + "/config.properties");
    private static XmppClient xmppClient = null;
    private static PropertiesManager propertiesManager = null;
    static boolean checkOwner = false;


    public static void main(String[] args) {
        String domain = "10.40.5.9";
        String user = "unab";
        String password = "unab";

        if (!file.exists()) {
            try {
                xmppClient = new XmppClient(domain, user, password);
                propertiesManager = new PropertiesManager(domain, user, password);
                xmppClient.InitialConnection();
            } catch (Exception e) {
                file.delete();
                System.out.println("ERROR: No se concretar la conexión inicial al servidor XMPP");
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
                            propertiesManager.getUser()
                    );
                    xmppClient.NormalConnection();
                }
            } catch (Exception e) {
                System.out.println("ERROR: No se logró realizar la conexión al servidor");
                e.printStackTrace();
                System.exit(-1);
            }
        }

        try {
            checkOwner = propertiesManager.checkOwner();

            if (checkOwner) {
                xmppClient.getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);
                System.out.println("INFO: Subcription mode MANUAL");
            } else {
                xmppClient.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                System.out.println("INFO: Subcription mode ACCEPT ALL");
            }
        } catch (Exception e) {
            System.out.println("ERROR: No se logró realizar el chechkeo del owner");
            e.printStackTrace();
            System.exit(-1);
        }

        new ListenerManager(xmppClient, propertiesManager);

        try {
            xmppClient.setRosterListener();
        } catch (Exception e) {
            System.out.println("ERROR: Rosterlistener no implementado");
            e.printStackTrace();
        }
        xmppClient.setIncomingChatMessageListener();
        xmppClient.setSubscribeListener();

    }
}
