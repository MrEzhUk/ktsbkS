package kts.dev.ktsbk.server.csv;

import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;

public class VirtualCsvWriter implements Closeable, Flushable {
    public final CSVPrinter printer;
    private final ByteArrayOutputStream outputStream;
    private final OutputStreamWriter w;
    private final String filename;
    public VirtualCsvWriter(String filename, String... header) throws IOException {
        this.filename = filename;
        outputStream = new ByteArrayOutputStream();
        w = new OutputStreamWriter(outputStream);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(header)
                .build();
        printer = new CSVPrinter(w, csvFormat);
    }

    @Override
    public void close() throws IOException {
        w.close();
        printer.close();
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
        w.flush();
        printer.flush();
    }

    public FileUpload getFileUpload() throws IOException {
        this.flush();
        return FileUpload.fromData(new ByteArrayInputStream(outputStream.toByteArray()), filename);
    }
}
