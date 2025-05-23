/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Client;

import org.junit.jupiter.api.*;
import java.io.*;

public class RegisterHandlerTest {

    @Test
    public void testValidRegister() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter out = new BufferedWriter(sw);

        RegisterHandler.processRegister("REGISTER:testuser,testpw,user", out);
        out.flush();

        String result = sw.toString().trim();
        Assertions.assertEquals("REGISTER_SUCCESS", result);
    }

    @Test
    public void testInvalidRegisterFormat() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter out = new BufferedWriter(sw);

        RegisterHandler.processRegister("REGISTER:onlyonevalue", out);
        out.flush();

        String result = sw.toString().trim();
        Assertions.assertEquals("REGISTER_FAIL", result);
    }
}