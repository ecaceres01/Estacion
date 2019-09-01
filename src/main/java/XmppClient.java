import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.IOException;

class XmppClient {
    private AbstractXMPPConnection connection;
    private XMPPTCPConnectionConfiguration configuration;
    private String domain, user, password;
    private Roster roster;
    private ChatManager chatManager;
    private PingManager pingManager;

    XmppClient(String domain, String user, String password) throws IOException, InterruptedException, XMPPException, SmackException {
        this.domain = domain;
        this.user = user;
        this.password = password;

        configuration = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(domain)
                .setHost(domain)
                .enableDefaultDebugger()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();

        connection = new XMPPTCPConnection(configuration);

        connection.connect();
        System.out.println("INFO: Conectandose a " + connection.getXMPPServiceDomain().toString());

    }

    ConnectionListener connectionListener(boolean checkOwner) {

        return new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection1) {
                logInAccount("test");
            }

            @Override
            public void authenticated(XMPPConnection connection1, boolean resumed) {
                manager();
                if (checkOwner){
                    roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
                } else {
                    roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                }
            }

            @Override
            public void connectionClosed() {

            }

            @Override
            public void connectionClosedOnError(Exception e) {

            }
        };
    }

    void generateAccount() throws XmppStringprepException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.sensitiveOperationOverInsecureConnection(true);
        accountManager.createAccount(Localpart.from(user), password);

    }

    private void logInAccount(String resource){
        try {
            connection.login(this.user, this.password, Resourcepart.from(resource));
        } catch (Exception e) {
            System.out.println("ERROR: No se pudo realizar el LogIn a la cuenta");
            e.printStackTrace();
        }
        System.out.println("INFO: Logeando en el servidor " + connection.getXMPPServiceDomain().toString() + " como: " + connection.getUser());
    }

    private void manager(){
        this.roster = Roster.getInstanceFor(connection);
        this.chatManager = ChatManager.getInstanceFor(connection);
        this.pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(300);
        System.out.println("INFO: La cuenta se encuentra autentificada");
    }

}
