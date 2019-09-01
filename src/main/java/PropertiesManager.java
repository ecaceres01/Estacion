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
        if (this.owner == null){
            System.out.println("INFO: Owner no asignado");
            return false;
        } else {
            System.out.println("INFO: Owner asignado: " + this.owner);
            return true;
        }
    }
}
