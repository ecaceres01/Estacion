package ClientManager;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CsvManager {
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
