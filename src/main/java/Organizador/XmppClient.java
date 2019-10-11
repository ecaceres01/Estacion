package Organizador;

import Organizador.Excepciones.ChatException;
import Organizador.Excepciones.RosterException;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

public class XmppClient {
    private String domain, user, password;
    private AbstractXMPPConnection connection;
    private XMPPTCPConnectionConfiguration configuration;
    private Roster roster;
    private ChatManager chatManager;
    private PingManager pingManager;
    private ReconnectionManager reconnectionManager;

    public XmppClient(String domain, String user, String password) throws IOException, InterruptedException, XMPPException, SmackException {
        this.domain = domain;
        this.user = user;
        this.password = password;

        configuration = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(domain)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                //.enableDefaultDebugger()
                .build();

        connection = new XMPPTCPConnection(configuration);
        connection.connect();

        System.out.println("INFO: Conectanse a " + connection.getXMPPServiceDomain().toString());
    }

    public void initialConnection() throws XMPPException, SmackException, InterruptedException, IOException {
        if (connection.isConnected()) {
            generateAccount();
            makeConnection();
        }
    }

    public void normalConnection() throws InterruptedException, XMPPException, SmackException, IOException {
        if (connection.isConnected()) {
            makeConnection();
        }
    }

    private void makeConnection() throws IOException, InterruptedException, SmackException, XMPPException {
        connection.login(user, password, Resourcepart.from("test"));
        setRoster();
        setChatManager();
        setPingManager();
        System.out.println("INFO: Ingresando a servidor como usuario " + connection.getUser());
    }

    private void generateAccount() throws XmppStringprepException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.sensitiveOperationOverInsecureConnection(true);
        accountManager.createAccount(Localpart.from(user), password);
        System.out.println("INFO: Generando cuenta en el servidor...");
    }

    public XMPPTCPConnection getConnection() {
        return (XMPPTCPConnection) connection;
    }

    private void setPingManager() {
        this.pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(60);
    }

    public Roster getRoster() throws RosterException {
        if (roster == null) {
            throw new RosterException("Problemas con Roster");
        }
        return roster;
    }

    private void setRoster() {
        this.roster = Roster.getInstanceFor(connection);
    }

    void subscribeOwner(BareJid owner) throws SmackException.NotLoggedInException, InterruptedException, SmackException.NotConnectedException {
        roster.sendSubscriptionRequest(owner);
        System.out.println("INFO: Respondiendo solicitud a owner...");
        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
    }

    void subcribeRequest(BareJid jid) throws SmackException.NotLoggedInException, InterruptedException, SmackException.NotConnectedException {
        roster.sendSubscriptionRequest(jid);
    }

    private void setChatManager() {
        this.chatManager = ChatManager.getInstanceFor(connection);
    }

    ChatManager getChatManager() throws ChatException {
        if (chatManager == null) {
            throw new ChatException("Problemas con Chat");
        }
        return chatManager;
    }

    public void setRosterListener() throws IOException {
        roster.addRosterListener(ListenerManager.rosterListener());
    }

    public void setIncomingChatMessageListener() {
        chatManager.addIncomingListener(ListenerManager.incomingChatMessageListener());
    }

    public void setSubscribeListener() {
        roster.addSubscribeListener(ListenerManager.subscribeListener());
    }

    public void setConnectionListener() {
        connection.addConnectionListener(ListenerManager.connectionListener());
    }

    public void setPingFailedListener() {
        pingManager.registerPingFailedListener(ListenerManager.pingFailedListener());
    }

}
