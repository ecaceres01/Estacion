import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;

import java.io.*;
import java.util.Collection;

public class App {

    static File file = new File(System.getProperty("user.dir") + "/config.properties");
    static XmppClient xmppClient = null;
    static PropertiesManager propertiesManager = null;
    static boolean checkOwner = false;


    public static void main(String[] args) {
        String domain   = "10.40.5.9";
        String user     = "estacion";
        String password = "estacion";

        for (int i = 0; i<10; i++){
            //INICIO conexión y generación de cuenta en XMPP
            if (!file.exists()){
                try {
                    propertiesManager = new PropertiesManager(domain, user, password);

                    xmppClient = new XmppClient(domain, user, password);
                    xmppClient.generateAccount();

                } catch (Exception e) {
                    file.delete();
                    System.out.println("ERROR: No se pudo generar la conexión al servidor");
                    e.printStackTrace();
                }
            }
            //FIN conexión y generación de cuenta en XMPP

            //INICIO carga de configuración e login de cuenta en servidor XMPP
            if (file.exists() && !file.isDirectory()){
                try {
                    if (xmppClient != null){
                        xmppClient.logInAccount("test");
                    } else  {
                        propertiesManager = new PropertiesManager();
                        xmppClient = new XmppClient(
                                propertiesManager.getDomain(),
                                propertiesManager.getUser(),
                                propertiesManager.getPassword()
                        );
                        xmppClient.logInAccount("test");
                    }

                    checkOwner = propertiesManager.checkOwner();

                    if (checkOwner == true){
                        xmppClient.getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);
                    } else {
                        xmppClient.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                    }

                    break;
                } catch (Exception e) {
                    System.out.println("ERROR: No se pudo logear en el servidor");
                    e.printStackTrace();
                }
            }
            //FIN carga de configuración e login de cuenta en servidor XMPP
        }

        //INICIO Listener para subscripción de cuentas
        try {
            xmppClient.getRoster().addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<Jid> addresses) {
                    System.out.println("INFO: El jid " + addresses + " a sido agregado al roster");
                    if (!checkOwner){
                        for (Jid recorredor: addresses){
                            try {
                                xmppClient.subscribeOwner(recorredor.asBareJid());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        for (Jid recorredor: addresses){
                            boolean checkWhiteList = propertiesManager.checkWhiteList(recorredor.toString());
                            if (checkWhiteList){
                                try {
                                    xmppClient.subscribeRequest(recorredor.asBareJid());
                                } catch (SmackException.NotLoggedInException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (SmackException.NotConnectedException e) {
                                    e.printStackTrace();
                                }
                            } else {

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
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        //FIN Listener para subscripción de cuentas

        //INICIO Listener de chat
        try {
            xmppClient.getChatmanager().addIncomingListener(new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                    System.out.println("<IN: " + message.getBody());
                    Message respuesta;

                    if (message.getBody().equalsIgnoreCase("datos")){

                        ReadCSV readCSV = new ReadCSV();
                        String[] lastData = null;
                        try {
                            lastData = readCSV.readData();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        respuesta = new Message(message.getTo(),
                                "timestamp: " + lastData[0] +
                                "\nTemperatura: " + lastData[1] + " °C" +
                                "\nHumedad ambiental: " + lastData[2] + " %" +
                                "\nVelocidad del viento: " + lastData[3] + " Km/h" +
                                "\nDirección del viento: " + lastData[4] +
                                "\nPresión atmosférica: " + lastData[5] + " Pa");

                        respuesta.setType(Message.Type.chat);
                        System.out.println(">OUT: " + respuesta.toString());
                        try {
                            chat.send(respuesta);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (message.getBody().equalsIgnoreCase("log")){
                        xmppClient.transferFile(message.getFrom());
                    }

                    if (from.toString().equals(propertiesManager.getOwner())){
                        if (message.getBody().startsWith("AGREGAR:")){
                            String[] texto = message.getBody().split(":");
                            String jid = texto[1].trim();
                            try {
                                propertiesManager.addWhiteList(jid);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            xmppClient.getRoster().addSubscribeListener(new SubscribeListener() {
                @Override
                public SubscribeAnswer processSubscribe(Jid from, Presence subscribeRequest) {
                    SubscribeAnswer subscribeAnswer;
                    boolean checkWhiteList = propertiesManager.checkWhiteList(from.toString());
                    if (checkWhiteList){
                        subscribeAnswer = SubscribeAnswer.Approve;
                    } else {
                        subscribeAnswer = SubscribeAnswer.Deny;
                    }
                    return subscribeAnswer;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        //FIN Listener de chat

        while (xmppClient.getConnection().isConnected()){

        }

    }
}
