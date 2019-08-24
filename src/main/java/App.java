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
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.*;
import java.util.Collection;

public class App {

    static File file = new File(System.getProperty("user.dir") + "/config.properties"); //se genera archivo de propiedades
    static XmppClient xmppClient = null; //se instancia la clase para los metodos de xmpp
    static PropertiesManager propertiesManager = null; //se instancia la clase para manejar las propiedades
    static boolean checkOwner = false; //se genera booleando para verificar estatus de dueño

    public static void main(String[] args) {
        //datos en duro para la conexión al servidor xmpp
        String domain   = "10.40.5.9";
        String user     = "estacion";
        String password = "estacion";

            //INICIO conexión y generación de cuenta en XMPP
            if (!file.exists()){
                try {
                    xmppClient = new XmppClient(domain, user, password);

                    if (xmppClient.getConnection().isConnected()){
                        xmppClient.generateAccount();
                        propertiesManager = new PropertiesManager(domain, user, password);
                    }

                } catch (Exception e) {
                    file.delete();
                    System.out.println("ERROR: No se pudo generar la conexión al servidor");
                    System.exit(-1); //matando el servicio
                    e.printStackTrace();
                }
            }
            //FIN conexión y generación de cuenta en XMPP

            //INICIO carga de configuración e login de cuenta en servidor XMPP
            if (file.exists() && !file.isDirectory()){
                try {
                    if (xmppClient == null){
                        propertiesManager = new PropertiesManager();
                        xmppClient = new XmppClient(
                                propertiesManager.getDomain(),
                                propertiesManager.getUser(),
                                propertiesManager.getPassword()
                        );
                    }

                    xmppClient.logInAccount("test");

                    checkOwner = propertiesManager.checkOwner();

                    if (checkOwner){
                        xmppClient.getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);
                        System.out.println("INFO: subcription mode manual");
                    } else {
                        xmppClient.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                        System.out.println("INFO: subcription mode accept all");
                    }

                    //xmppClient.sendPresence();

                } catch (Exception e) {
                    System.out.println("ERROR: No se pudo logear en el servidor");
                    System.exit(-1); //matando el servicio
                    e.printStackTrace();
                }
            }
            //FIN carga de configuración e login de cuenta en servidor XMPP



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
                                System.out.println("INFO: El jid " + addresses + "está siendo configurado como OWNER");
                                System.out.println("INFO: " + recorredor.toString());
                                propertiesManager.setOwner(recorredor.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.exit(-1); //matando el servicio
                            }
                        }
                    } else {
                        for (Jid recorredor: addresses){
                            boolean checkWhiteList = propertiesManager.checkWhiteList(recorredor.toString());
                            if (checkWhiteList){
                                try {
                                    xmppClient.subscribeRequest(recorredor.asBareJid());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.exit(-1); //matando el servicio
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

                    System.out.println("INFO: " + presence);
                }
            });
        } catch (Exception e) {
            System.out.println("ERROR: cerrando");
            e.printStackTrace();
            System.exit(-1); //matando el servicio
        }
        //FIN Listener para subscripción de cuentas

        //INICIO Listener de chat
        try {
            xmppClient.getChatmanager().addIncomingListener(new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                    System.out.println("<IN: " + message.getBody());
                    System.out.println("<FROM: " + from);
                    //System.out.println(propertiesManager.getOwner());
                    Message respuesta;

                    if (message.getBody().equalsIgnoreCase("datos")){

                        ReadCSV readCSV = new ReadCSV();
                        String[] lastData = null;
                        try {
                            lastData = readCSV.readData();
                        } catch (Exception e) {
                            System.out.println("ERROR: algo paso en el chatmanager");
                            e.printStackTrace();
                            System.exit(-1); //matando el servicio
                        }

                        respuesta = new Message(message.getTo(),
                                "DateTime: " + lastData[0] +
                                "\nDirección del viento: " + lastData[1] +
                                "\nVelocidad del viento: " + lastData[2] + " Km/h" +
                                "\nLluvia acumulada: " + lastData[3] + " mm" +
                                "\nPresión atmosférica: " + lastData[4] + " hPa" +
                                "\nHumedad ambiental: " + lastData[5] + " %" +
                                "\nTemperatura: " + lastData[6] + " °C" +
                                "\nConcentración de CO: " + lastData[10] +
                                "\nConcentración de SO2: " + lastData[11] +
                                "\nConcentración de NO2: " + lastData[12]
                                );

                        respuesta.setType(Message.Type.chat);
                        System.out.println(">OUT: " + respuesta.toString());
                        try {
                            chat.send(respuesta);
                        } catch (Exception e) {
                            System.out.println("ERROR: algo paso en la respuesta del chatmanager");
                            e.printStackTrace();
                            System.exit(-1); //matando el servicio
                        }
                    } else if (message.getBody().equalsIgnoreCase("log")){
                        try {
                            xmppClient.transferFile(message.getFrom());
                            System.out.println("INFO: tratando de enviar archivo a" + message.getFrom());
                        } catch (Exception e) {
                            System.out.println("ERROR: algo pasó en el transferfile");
                            e.printStackTrace();
                            System.exit(-1); //matando el servicio
                        }

                    }

                    if (from.toString().equals(propertiesManager.getOwner())){
                        if (message.getBody().startsWith("AGREGAR:")){
                            String[] texto = message.getBody().split(":");
                            String jid = texto[1].trim();
                            try {
                                propertiesManager.addWhiteList(jid);
                            } catch (Exception e) {
                                System.out.println("ERROR: algo paso al agregar a la lista blanca");
                                e.printStackTrace();
                                System.exit(-1); //matando el servicio
                            }
                        }
                    }

                }
            });
        } catch (Exception e) {
            System.out.println("ERROR: en la wea grande");
            e.printStackTrace();
            System.exit(-1); //matando el servicio
        }

        if (checkOwner){
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
                System.out.println("ERROR: send subcribe");
                e.printStackTrace();
                System.exit(-1); //matando el servicio
            }
        }

        //FIN Listener de chat

        /*
        while (xmppClient.getConnection().isConnected()){

        }

        if (!xmppClient.getConnection().isConnected()){
            System.exit(-1);
        }
        */

        while (true) {
            if (!xmppClient.getConnection().isConnected()) {
                System.out.println("MURIO LA WEA");
                break;
            }
        }
        System.exit(-2);
    }
}
