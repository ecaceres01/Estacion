import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.jid.*;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.IOException;

import static java.lang.Thread.sleep;

public class XmppClient {
    private AbstractXMPPConnection connection;
    private XMPPTCPConnectionConfiguration configuration;
    private String domain, user, password, owner;
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
                //.enableDefaultDebugger()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();

        connection = new XMPPTCPConnection(configuration);
        connection.connect();
        System.out.println("INFO: Conectandose a " + connection.getXMPPServiceDomain().toString());
    }

    public XMPPTCPConnection getConnection() {
        return (XMPPTCPConnection) connection;
    }

    public void generateAccount() throws XmppStringprepException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {

        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.sensitiveOperationOverInsecureConnection(true);
        accountManager.createAccount(Localpart.from(user), password);
    }

    public void logInAccount(String resource) throws IOException, InterruptedException, XMPPException, SmackException {
        connection.login(this.user, this.password, Resourcepart.from(resource));
        System.out.println("INFO: Logeando en el servidor " + connection.getXMPPServiceDomain().toString() + " como usuario: " + connection.getUser());

        this.roster = Roster.getInstanceFor(connection);
        this.chatManager = ChatManager.getInstanceFor(connection);
        this.pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(300);
    }

    public Roster getRoster() throws Exception {
        if (this.roster == null) {
            throw new Exception("No hay roster creado");
        }
        return this.roster;
    }

    public void subscribeOwner(BareJid owner) throws SmackException.NotLoggedInException, InterruptedException, SmackException.NotConnectedException {
        roster.sendSubscriptionRequest(owner);
        this.owner = owner.toString();
        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
    }

    public void subscribeRequest(BareJid jid) throws SmackException.NotLoggedInException, InterruptedException, SmackException.NotConnectedException {
        roster.sendSubscriptionRequest(jid);
    }

    public ChatManager getChatmanager() throws Exception {
        if (this.chatManager == null) {
            throw new Exception("No hay chatmanager creado");
        }
        return this.chatManager;
    }

    public void transferFile(Jid jid) throws XmppStringprepException, SmackException, InterruptedException {
        FileTransferManager fileTransferManager = FileTransferManager.getInstanceFor(connection);
        File file = new File(System.getProperty("user.dir") + "/log.csv");

        EntityFullJid entityFullJid = JidCreate.entityFullFrom(jid);
        OutgoingFileTransfer transfer = fileTransferManager.createOutgoingFileTransfer(entityFullJid);
        transfer.sendFile(file, null);

        while (!transfer.isDone()) {
            if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                System.out.println("ERROR: " + transfer.getError());
            } else {
                System.out.println(transfer.getStatus());
                System.out.println(transfer.getProgress());
            }
            sleep(10000);
        }

        System.out.println("INFO: Archivo transferido");
    }

}
