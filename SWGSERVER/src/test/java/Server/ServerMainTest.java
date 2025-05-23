/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Server;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class ServerMainTest {

    @Test
    void testServerAcceptsConnection() throws Exception {
        Thread serverThread = new Thread(() -> {
            try {
                ServerMain.main(null); // 서버를 백그라운드에서 실행
            } catch (Exception ignored) {}
        });
        serverThread.start();

        // 서버가 뜰 시간을 기다림
        Thread.sleep(1000);

        try (Socket socket = new Socket("127.0.0.1", 5000)) {
            assertTrue(socket.isConnected());
        }
    }
}