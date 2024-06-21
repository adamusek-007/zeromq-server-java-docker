package pico.server;

import java.sql.Connection;
import java.sql.DriverManager;                                       
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

public class App {
    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            Connection conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/test", "sa", "");

            Socket socket = context.createSocket(SocketType.PULL);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                String message = socket.recvStr();
                System.out.println("Received message: " + message);

                // Process the received message here
                saveToDatabase(conn, message);

                // Simulate some processing time
                Thread.sleep(1000);
            }
            conn.close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveToDatabase(Connection conn, String message) throws SQLException {
        String[] data = message.split(",");
        String sql = "INSERT INTO sensor_data (temperature, humidity, pressure, air_quality, " +
                "laser_distance, ultrasonic_distance, lightness) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setDouble(1, Double.parseDouble(data[2]));
        pstmt.setDouble(2, Double.parseDouble(data[3]));
        pstmt.setDouble(3, Double.parseDouble(data[4]));
        pstmt.setDouble(4, Double.parseDouble(data[5]));
        pstmt.setDouble(5, Double.parseDouble(data[6]));
        pstmt.setDouble(6, Double.parseDouble(data[7]));
        pstmt.setDouble(7, Double.parseDouble(data[8]));
        pstmt.executeUpdate();
    }
}
