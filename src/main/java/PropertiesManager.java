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

    public String getOwner(){
        return owner;
    }

    public PropertiesManager() throws IOException {
        inputStream = new FileInputStream(System.getProperty("user.dir") + "/config.properties");
        properties.clear();
        properties.load(inputStream);

        domain = properties.getProperty("dominio");
        user = properties.getProperty("usuario");
        password = properties.getProperty("password");

        inputStream.close();
    }

    public PropertiesManager(String domain, String user, String password) throws IOException {
        this.domain = domain;
        this.user = user;
        this.password = password;

        outputStream = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
        System.out.println("Creando archivo de configuración " + System.getProperty("user.dir") + "/config.properties");

        properties.setProperty("dominio", domain);
        properties.setProperty("usuario", user);
        properties.setProperty("password", password);
        properties.store(outputStream, "CONFIGURACIÓN DE REGISTRO");

        closeOutputstream();
    }

    private void closeOutputstream() throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
    }

    public void loadOwner(String owner) throws IOException {
        this.owner = owner;
        System.out.println("INFO: Seteando a owner...");
        outputStream = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
        properties.setProperty("owner", owner);
        properties.store(outputStream, "CONFIGURACIÓN DE REGISTRO CON DUEÑO");
        closeOutputstream();
    }

    public boolean checkOwner() throws IOException {
        inputStream = new FileInputStream(System.getProperty("user.dir") + "/config.properties");
        properties.clear();
        properties.load(inputStream);
        owner = properties.getProperty("owner");
        inputStream.close();
        if (owner == null){
            System.out.println("INFO: Owner no asignado");
            return false;
        } else {
            System.out.println("INFO: Owner asignado: " + owner);
            return true;
        }
    }

    public void addWhiteList(String jid) throws Exception {
        loadWhiteList();

        outputStream = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");

        if (whiteList == null){
            whiteList = jid.concat(";");
            properties.setProperty("listaBlanca", whiteList);
            properties.store(outputStream, "CONFIGURACIÓN DE REGISTRO CON DUEÑO Y LISTA BLANCA");

        } else {
            String[] helper = whiteList.split(";");
            for (int i=0; i < helper.length; i++){
                if (helper[i].equals(jid)){
                    properties.setProperty("listaBlanca", whiteList);
                    properties.store(outputStream, "CONFIGURACIÓN DE REGISTRO CON DUEÑO Y LISTA BLANCA");
                    closeOutputstream();
                    throw new Exception("ERROR: JID " + jid + " ya ingresada en la lista blanca");
                }
            }
            whiteList = whiteList.concat(jid + ";");
            properties.setProperty("listaBlanca", whiteList);
            properties.store(outputStream, "CONFIGURACIÓN DE REGISTRO CON DUEÑO Y LISTA BLANCA");
        }

        System.out.println("INFO: Agregando JID: " + jid + " a la lista blanca");

        closeOutputstream();
    }

    private void loadWhiteList(){
        try {
            inputStream = new FileInputStream(System.getProperty("user.dir") + "/config.properties");
            properties.clear();
            properties.load(inputStream);
            whiteList = properties.getProperty("listaBlanca");
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkWhiteList(String jid){
        loadWhiteList();
        String[] helper = whiteList.split(";");
        for (int i=0; i < helper.length; i++){
            if (helper[i].equals(jid)){
                return true;
            }
        }
        return false;
    }
}
