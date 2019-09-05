package ClientManager;

import java.io.*;
import java.util.Properties;

public class PropertiesManager {
    private Properties properties = new Properties();
    private String domain, user, password, owner, whiteList;
    private OutputStream outputStream;
    private InputStream inputStream;

    public String getDomain() {
        return domain;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getOwner() {
        return owner;
    }

    private void getInputstream() throws IOException {
        this.inputStream = new FileInputStream(System.getProperty("user.dir") + "/config.properties");
        this.properties.clear();
        this.properties.load(inputStream);
    }

    private void getOutputstream() throws FileNotFoundException {
        this.outputStream = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
    }

    public PropertiesManager() throws IOException {
        getInputstream();
        this.domain = properties.getProperty("dominio");
        this.user = properties.getProperty("usuario");
        this.password = properties.getProperty("password");
        inputStream.close();
    }

    public PropertiesManager(String domain, String user, String password) throws IOException {
        this.domain = domain;
        this.user = user;
        this.password = password;

        getOutputstream();
        System.out.println("INFO: Creando archivo de configuración");

        properties.setProperty("dominio", domain);
        properties.setProperty("usuario", user);
        properties.setProperty("password", password);
        properties.store(outputStream, "CONFIGURACION DE REGISTRO");

        outputStream.close();
    }

    public void setOwner(String owner) throws IOException {
        this.owner = owner;
        System.out.println("INFO: Se está recalamando la estación");

        getOutputstream();
        properties.setProperty("owner", owner);
        properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER");
        outputStream.close();
    }

    public boolean checkOwner() throws IOException {
        getInputstream();
        this.owner = properties.getProperty("owner");
        inputStream.close();
        if (owner == null) {
            System.out.println("INFO: Owner no asignado");
            return false;
        } else {
            System.out.println("INFO: Owner asignado: " + owner);
            return true;
        }
    }

    public void addWhiteList(String jid) throws Exception {
        loadWhiteList();
        getOutputstream();

        if (whiteList == null) {
            whiteList = jid.concat(";");
            properties.setProperty("whiteList", whiteList);
            properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER Y LISTA DE ACEPTADAS");
        } else {
            String[] helper = whiteList.split(";");
            for (String s : helper) {
                if (s.equals(jid)) {
                    properties.setProperty("whiteList", whiteList);
                    properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER Y LISTA BLANCA");
                    outputStream.close();
                    throw new Exception("ERROR: JID " + jid + " ya ingresada en la lista blanca");
                }
            }
            whiteList = whiteList.concat(jid + ";");
            properties.setProperty("whiteList", whiteList);
            properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER Y LISTA DE ACEPTADAS");
        }

        System.out.println("INFO: El jid " + jid + " ha sido agregado a la lista blanca");
        outputStream.close();
    }

    private void loadWhiteList() throws IOException {
        getInputstream();
        this.whiteList = properties.getProperty("whiteList");
        inputStream.close();
    }

    public boolean checkWhiteList(String jid) throws IOException {
        loadWhiteList();
        String[] helper = whiteList.split(";");
        for (String s : helper) {
            if (s.equals(jid)) {
                return true;
            }
        }
        return false;
    }
}