import java.io.*;
import java.util.Properties;

class PropertiesManager {
    private Properties properties = new Properties();
    private String domain, user, password, owner, whiteList;
    private OutputStream outputStream;
    private InputStream inputStream;

    String getDomain() {
        return domain;
    }

    String getUser() {
        return user;
    }

    String getPassword() {
        return password;
    }

    String getOwner() {
        return owner;
    }

    PropertiesManager(String domain, String user, String password) throws IOException {
        this.domain = domain;
        this.user = user;
        this.password = password;

        outputStream = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
        System.out.println("INFO: Creando archivo de configuración");

        properties.setProperty("dominio", domain);
        properties.setProperty("usuario", user);
        properties.setProperty("password", password);
        properties.store(outputStream, "CONFIGURACION DE REGISTRO");

        outputStream.close();
    }

    PropertiesManager() throws IOException {
        inputStream = new FileInputStream(System.getProperty("user.dir") + "/config.properties");
        properties.load(inputStream);

        this.domain = properties.getProperty("dominio");
        this.user = properties.getProperty("usuario");
        this.password = properties.getProperty("password");

        inputStream.close();
    }

    private void loadOwner() {
        try {
            inputStream = new FileInputStream(System.getProperty("user.dir") + "/config.properties");
            properties.clear();
            properties.load(inputStream);
            this.owner = properties.getProperty("owner");
            inputStream.close();
        } catch (IOException e) {
            System.out.println("ERROR: No se pudo cargar archivo de configuración");
            e.printStackTrace();
        }
    }

    boolean checkOwner() {
        loadOwner();

        if (this.owner == null) {
            System.out.println("INFO: OWNER no asignado");
            return false;
        } else {
            System.out.println("INFO: OWNER asignado: " + this.owner);
            return true;
        }
    }

    void setOwner(String owner) {
        this.owner = owner;
        try {
            outputStream = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
            properties.setProperty("owner", owner);
            properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER");
            outputStream.close();
            System.out.println("INFO: El JID " + owner + " ha sido registrado como OWNER");
        } catch (IOException e) {
            System.out.println("ERROR: No se pudo cargar archivo de configuración");
            e.printStackTrace();
        }
    }

    private void loadWhitelist() {
        try {
            inputStream = new FileInputStream(System.getProperty("user.dir") + "/config.properties");
            properties.clear();
            properties.load(inputStream);
            this.whiteList = properties.getProperty("jid autorizados");
            inputStream.close();
        } catch (IOException e) {
            System.out.println("INFO: No se pudo abrir archivo para cargar lista blanca");
            e.printStackTrace();
        }
    }

    void addWhitelist(String jid) {
        loadWhitelist();
        try {
            if (whiteList == null) {
                outputStream = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
                whiteList = jid.concat(";");
                properties.setProperty("jid autorizados", whiteList);
                properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER Y LISTA DE USUARIOS AUTORIZADOS");
                outputStream.close();
            } else {
                String[] helper = whiteList.split(";");
                for (String s : helper) {
                    if (s.equalsIgnoreCase(jid)) {
                        throw new Exception("ERROR: JID " + jid + "ya se encuentra registrada en la lista blanca");
                    }
                }
                outputStream = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
                whiteList = whiteList.concat(jid + ";");
                properties.setProperty("jid autorizados", whiteList);
                properties.store(outputStream, "CONFIGURACION DE REGISTRO CON OWNER Y LISTA DE USUARIOS AUTORIZADOS");
                outputStream.close();
            }
        } catch (IOException e) {
            System.out.println("ERROR: No se pido abrir archivo para agregar usuario autorizado");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean checkWhitelist(String jid) {
        loadWhitelist();
        String[] helper = whiteList.split(";");
        for (String s : helper) {
            if (s.equalsIgnoreCase(jid)) {
                return true;
            }
        }
        return false;
    }
}
