import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadCSV {

    public void readAll() throws IOException {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .build();

        Reader reader = Files.newBufferedReader(Paths.get(System.getProperty("user.dir") + "/last.csv"));

        CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();

        String[] record;
        while ((record = csvReader.readNext()) != null){
            System.out.println("DateTime: " + record[0]);
            System.out.println("Temperatura: " + record[1] + " °C");
            System.out.println("Humedad ambiental: " + record[2] + " %");
            System.out.println("Velocidad viento: " + record[3] + " Km/h");
            System.out.println("Direccion viento: " + record[4]);
            System.out.println("Presion atmosferica: " + record[5] + " Pa");
            System.out.println("Lluvia caida: " + record[6] + " mm");


        }
    }

    public String[] readData() throws Exception {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .build();

        Reader reader = Files.newBufferedReader(Paths.get(System.getProperty("user.dir") + "/last.csv"));

        CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();

        String[] line;

        if ((line = csvReader.readNext()) != null){
            return line;
        } else {
            throw new Exception("ERROR: no se puede leer la data");
        }
    }
}
