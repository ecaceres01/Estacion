package Organizador;

import Organizador.Excepciones.ListException;

import java.io.*;
import java.util.Properties;

public class PropertiesManager {
    private Properties properties = new Properties();
    private String domain, user, password, owner, listaBlanca;
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

    public PropertiesManager() throws IOException {
        getInputstream();
        this.domain = properties.getProperty("dominio");
        this.user = properties.getProperty("usuario");
        this.password = properties.getProperty("password");
        inputStream.close();
    }

    private void getInputstream() throws IOException {
        this.inputStream = new FileInputStream(System.getProperty("user.dir") + "/config.properties");
        this.properties.clear();
        this.properties.load(inputStream);
    }

    private void getOutputstream() throws FileNotFoundException {
        this.outputStream = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
    }

    public boolean checkOwner() throws IOException {
        getInputstream();
        this.owner = properties.getProperty("owner");
        inputStream.close();

        if (owner == null) {
            return false;
        } else {
            return true;
        }
    }

    void setOwner(String owner) throws IOException {
        this.owner = owner;
        System.out.println("INFO: Se está reclamando la estación");
        getOutputstream();
        properties.setProperty("owner", owner);
        properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER");
        outputStream.close();
    }

    private void loadList() throws IOException {
        getInputstream();
        this.listaBlanca = properties.getProperty("listaBlanca");
        inputStream.close();
    }

    public boolean checkList(String jid) throws IOException {
        loadList();
        String[] helper;
        if (listaBlanca != null) {
            helper = listaBlanca.split(";");
            for (String s : helper) {
                if (s.equals(jid)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addList(String jid) throws IOException, ListException {
        loadList();
        getOutputstream();
        String[] helper;

        if (listaBlanca == null) {
            listaBlanca = jid.concat(";");
            properties.setProperty("listaBlanca", listaBlanca);
            properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER Y LISTA DE ACEPTADAS");
        } else {
            helper = listaBlanca.split(";");
            for (String s : helper) {
                if (s.equals(jid)) {
                    properties.setProperty("listaBlanca", listaBlanca);
                    properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER Y LISTA DE ACEPTADAS");
                    outputStream.close();
                    throw new ListException("JID ya ingresado");
                }
            }
            listaBlanca = listaBlanca.concat(jid + ";");
            properties.setProperty("listaBlanca", listaBlanca);
            properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER Y LISTA DE ACEPTADAS");
        }

        System.out.println("INFO: El jid " + " ha sido agregado a la lista blanca");
        outputStream.close();
    }
}
