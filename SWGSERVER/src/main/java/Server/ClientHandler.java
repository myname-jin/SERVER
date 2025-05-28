/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import Client.FileSyncManager;
import java.io.*;
import java.net.Socket;

import Client.RegisterHandler;     // 서버 쪽 핸들러
import Client.UserInfoHandler;     // 서버 쪽 핸들러

public class ClientHandler extends Thread {

    private final Socket socket;
    private final SessionManager sessionManager;
    private BufferedReader in;
    private BufferedWriter out;
    private String userId = null;

    public ClientHandler(Socket socket, SessionManager sessionManager) {
        this.socket = socket;
        this.sessionManager = sessionManager;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("스트림 초기화 실패: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                // ─── 회원가입 처리 ───────────────────────────────────
                if (msg.startsWith("REGISTER:")) {
                    RegisterHandler regHandler = new RegisterHandler(out);
                    regHandler.handle(msg);
                    continue;
                }

                // ─── 사용자 정보 요청 처리 ────────────────────────────
                if (msg.startsWith("INFO_REQUEST:")) {
                    UserInfoHandler infoHandler = new UserInfoHandler(socket, out);
                    infoHandler.handle(msg);
                    continue;
                }

                // ─── 로그인 처리 ─────────────────────────────────────
                if (msg.startsWith("LOGIN:")) {
                    System.out.println("[서버] 로그인 요청: " + msg);
                    String[] parts = msg.substring("LOGIN:".length()).split(",");
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
                    System.out.println("[서버] 로그인 검증 결과: " + valid);
                    if (!valid) {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: FAIL");
                        continue;
                    }

                    if ("admin".equalsIgnoreCase(role)) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: LOGIN_SUCCESS (admin)");
                        continue;
                    }

                    SessionManager.PendingClient pending
                            = new SessionManager.PendingClient(socket, userId, out);
                    SessionManager.LoginDecision result
                            = sessionManager.tryLogin(userId, pending);

                    if (result == SessionManager.LoginDecision.OK) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: LOGIN_SUCCESS (user)");
                    } else if (result == SessionManager.LoginDecision.WAIT) {
                        out.write("WAIT");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: WAIT (queued)");
                    } else {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: FAIL");
                    }
                    continue;
                }
                // ─── 텍스트 파일 동기화 처리 ──────────────────────────
                if (msg.startsWith("FILE_UPDATE:")) {
                    String filename = msg.substring("FILE_UPDATE:".length()).trim();
                    StringBuilder content = new StringBuilder();
                    String line;
                    while (!(line = in.readLine()).equals("<<EOF>>")) {
                        content.append(line).append("\n");
                    }

                    FileSyncManager manager = new FileSyncManager();
                    try {
                        manager.updateFile(filename, content.toString());
                        System.out.println("[서버] 파일 동기화 완료: " + filename);
                    } catch (IOException e) {
                        System.err.println("[서버] 파일 동기화 실패: " + filename);
                        e.printStackTrace();
                    }
                    continue;
                }
                // ─── 로그아웃 처리 ────────────────────────────────────
                if (msg.startsWith("LOGOUT:")) {
                    String logoutId = msg.substring("LOGOUT:".length()).trim();
                    sessionManager.logout(logoutId);
                    System.out.println("[서버] 로그아웃 처리: " + logoutId);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("[서버] 오류: " + e.getMessage());
        } finally {
            try {
                if (userId != null) {
                    sessionManager.logout(userId);
                    System.out.println("[서버] 세션 정리: " + userId);
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("[서버] 종료 오류: " + e.getMessage());
            }
        }
    }

    private boolean validateLogin(String userId, String password, String role) {
        String filePath = role.equalsIgnoreCase("admin")
                ? "src/main/resources/ADMIN_LOGIN.txt"
                : "src/main/resources/USER_LOGIN.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2
                        && parts[0].trim().equals(userId)
                        && parts[1].trim().equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("[서버] 로그인 검증 오류: " + e.getMessage());
        }
        return false;
    }
}
