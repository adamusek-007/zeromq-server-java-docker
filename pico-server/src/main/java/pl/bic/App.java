package pl.bic;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.sql.Connection;
import java.sql.DriverManager;                                       
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

public class App {
    private static String[] messageTypes = { "hell", "temp", "humi", "pres", "alti", "arqa", "gpsl", "dstl", "dstu",
            "brgh" };
    private static HashMap<String, String> messageTypesExplaintations;
    private static final Pattern HEXADECIMAL_PATTERN = Pattern.compile("^[0-9A-F]{5}$");

    public static void main(String[] args) {
        try {
            String jdbcUrl = "jdbc:mysql://" + System.getenv("DB_HOST") + ":" + System.getenv("DB_PORT") + "/" + System.getenv("DB_NAME");
            String jdbcUser = System.getenv("DB_USER");
            String jdbcPassword = System.getenv("DB_PASSWORD");
            Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);

            Statement createTable = connection.createStatement();
 			createTable.executeUpdate("CREATE TABLE IF NOT EXISTS `jajaj`(id INT AUTO_INCREMENT PRIMARY KEY);");

            Statement statment = connection.createStatement();
            ResultSet resultSet = statment.executeQuery("SHOW TABLES;");
            while(resultSet.next()){  
                System.out.println(resultSet.getString(1));  
            }  
        } catch (Exception exception) {
            System.out.println(exception);
        }
        // createHashMap();
        try (ZContext context = new ZContext()) {
            Socket socket = context.createSocket(SocketType.PULL);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr();
                System.out.println("Received message: " + message);
                parseMessage(message);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // private static void createHashMap() {
    //     messageTypesExplaintations.put("hell", "Hello - introduction");
    //     messageTypesExplaintations.put("temp", "Temperature");
    //     messageTypesExplaintations.put("humi", "Humidity");
    //     messageTypesExplaintations.put("pres", "Pressure");
    //     messageTypesExplaintations.put("alti", "Altitude");
    //     messageTypesExplaintations.put("arqa", "Air Quality");
    //     messageTypesExplaintations.put("gpsl", "GPS Location");
    //     messageTypesExplaintations.put("dstl", "Distance from laser sensor");
    //     messageTypesExplaintations.put("dstu", "Distance from ultrasonic sensor");
    //     messageTypesExplaintations.put("brgh", "Brightness");
    // }

    private static void parseMessage(String message) {
        String[] messageParts = message.split(":");
        if (messageParts.length == 4) {
            String deviceId = messageParts[0];
            String messageType = messageParts[1];
            String sensorIdStr = messageParts[2];
            String sensorValueStr = messageParts[3];
            boolean deviceIdStrValid = validateDeviceIdStr(deviceId);
            boolean messageTypeStrValid = validateMessageTypeStr(messageType);
            boolean sensorIdStrValid = validateSensorIdStr(sensorIdStr);
            boolean sensorValueStrValid = validateSensorValueStr(sensorValueStr);
            if (deviceIdStrValid & messageTypeStrValid & sensorIdStrValid & sensorValueStrValid) {
                int sensorId = Integer.valueOf(sensorIdStr);
                float sensorValue = Float.valueOf(sensorValueStr);
                // insertDataToDatabase(deviceId, messageType, sensorId, sensorValue);
            }
        } else if (messageParts.length == 2) {
            // String deviceIdStr = messageParts[0];
            // String messageTypeStr = messageParts[1];
            // if (!isHexadecimal(deviceIdStr)) {
            //     printErrorInfo(message);
            // }

        } else {
            printErrorInfo(message);
        }
    }

    private static void insertDataToDatabase(String deviceId, String messageType, int sensorId, float sensorValue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertDataToDatabase'");
    }

    private static boolean validateSensorValueStr(String sensorValueStr) {
        try {
            Float.valueOf(sensorValueStr);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static boolean validateSensorIdStr(String sensorIdStr) {
        try {
            Integer.valueOf(sensorIdStr);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static boolean validateMessageTypeStr(String messageTypeStr) {
        for (String messageType : messageTypes) {
            if (messageType.equals(messageTypeStr)) {
                return true;
            }
        }
        return false;
    }

    private static boolean validateDeviceIdStr(String deviceIdStr) {
        return isHexadecimal(deviceIdStr);
    }

    private static void printErrorInfo(String message) {
        System.out.println("Nieprawidłowa struktura wiadomości");
        System.out.println("Zawartosc wiadomosci: " + message);
    }

    private static boolean isHexadecimal(String input) {
        final Matcher matcher = HEXADECIMAL_PATTERN.matcher(input);
        return matcher.matches();
    }
}
