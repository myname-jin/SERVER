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
import java.net.Socket;

public class RegisterHandler {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;

    public RegisterHandler(Socket socket, BufferedReader in, BufferedWriter out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    public void handle(String message) throws IOException {
        if (!message.startsWith("REGISTER:")) return;

        String[] parts = message.substring("REGISTER:".length()).split(",");
        if (parts.length != 6) {
            out.write("REGISTER_FAIL:잘못된 형식\n");
            out.flush();
            return;
        }

        String userId = parts[0];
        String password = parts[1];
        String name = parts[2];
        String studentId = parts[3];
        String department = parts[4];
        String role = parts[5];

        // 로그인 파일 저장
        String loginFile = role.equals("admin") ? "src/main/resources/ADMIN_LOGIN.txt" : "src/main/resources/USER_LOGIN.txt";
        try (BufferedWriter loginWriter = new BufferedWriter(new FileWriter(loginFile, true))) {
            loginWriter.write(userId + "," + password);
            loginWriter.newLine();
        }

        // 전체 사용자 정보 저장
        try (BufferedWriter infoWriter = new BufferedWriter(new FileWriter("src/main/resources/USER_INFO.txt", true))) {
            infoWriter.write(studentId + "," + password + "," + name + "," + department + "," + role);
            infoWriter.newLine();
        }

        out.write("REGISTER_SUCCESS\n");
        out.flush();
    }
}