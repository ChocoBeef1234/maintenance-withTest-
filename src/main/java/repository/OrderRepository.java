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
import main.java.config.OrderConstants;
import main.java.model.OrderLine;
import main.java.model.OrderRecord;

public class OrderRepository {

    public List<OrderRecord> findAll() throws IOException {
        List<OrderRecord> list = new ArrayList<>();
        File file = new File(FilePaths.ORDER);
        if (!file.exists()) return list;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                OrderRecord record = parse(line);
                if (record != null) list.add(record);
            }
        }
        return list;
    }

    public OrderRecord findByNumber(String orderNumber) throws IOException {
        File file = new File(FilePaths.ORDER);
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                OrderRecord record = parse(line);
                if (record != null && record.getOrderNumber().equals(orderNumber)) {
                    return record;
                }
            }
        }
        return null;
    }

    public boolean add(OrderRecord record) throws IOException {
        File file = new File(FilePaths.ORDER);
        if (!file.exists()) return false;
        if (findByNumber(record.getOrderNumber()) != null) return false;
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println(toLine(record));
        }
        return true;
    }

    public boolean update(String orderNumber, OrderRecord updatedRecord) throws IOException {
        File file = new File(FilePaths.ORDER);
        File tmp = new File(FilePaths.ORDER + OrderConstants.TEMP_FILE_EXTENSION);
        if (!file.exists()) return false;
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             PrintWriter writer = new PrintWriter(new FileWriter(tmp))) {
            String line;
            while ((line = reader.readLine()) != null) {
                OrderRecord record = parse(line);
                if (record != null && record.getOrderNumber().equals(orderNumber)) {
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

    public boolean delete(String orderNumber) throws IOException {
        File file = new File(FilePaths.ORDER);
        File tmp = new File(FilePaths.ORDER + OrderConstants.TEMP_FILE_EXTENSION);
        if (!file.exists()) return false;
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file));
             PrintWriter writer = new PrintWriter(new FileWriter(tmp))) {
            String line;
            while ((line = reader.readLine()) != null) {
                OrderRecord record = parse(line);
                if (record != null && record.getOrderNumber().equals(orderNumber)) {
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

    private OrderRecord parse(String line) {
        String[] parts = line.split(OrderConstants.FIELD_DELIMITER_REGEX);
        if (parts.length < OrderConstants.MIN_PARTS_LENGTH) return null;
        String number = parts[0];
        String date = parts[1];
        List<OrderLine> lines = new ArrayList<>();
        for (int i = OrderConstants.ORDER_LINE_START_INDEX; i < parts.length - 1; i += OrderConstants.ORDER_LINE_FIELD_COUNT) {
            if (i + OrderConstants.ORDER_LINE_FIELD_COUNT - 1 >= parts.length) break;
            String code = parts[i];
            int qty = Integer.parseInt(parts[i + 1]);
            double subtotal = Double.parseDouble(parts[i + 2]);
            lines.add(new OrderLine(code, qty, subtotal));
        }
        double total = Double.parseDouble(parts[parts.length - 1]);
        return new OrderRecord(number, date, lines, total);
    }

    private String toLine(OrderRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getOrderNumber()).append(OrderConstants.FIELD_DELIMITER)
          .append(record.getDate()).append(OrderConstants.FIELD_DELIMITER);
        for (OrderLine line : record.getLines()) {
            sb.append(line.getItemCode()).append(OrderConstants.FIELD_DELIMITER)
              .append(line.getQuantity()).append(OrderConstants.FIELD_DELIMITER)
              .append(line.getSubtotal()).append(OrderConstants.FIELD_DELIMITER);
        }
        sb.append(record.getTotal());
        return sb.toString();
    }
}

