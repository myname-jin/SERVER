/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

/**
 *
 * @author adsd3
 */
import java.io.*;
import java.util.*;

public class RuleHandler {
    private static final String RULE_FILE = "src/main/resources/rules.txt";

    public static void handleGetRules(BufferedWriter out) throws IOException {
        StringBuilder sb = new StringBuilder("RULES:");
        try (BufferedReader reader = new BufferedReader(new FileReader(RULE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    sb.append(line.trim()).append("\n");
                }
            }
        }
        out.write(sb.toString().trim());
        out.newLine();
        out.flush();
    }

    public static void handleAddRule(String msg, BufferedWriter out) throws IOException {
        String newRule = msg.substring("ADD_RULE:".length()).trim();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RULE_FILE, true))) {
            writer.write(newRule);
            writer.newLine();
        }
        out.write("ADD_RULE_SUCCESS");
        out.newLine();
        out.flush();
    }

    public static void handleDeleteRules(String msg, BufferedWriter out) throws IOException {
        String data = msg.substring("DELETE_RULES:".length());
        List<String> toDelete = Arrays.asList(data.split("\\|"));

        List<String> remaining = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(RULE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!toDelete.contains(line.trim())) {
                    remaining.add(line.trim());
                }
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RULE_FILE))) {
            for (String rule : remaining) {
                writer.write(rule);
                writer.newLine();
            }
        }

        out.write("DELETE_RULES_SUCCESS");
        out.newLine();
        out.flush();
    }
}