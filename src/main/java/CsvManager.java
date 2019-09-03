import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

class CsvManager {

    String[] readData() throws Exception {
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();

        Reader reader = Files.newBufferedReader(Paths.get(System.getProperty("user.dir") + "/last.csv"));
        CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
        String[] line;
        if ((line = csvReader.readNext()) != null) {
            return line;
        } else {
            throw new Exception("ERROR: No se pudo leer la data de last.csv");
        }
    }

}
