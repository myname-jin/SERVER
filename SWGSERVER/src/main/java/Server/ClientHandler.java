/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */

import Client.RegisterHandler;
import Client.UserInfoHandler;
import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final SessionManager sessionManager;
    private BufferedReader in;
    private BufferedWriter out;
    private String userId = null;
    private boolean isWaiting = false;

    public ClientHandler(Socket socket, SessionManager sessionManager) {
        this.socket = socket;
        this.sessionManager = sessionManager;

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("❌ 스트림 초기화 실패: " + e.getMessage());
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

                // ✅ 회원가입 처리
                if (msg.startsWith("REGISTER:")) {
                    RegisterHandler registerHandler = new RegisterHandler(socket, in, out);
                    registerHandler.handle(msg);
                    continue;
                }

                // ✅ 사용자 정보 요청 처리
                if (msg.startsWith("INFO_REQUEST:")) {
                    UserInfoHandler infoHandler = new UserInfoHandler(socket, out);
                    infoHandler.handle(msg);
                    continue;
                }

                // ✅ 로그인 처리
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

                    boolean valid = validateLogin(userId, password, role);
                    if (!valid) {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                        continue;
                    }

                    if ("admin".equalsIgnoreCase(role)) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("👑 관리자 로그인: " + userId);
                        continue;
                    }

                    SessionManager.PendingClient pending = new SessionManager.PendingClient(socket, userId, out);
                    SessionManager.LoginDecision result = sessionManager.tryLogin(userId, pending);

                    if (result == SessionManager.LoginDecision.OK) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("✅ 사용자 로그인: " + userId);
                    } else if (result == SessionManager.LoginDecision.WAIT) {
                        out.write("WAIT");
                        out.newLine();
                        out.flush();
                        System.out.println("⌛ 대기 상태 진입: " + userId);
                        isWaiting = true;
                    } else {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                    }
                    continue;
                }

                // ✅ 로그아웃 처리
                if (msg.startsWith("LOGOUT:")) {
                    String logoutId = msg.substring(7).trim();
                    sessionManager.logout(logoutId);
                    System.out.println("📤 로그아웃 처리됨: " + logoutId);
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("❌ 오류: " + e.getMessage());
        } finally {
            try {
                if (userId != null) {
                    sessionManager.logout(userId);
                    System.out.println("🧹 세션 정리됨: " + userId);
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("❌ 종료 오류: " + e.getMessage());
            }
        }
    }

    private boolean validateLogin(String userId, String password, String role) {
        String filePath = role.equals("admin") ? "src/main/resources/ADMIN_LOGIN.txt" : "src/main/resources/USER_LOGIN.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].trim().equals(userId) && parts[1].trim().equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("❌ 로그인 검증 오류: " + e.getMessage());
        }
        return false;
    }
}