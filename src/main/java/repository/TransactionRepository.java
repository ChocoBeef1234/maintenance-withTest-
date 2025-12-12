package main.java.repository;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import main.java.model.TransactionRecord;
import main.java.config.FilePaths;

public class TransactionRepository {

    public boolean add(TransactionRecord record) throws IOException {
        File file = new File(FilePaths.TRANSACTION);
        if (!file.exists()) return false;
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println(toLine(record));
        }
        return true;
    }

    public TransactionRecord findByOrder(String orderNumber) throws IOException {
        File file = new File(FilePaths.TRANSACTION);
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                TransactionRecord r = parse(line);
                if (r != null && r.getOrderNumber().equals(orderNumber)) {
                    return r;
                }
            }
        }
        return null;
    }

    public java.util.List<TransactionRecord> findAll() throws IOException {
        java.util.List<TransactionRecord> list = new java.util.ArrayList<>();
        File file = new File(FilePaths.TRANSACTION);
        if (!file.exists()) return list;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                TransactionRecord r = parse(line);
                if (r != null) list.add(r);
            }
        }
        return list;
    }

    public boolean delete(String orderNumber) throws IOException {
        File file = new File(FilePaths.TRANSACTION);
        File tmp = new File(FilePaths.TRANSACTION + ".tmp");
        if (!file.exists()) return false;
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             PrintWriter writer = new PrintWriter(new FileWriter(tmp))) {
            String line;
            while ((line = reader.readLine()) != null) {
                TransactionRecord r = parse(line);
                if (r != null && r.getOrderNumber().equals(orderNumber)) {
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

    private String toLine(TransactionRecord r) {
        return r.getOrderNumber() + "||" + r.getTotalPrice() + "||" + r.getDiscountPercent() + "||" +
                r.getDiscountAmount() + "||" + r.getTaxPercent() + "||" + r.getFinalPrice() + "||" +
                r.getField1() + "||" + r.getField2() + "||" + r.getMethod().name();
    }

    private TransactionRecord parse(String line) {
        String[] p = line.split("\\|\\|");
        if (p.length < 9) return null;
        String order = p[0];
        double total = Double.parseDouble(p[1]);
        double discPct = Double.parseDouble(p[2]);
        double discAmt = Double.parseDouble(p[3]);
        double tax = Double.parseDouble(p[4]);
        double finalPrice = Double.parseDouble(p[5]);
        String field1 = p[6];
        String field2 = p[7];
        // accept legacy or mixed-case method strings from file
        String methodRaw = p[8].trim().toUpperCase();
        TransactionRecord.Method method;
        try {
            method = TransactionRecord.Method.valueOf(methodRaw);
        } catch (IllegalArgumentException ex) {
            // unknown payment method in file; skip this record
            return null;
        }
        return new TransactionRecord(order, total, discPct, discAmt, tax, finalPrice, method, field1, field2);
    }
}

