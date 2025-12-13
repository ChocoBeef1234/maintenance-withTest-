package main.java.repository;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import main.java.config.FilePaths;
import main.java.config.ItemCodePrefix;
import main.java.model.ItemRecord;

public class ItemRepository {


    public List<ItemRecord> findAll() throws IOException {
        List<ItemRecord> items = new ArrayList<>();
        File file = new File(FilePaths.ITEM);
        if (!file.exists()) {
            return items;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ItemRecord record = parse(line);
                if (record != null) {
                    items.add(record);
                }
            }
        }
        return items;
    }

    public ItemRecord findByCode(String code) throws IOException {
        File file = new File(FilePaths.ITEM);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ItemRecord record = parse(line);
                if (record != null && record.getCode().equals(code)) {
                    return record;
                }
            }
        }
        return null;
    }

    public boolean add(ItemRecord r) throws IOException {
        if (findByCode(r.getCode()) != null) {
            return false; 
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(FilePaths.ITEM, true))) {
            writer.println(toLine(r));
        }
        return true;
    }
    
    public boolean update(String oldCode, ItemRecord updatedRecord) throws IOException {
        File file = new File(FilePaths.ITEM);
        File tmp = new File(FilePaths.ITEM + ".tmp");
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             PrintWriter writer = new PrintWriter(new FileWriter(tmp))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ItemRecord r = parse(line);
                if (r != null && r.getCode().equals(oldCode)) {
                    // Write the updated record instead of the old one
                    writer.println(toLine(updatedRecord));
                    found = true;
                    continue;
                }
                writer.println(line);
            }
        }

        if (found) {
            file.delete();
            tmp.renameTo(file);
        } else {
            tmp.delete();
        }
        return found;
    }
    
    public boolean delete(String code) throws IOException {
        File file = new File(FilePaths.ITEM);
        File tmp = new File(FilePaths.ITEM + ".tmp");
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             PrintWriter writer = new PrintWriter(new FileWriter(tmp))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ItemRecord r = parse(line);
                if (r != null && r.getCode().equals(code)) {
                    found = true;
                    continue;
                }
                writer.println(line);
            }
        }
        if (found) {
            file.delete();
            tmp.renameTo(file);
        } else {
            tmp.delete();
        }
        return found;
    }

    private ItemRecord parse(String line) {
        String[] info = line.split("\\|\\|");
        if (info.length < 6) return null;
        String code = info[0];
        String desc = info[1];
        
        // Original logic for parsing price and quantity
        double price = Double.parseDouble(info[2]);
        int qty = Integer.parseInt(info[3]);
        
        // REFACTORED: Use ItemCodePrefix constants instead of "M" and "S"
        if (code.startsWith(ItemCodePrefix.MEDICINE)) { 
            return new ItemRecord(code, desc, price, qty, ItemRecord.Type.MEDICINE, info[4], Integer.parseInt(info[5]));
        } else if (code.startsWith(ItemCodePrefix.SUPPLEMENT)) {
            return new ItemRecord(code, desc, price, qty, ItemRecord.Type.SUPPLEMENT, info[4], Integer.parseInt(info[5]));
        }
        return null;
    }

    private String toLine(ItemRecord r) {
        return r.getCode() + "||" + r.getDescription() + "||" + r.getPrice() + "||" + r.getQuantity() + "||" + r.getExtra1() + "||" + r.getExtra2();
    }
}