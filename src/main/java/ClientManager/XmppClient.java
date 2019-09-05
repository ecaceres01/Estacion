package ClientManager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;

import java.io.File;
import java.io.IOException;

import static java.lang.Thread.sleep;

public class XmppClient {
    private AbstractXMPPConnection connection;
    private XMPPTCPConnectionConfiguration configuration;
    private String domain, user, password;
    private Roster roster = null;
    private ChatManager chatManager = null;
    private PingManager pingManager = null;

    public XmppClient(String domain, String user, String password) throws IOException, InterruptedException, XMPPException, SmackException {
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
        System.out.println("INFO: Conectandose a " + this.domain);
    }

    private void makeConnection() throws InterruptedException, XMPPException, SmackException, IOException {
        logInAccount("test");
        setRoster();
        setChatManager();
        setPingManager();
    }

    public void InitialConnection() throws InterruptedException, XMPPException, SmackException, IOException {
        if (connection.isConnected()) {
            generateAccount();
            makeConnection();
        }
    }

    public void NormalConnection() throws InterruptedException, IOException, SmackException, XMPPException {
        if (connection.isConnected()) {
            makeConnection();
        }
    }

    private void setPingManager() {
        this.pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(300);
        System.out.println("INFO: Intervalo del ping de " + (pingManager.getPingInterval())/60 + " minutos");
    }

    public void generateAccount() throws IOException, XMPPException, SmackException, InterruptedException {
        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.sensitiveOperationOverInsecureConnection(true);
        accountManager.createAccount(Localpart.from(user), password);
        System.out.println("INFO: Generando cuenta en el servidor " + connection.getXMPPServiceDomain().toString());
    }

    private void logInAccount(String resource) throws IOException, InterruptedException, SmackException, XMPPException {
        connection.login(user, password, Resourcepart.from(resource));
        System.out.println("INFO: Ingresando a servidor como usuario " + connection.getUser());
    }

    private void setRoster() {
        this.roster = Roster.getInstanceFor(connection);
    }

    public Roster getRoster() throws Exception {
        if (roster == null) {
            throw new Exception("ERROR: No existe roster creado");
        }
        return roster;
    }

    private void setChatManager() {
        this.chatManager = ChatManager.getInstanceFor(connection);
    }

    public void setRosterListener() throws Exception {
        this.roster.addRosterListener(ListenerManager.rosterListener());
    }

    public void setIncomingChatMessageListener() {
        this.chatManager.addIncomingListener(ListenerManager.chatMessageListener());
    }

    public void setSubscribeListener() {
        this.roster.addSubscribeListener(ListenerManager.subscribeListener());
    }

    void subscribeOwner(BareJid owner) throws SmackException.NotLoggedInException, InterruptedException, SmackException.NotConnectedException {
        roster.sendSubscriptionRequest(owner);
        System.out.println("INFO: Enviando solicitud de amistad al nuevo OWNER");
    }

    void subscribeRequest(BareJid jid) throws SmackException.NotLoggedInException, InterruptedException, SmackException.NotConnectedException {
        roster.sendSubscriptionRequest(jid);
    }

    void transferFile(Jid jid) throws Exception {
        int count = 0;
        FileTransferManager fileTransferManager = FileTransferManager.getInstanceFor(connection);
        File file = new File(System.getProperty("user.dir") + "/log.csv");

        EntityFullJid entityFullJid = JidCreate.entityFullFrom(jid);
        OutgoingFileTransfer transfer = fileTransferManager.createOutgoingFileTransfer(entityFullJid);
        transfer.sendFile(file, null);

        while (!transfer.isDone() && count < 5) {
            if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                System.out.println("ERROR: " + transfer.getError());
                throw new Exception("ERROR : " + transfer.getError());
            } else {
                System.out.println(transfer.getStatus());
                System.out.println(transfer.getProgress());
            }
            count++;
            sleep(5000);
        }

        if (count >= 5) {
            transfer.cancel();
            System.out.println("INFO: Transferencia cancelada");
        } else {
            System.out.println("INFO: Archivo transferido");
        }
    }

}
