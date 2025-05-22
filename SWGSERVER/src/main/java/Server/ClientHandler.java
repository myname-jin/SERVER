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
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private SessionManager sessionManager;
    private LoginProcessor loginProcessor;
    private String userId = null;
    private boolean isWaiting = false;

    public ClientHandler(Socket socket, SessionManager sessionManager) {
        this.socket = socket;
        this.sessionManager = sessionManager;
        this.loginProcessor = new LoginProcessor(sessionManager);

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (isWaiting) {
                    Thread.sleep(500);
                    continue;
                }

                String msg = in.readLine();
                if (msg == null) break;

                if (msg.startsWith("LOGIN:")) {
                    String[] parts = msg.substring(6).split(",");
                    if (parts.length < 3) {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                        continue;
                    }

                    userId = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim();

                    boolean valid = loginProcessor.validateLogin(userId, password, role);
                    if (!valid) {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                        continue;
                    }

                    if (role.equals("admin")) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("👑 관리자 " + userId + " 접속 허용됨");
                        continue;
                    }

                    SessionManager.LoginDecision result = loginProcessor.tryUserLogin(userId, socket, out);

                    if (result == SessionManager.LoginDecision.OK) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("✅ 사용자 " + userId + " 로그인됨");
                    } else if (result == SessionManager.LoginDecision.WAIT) {
                        out.write("WAIT");
                        out.newLine();
                        out.flush();
                        System.out.println("⌛ 사용자 " + userId + " 대기 중");
                        isWaiting = true;
                    } else {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                    }

                } else if (msg.startsWith("LOGOUT:")) {
                    String logoutUser = msg.substring(7).trim();
                    System.out.println("📤 로그아웃: " + logoutUser);
                    loginProcessor.logout(logoutUser);
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("❌ 연결 오류: " + e.getMessage());
        } finally {
            try {
                if (userId != null) {
                    loginProcessor.logout(userId);
                    System.out.println("🧹 세션 정리됨: " + userId);
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("❌ 종료 정리 실패: " + e.getMessage());
            }
        }
    }
}