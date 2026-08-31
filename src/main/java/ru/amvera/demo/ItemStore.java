package ru.amvera.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemStore {
    private final ObjectMapper mapper;
    private final Path dataFile;

    public ItemStore(ObjectMapper mapper) {
        this.mapper = mapper;
        String configured = System.getenv("DATA_DIR");
        String dataDir = configured != null ? configured : System.getenv("AMVERA") != null ? "/data" : "data";
        this.dataFile = Path.of(dataDir, "items.json");
    }

    public Path dataFile() {
        return dataFile;
    }

    public synchronized List<Item> read() throws IOException {
        Files.createDirectories(dataFile.getParent());
        if (Files.notExists(dataFile)) Files.writeString(dataFile, "[]");
        return mapper.readValue(dataFile.toFile(), new TypeReference<>() {});
    }

    public synchronized Item create(String name) throws IOException {
        List<Item> items = read();
        long id = items.stream().mapToLong(Item::id).max().orElse(0) + 1;
        Item item = new Item(id, name);
        items.add(item);
        mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), items);
        return item;
    }

    public synchronized boolean delete(long id) throws IOException {
        List<Item> items = read();
        List<Item> next = new ArrayList<>(items.stream().filter(item -> item.id() != id).toList());
        if (next.size() == items.size()) return false;
        mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), next);
        return true;
    }
}
