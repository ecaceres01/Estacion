package ClientManager;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;

import java.io.IOException;
import java.util.Collection;

public class ListenerManager {
    private static XmppClient xmppClient;
    private static PropertiesManager propertiesManager;

    public ListenerManager(XmppClient xmppClient, PropertiesManager propertiesManager) {
        ListenerManager.xmppClient = xmppClient;
        ListenerManager.propertiesManager = propertiesManager;
    }

    static RosterListener rosterListener() throws Exception {
        boolean checkOwner = propertiesManager.checkOwner();
        return new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> collection) {
                System.out.println("INFO: El jid " + collection + " a sido agregado al roster");
                if (!checkOwner) {
                    for (Jid recorredor : collection) {
                        try {
                            xmppClient.subscribeOwner(recorredor.asBareJid());
                            System.out.println("INFO: El jid " + recorredor.toString() + " está siendo seteado como OWNER");

                            propertiesManager.setOwner(recorredor.toString());

                            if (checkOwner) {
                                xmppClient.getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);
                            }
                        } catch (Exception e) {
                            System.out.println("ERROR: no se logró subcribir a " + recorredor.toString());
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (Jid recorredor : collection) {
                        try {
                            boolean checkWhiteList = propertiesManager.checkWhiteList(recorredor.toString());
                            if (checkWhiteList) {
                                xmppClient.subscribeRequest(recorredor.asBareJid());
                            }
                        } catch (Exception e) {
                            System.out.println("ERROR: No se logro acceso al whitelist");
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void entriesUpdated(Collection<Jid> collection) {

            }

            @Override
            public void entriesDeleted(Collection<Jid> collection) {

            }

            @Override
            public void presenceChanged(Presence presence) {
                System.out.println("INFO: " + presence);
            }
        };
    }

    static IncomingChatMessageListener chatMessageListener() {
        return new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
                System.out.println("<IN: " + message.getBody());
                System.out.println("<FROM: " + entityBareJid);

                Message respuesta;

                if (message.getBody().equalsIgnoreCase("datos")) {

                    CsvManager readCSV = new CsvManager();
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
                } else if (message.getBody().equalsIgnoreCase("log")) {
                    try {
                        xmppClient.transferFile(message.getFrom());
                        System.out.println("INFO: tratando de enviar archivo a" + message.getFrom());
                    } catch (Exception e) {
                        System.out.println("ERROR: algo pasó en el transferfile");
                        e.printStackTrace();
                    }
                }

                if (entityBareJid.toString().equals(propertiesManager.getOwner())) {
                    if (message.getBody().startsWith("AGREGAR:")) {
                        String[] texto = message.getBody().split(":");
                        String jid = texto[1].trim();
                        try {
                            propertiesManager.addWhiteList(jid);
                        } catch (Exception e) {
                            System.out.println("ERROR: algo paso al agregar a la lista blanca");
                            e.printStackTrace();
                        }

                        respuesta = new Message(message.getTo(),
                                "el usuario " + jid + "ha sido agregado a tu lista blanca con éxito");

                        respuesta.setType(Message.Type.chat);

                        try {
                            chat.send(respuesta);
                        } catch (Exception e) {
                            System.out.println("ERROR: No se pudo enviar respuesat de whitelist");
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    static SubscribeListener subscribeListener() {
        return new SubscribeListener() {
            @Override
            public SubscribeAnswer processSubscribe(Jid jid, Presence presence) {
                SubscribeAnswer subscribeAnswer = SubscribeAnswer.Deny;
                try {
                    boolean checkWhiteList = propertiesManager.checkWhiteList(jid.toString());
                    if (checkWhiteList) {
                        subscribeAnswer = SubscribeAnswer.ApproveAndAlsoRequestIfRequired;
                    }
                } catch (IOException e) {
                    System.out.println("ERROR: No se logró activar el SubscribeListener");
                    e.printStackTrace();
                }
                return subscribeAnswer;
            }
        };
    }

}