/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */

import java.io.*;

public class RegisterHandler {
    public static void processRegister(String msg, BufferedWriter out) throws IOException {
        String[] parts = msg.substring(9).split(",");
        if (parts.length == 3) {
            String newId = parts[0].trim();
            String newPw = parts[1].trim();
            String role = parts[2].trim().toLowerCase();

            String filename = role.equals("admin") ?
                    "src/main/resources/ADMIN_LOGIN.txt" :
                    "src/main/resources/USER_LOGIN.txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
                writer.write(newId + "," + newPw);
                writer.newLine();
            }

            out.write("REGISTER_SUCCESS");
        } else {
            out.write("REGISTER_FAIL");
        }
        out.newLine();
        out.flush();
    }
}
