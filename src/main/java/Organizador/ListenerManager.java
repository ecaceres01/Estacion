package Organizador;

import Organizador.Excepciones.ChatException;
import Organizador.Excepciones.CsvException;
import Organizador.Excepciones.ListException;
import Organizador.Excepciones.RosterException;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.PresenceEventListener;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.io.IOException;
import java.util.Collection;

public class ListenerManager {
    private static XmppClient xmppClient;
    private static PropertiesManager propertiesManager;

    public ListenerManager(XmppClient xmppClient, PropertiesManager propertiesManager) {
        ListenerManager.xmppClient = xmppClient;
        ListenerManager.propertiesManager = propertiesManager;
    }

    static RosterListener rosterListener() throws IOException {
        boolean checkOwner = propertiesManager.checkOwner();
        return new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> collection) {
                System.out.println("INFO: El jid " + collection + " a sido agregado al roster");
                if (!checkOwner) {
                    for (Jid recorredor : collection) {
                        try {
                            xmppClient.subscribeOwner(recorredor.asBareJid());
                            propertiesManager.setOwner(recorredor.toString());

                        } catch (Exception e) {
                            System.out.println("ERROR: No se logró asignar a " + recorredor.toString() + " como owner");
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (Jid recorredor : collection) {
                        try {
                            boolean checkList = propertiesManager.checkList(recorredor.toString());
                            if (checkList) {
                                xmppClient.subcribeRequest(recorredor.asBareJid());
                            }
                        } catch (Exception e) {
                            System.out.println("ERROR: Problemas con revisar la lista");
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

            }
        };
    }

    static SubscribeListener subscribeListener() {
        return new SubscribeListener() {
            @Override
            public SubscribeAnswer processSubscribe(Jid jid, Presence presence) {
                SubscribeAnswer subscribeAnswer = SubscribeAnswer.Deny;
                try {
                    boolean checkWhiteList = propertiesManager.checkList(jid.toString());
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

    static IncomingChatMessageListener incomingChatMessageListener() {
        return new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
                System.out.println("<IN: " + message.getBody());
                System.out.println("<FROM: " + entityBareJid);
                Message respuesta;
                Message error;

                if (entityBareJid.toString().equals(propertiesManager.getOwner())) {
                    if (message.getBody().startsWith("AGREGAR:")) {
                        String[] texto = message.getBody().split(":");
                        String jid = texto[1].trim();
                        try {
                            propertiesManager.addList(jid);
                        } catch (IOException e) {
                            System.out.println("ERROR: No se pudo cargar inputStream");
                            e.printStackTrace();
                        } catch (ListException e) {
                            error = new Message(message.getTo(),
                                    "el jid ya existe en la lista");
                            error.setType(Message.Type.chat);
                            try {
                                chat.send(error);
                            } catch (Exception ex) {
                                System.out.println("ERROR: No se pudo enviar el mensaje");
                                ex.printStackTrace();
                            }
                            System.out.println("INFO: JID ya agregado");
                            e.printStackTrace();
                        }

                        respuesta = new Message(message.getTo(),
                                "el usuario " + jid + " ha sido agregado a tu lista blanca con éxito");
                        respuesta.setType(Message.Type.chat);
                        try {
                            chat.send(respuesta);
                        } catch (Exception e) {
                            System.out.println("ERROR: No se pudo enviar el mensaje");
                            e.printStackTrace();
                        }
                    }
                }

                if (message.getBody().equalsIgnoreCase("datos")) {
                    CsvManager readCSV = new CsvManager();
                    String[] lastData = null;
                    try {
                        lastData = readCSV.readData();
                    } catch (IOException e) {
                        System.out.println("ERROR: No se pudo leer la data");
                        e.printStackTrace();
                    } catch (CsvException e) {
                        System.out.println("ERROR: No se pudo leer archivo csv");
                        e.printStackTrace();
                    }

                    respuesta = new Message(message.getTo(),
                            "DateTime: " + lastData[0] +
                                    "\nDirección del viento: " + lastData[1] +
                                    "\nVelocidad del viento: " + lastData[2] + " Km/h" +
                                    "\nPrecipitacion en la última hora: " + lastData[3] + " mm" +
                                    "\nPresión atmosférica: " + lastData[4] + " kPa" +
                                    "\nAltitud: " + lastData[5] + " m" +
                                    "\nHumedad ambiental: " + lastData[7] + " %" +
                                    "\nTemperatura: " + lastData[8] + " °C" +
                                    "\nCO: " + lastData[12] + " ug/m3" +
                                    "\nSO2: " + lastData[13] + " ug/m3" +
                                    "\nNO2: " + lastData[14] + " ug/m3" +
                                    "\nPM2,5: " + lastData[15] + " ug/m3"
                    );

                    respuesta.setType(Message.Type.chat);
                    try {
                        chat.send(respuesta);
                    } catch (Exception e) {
                        System.out.println("ERROR: No se pudo enviar el mensaje");
                        e.printStackTrace();
                    }
                }

                if (message.getBody().equalsIgnoreCase("ping")) {
                    respuesta = new Message(message.getTo(), "pong");
                    respuesta.setType(Message.Type.chat);
                    try {
                        chat.send(respuesta);
                    } catch (Exception e) {
                        System.out.println("ERROR: No se pudo enviar el mensaje");
                        e.printStackTrace();
                    }
                }

            }
        };
    }

    static ConnectionListener connectionListener() {
        return new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {

            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {

            }

            @Override
            public void connectionClosed() {

            }

            @Override
            public void connectionClosedOnError(Exception e) {
                try {
                    xmppClient.normalConnection();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (SmackException ex) {
                    ex.printStackTrace();
                } catch (XMPPException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    static PingFailedListener pingFailedListener() {
        return new PingFailedListener() {
            @Override
            public void pingFailed() {
                try {
                    xmppClient.normalConnection();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

}
