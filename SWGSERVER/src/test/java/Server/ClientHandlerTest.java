/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Server;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientHandlerTest {

    @BeforeAll
    static void startServer() {
        new Thread(() -> {
            try {
                ServerMain.main(null); // 백그라운드에서 서버 실행
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 서버가 포트 열 준비할 시간 주기
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
    }

    @Test
    void testRegisterResponseFromServer() throws IOException {
        try (Socket socket = new Socket("127.0.0.1", 5000);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.write("REGISTER:junitUser,junitPw,user");
            out.newLine();
            out.flush();

            String response = in.readLine();
            assertEquals("REGISTER_SUCCESS", response);
        }
    }
}