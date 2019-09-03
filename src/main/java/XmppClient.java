import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static java.lang.Thread.sleep;

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

    ConnectionListener connectionListener(PropertiesManager propertiesManager) {

        return new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection1) {
                logInAccount("test");
            }

            @Override
            public void authenticated(XMPPConnection connection1, boolean resumed) {
                manager();
                if (propertiesManager.checkOwner()) {
                    roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
                } else {
                    roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                }
            }

            @Override
            public void connectionClosed() {
                try {
                    connection.connect();
                    System.out.println("INFO: Intentando reconección con el servidor");
                } catch (Exception e) {
                    System.out.println("ERROR: No se pudo realizar la reconección");
                    e.printStackTrace();
                }
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

    private void logInAccount(String resource) {
        try {
            connection.login(this.user, this.password, Resourcepart.from(resource));
        } catch (Exception e) {
            System.out.println("ERROR: No se pudo realizar el LogIn a la cuenta");
            e.printStackTrace();
        }
        System.out.println("INFO: Logeando en el servidor " + connection.getXMPPServiceDomain().toString() + " como: " + connection.getUser());
    }

    private void manager() {
        this.roster = Roster.getInstanceFor(connection);
        this.chatManager = ChatManager.getInstanceFor(connection);
        this.pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(300);
        System.out.println("INFO: La cuenta se encuentra autentificada");
    }

    RosterListener rosterListener(PropertiesManager propertiesManager) {
        return new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> addresses) {
                for (Jid recorredor : addresses) {
                    if (!propertiesManager.checkOwner()) {
                        try {
                            roster.sendSubscriptionRequest(recorredor.asBareJid());
                            propertiesManager.setOwner(recorredor.toString());
                            System.out.println("INFO: Reiniciando servicio para confirmar nuevo estatus");
                            connection.disconnect();
                        } catch (Exception e) {
                            System.out.println("ERROR: No se pudo realizar registro para OWNER");
                            e.printStackTrace();
                        }
                    } else {
                        boolean chechWhitelist = propertiesManager.checkWhitelist(recorredor.toString());
                        if (chechWhitelist) {
                            try {
                                roster.sendSubscriptionRequest(recorredor.asBareJid());
                            } catch (Exception e) {
                                System.out.println("ERROR: No se pudo enviar la solicitud de subcripción");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void entriesUpdated(Collection<Jid> addresses) {

            }

            @Override
            public void entriesDeleted(Collection<Jid> addresses) {

            }

            @Override
            public void presenceChanged(Presence presence) {

            }
        };
    }

    IncomingChatMessageListener incomingChatMessageListener(PropertiesManager propertiesManager) {
        return new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                System.out.println("FROM: " + from);
                System.out.println("INPUT: " + message.getBody());

                Message respuesta;

                if (message.getBody().equalsIgnoreCase("datos")) {
                    CsvManager csvManager = new CsvManager();
                    String[] lastData = null;
                    try {
                        lastData = csvManager.readData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    respuesta = new Message(message.getTo(),
                            "DateTime: " + lastData[0] +
                                    "\nDirección del viento: " + lastData[1] +
                                    "\nVelocidad del viento: " + lastData[2] + " Km/h" +
                                    "\nLluvia acumulada en el día: " + lastData[3] + " mm" +
                                    "\nPresión atmosférica: " + lastData[4] + " hPa" +
                                    "\nHumedad ambiental: " + lastData[5] + " %" +
                                    "\nTemperatura: " + lastData[6] + " °C" +
                                    "\nConcentración de CO: " + lastData[10] +
                                    "\nConcentración de SO2: " + lastData[11] +
                                    "\nConcentración de NO2: " + lastData[12]
                    );
                    respuesta.setType(Message.Type.chat);
                    try {
                        chat.send(respuesta);
                        System.out.println("OUTPUT: " + respuesta.toString());
                    } catch (Exception e) {
                        System.out.println("ERROR: No se pudo mandar el mensaje");
                        e.printStackTrace();
                    }
                }

                if (message.getBody().equalsIgnoreCase("log")) {
                    transferFile(from);
                    respuesta = new Message(message.getTo(), "Enviando archivo...");
                    respuesta.setType(Message.Type.chat);
                    try {
                        chat.send(respuesta);
                        System.out.println("OUTPUT: " + respuesta.toString());
                    } catch (Exception e) {
                        System.out.println("ERROR: No se pudo mandar el mensaje");
                        e.printStackTrace();
                    }
                    System.out.println("INFO: Enviando archivo a " + from);
                }

                if (from.toString().equals(propertiesManager.getOwner())) {
                    if (message.getBody().startsWith("AGREGAR:")) {
                        String[] texto = message.getBody().split(":");
                        String jid = texto[1].trim();
                        propertiesManager.addWhitelist(jid);
                        respuesta = new Message(message.getTo(),
                                "El JID " + jid + "ha sido agregado a la lista blanca con éxito");
                        respuesta.setType(Message.Type.chat);
                        try {
                            chat.send(respuesta);
                            System.out.println("OUTPUT: " + respuesta.toString());
                        } catch (Exception e) {
                            System.out.println("ERROR: No se pudo mandar el mensaje");
                            e.printStackTrace();
                        }
                    }
                }

            }
        };
    }

    private void transferFile(EntityBareJid jid) {
        try {
            int count = 0;
            EntityFullJid entityFullJid = JidCreate.entityFullFrom(jid);
            File file = new File(System.getProperty("user.dir") + "/log.csv");

            FileTransferManager fileTransferManager = FileTransferManager.getInstanceFor(connection);
            OutgoingFileTransfer outgoingFileTransfer = fileTransferManager.createOutgoingFileTransfer(entityFullJid);
            outgoingFileTransfer.sendFile(file, null);

            while (!outgoingFileTransfer.isDone()) {
                if (outgoingFileTransfer.getStatus().equals(FileTransfer.Status.error)) {
                    System.out.println("ERROR: " + outgoingFileTransfer.getError());
                } else {
                    System.out.println(outgoingFileTransfer.getStatus());
                    System.out.println(outgoingFileTransfer.getProgress());
                }
                sleep(5000);
                count++;
                if (count == 4) {
                    outgoingFileTransfer.cancel();
                }
            }
        } catch (XmppStringprepException | SmackException | InterruptedException e) {
            System.out.println("ERROR: No se pudo enviar el archivo");
            e.printStackTrace();
        }
    }

    SubscribeListener subscribeListener(PropertiesManager propertiesManager) {
        return new SubscribeListener() {
            @Override
            public SubscribeAnswer processSubscribe(Jid from, Presence subscribeRequest) {
                SubscribeAnswer subscribeAnswer;
                if (propertiesManager.checkWhitelist(from.toString())) {
                    subscribeAnswer = SubscribeAnswer.ApproveAndAlsoRequestIfRequired;
                } else {
                    subscribeAnswer = SubscribeAnswer.Deny;
                }
                return subscribeAnswer;
            }
        };
    }

}
