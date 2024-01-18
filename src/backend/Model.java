package backend;
import frontend.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

public class Model {
    private static DbFunctions db;
    private static Connection conn;
    private static int userId = -1;

    public Model() {
        String password = "";
        try{
            password = new String(Files.readAllBytes(Paths.get("password.txt")), StandardCharsets.UTF_8);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        db = new DbFunctions();
        conn = db.connection("TravelAgency","postgres",password);

        db.initDB(conn);

        View.init();    //Initializing GUI
    }


    //Этот метод должен возвращать -1 если креды невалидные, 0 если они валидные и пользователь не админ и 1 если креды валидные и это админ
    public static int validateCredentials(String login, String password) {
        Statement statement;

        String user = String.format("SELECT * FROM users WHERE login = '%s' AND userpassword = '%s'", login, password);
        String admin = String.format("SELECT isAdmin FROM users WHERE login = '%s' AND userpassword = '%s'", login, password);
        boolean isAdmin = false;
        boolean isAuthCorrect = false;
        try{
            ResultSet resultSet;
            statement = conn.createStatement();
            resultSet = statement.executeQuery(user);
            if (resultSet.next()){
                isAuthCorrect = true;
                userId = resultSet.getInt(1);
            }
            resultSet = statement.executeQuery(admin);
            if (resultSet.next()){
                isAdmin = resultSet.getBoolean(1);
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        if(isAdmin) return 1;
        if(isAuthCorrect) return 0;
        return -1;
    }

    private static boolean isCorrectCreditCardDetails(String cardNumber, String expireDate, int cvv){
        return cardNumber.length() == 16 && expireDate.matches("\\d{2}/\\d{2}") && cvv > 99 && cvv < 1000;
    }

    private static String aesEncrypt(String element){
        try{
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKeySpec = new SecretKeySpec("bestProjectInTheWorld!!!".getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedElement = cipher.doFinal(element.getBytes());
            return DatatypeConverter.printHexBinary(encryptedElement);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static String aesDecrypt(String element){
        try{
            Cipher cipher =  Cipher.getInstance("AES");
            SecretKeySpec secretKeySpec = new SecretKeySpec("bestProjectInTheWorld!!!".getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] dectyptElement = cipher.doFinal(DatatypeConverter.parseHexBinary(element));
            return new String(dectyptElement);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static void addPaymentData(int id, String cardNumber, String expireDate, String cardName, String cvv){
        try{
            if(isCorrectCreditCardDetails(cardNumber, expireDate, Integer.parseInt(cvv))){
                String[] paymentData = new String[] {Integer.toString(id),
                        "'" + Model.aesEncrypt(cardNumber) + "'",
                        "'" + Model.aesEncrypt(expireDate) + "'",
                        "'" + Model.aesEncrypt(cardName) + "'",
                        "'" + Model.aesEncrypt(cvv) + "'"
                };
                db.insertRow(conn, "paymentData", paymentData);
            }
            else{
                System.out.println("Credit card details are not correct!");
            }

        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }


    }

    public static String[] getUserPaymentData(int id){
        Statement statement;
        String userPaymentDataQuery = String.format("SELECT * FROM paymentData WHERE fk_user_ID = %s", id);
        String[] userPaymentData = new String[5];
        try{
            ResultSet resultSet;
            statement = conn.createStatement();
            resultSet = statement.executeQuery(userPaymentDataQuery);

            while(resultSet.next()){
                userPaymentData[0] = Integer.toString(resultSet.getInt(1));
                userPaymentData[1] = Model.aesDecrypt(resultSet.getString(2));
                userPaymentData[2] = Model.aesDecrypt(resultSet.getString(3));
                userPaymentData[3] = Model.aesDecrypt(resultSet.getString(4));
                userPaymentData[4] = Model.aesDecrypt(resultSet.getString(5));
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        return userPaymentData;
    }
    public static int[][] getAllFlyghtSeats(int flightId){
        String flightQuery = "";
        Statement statement;
        int[][] allFlightSeats = null;
        try{
            flightQuery = String.format("SELECT flights.flight_ID, flights.fk_plane_ID, planes.rowsNumber, planes.columnsNumber FROM flights LEFT JOIN planes\n" +
                    "ON flights.fk_plane_id = plane_ID WHERE flights.flight_ID = %s", flightId);
            statement = conn.createStatement();;
            ResultSet resultSet = statement.executeQuery(flightQuery);
            resultSet.next();
            allFlightSeats = new int[resultSet.getInt("rowsnumber")][resultSet.getInt("columnsnumber")];
            initAllFlyghtSeats(flightId, allFlightSeats);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return allFlightSeats;
    }
    private static void initAllFlyghtSeats(int flyghtId, int[][] allFlightSeats){
        String ticketQuery = "";
        Statement statement;
        try{
            ticketQuery = String.format("SELECT fk_flight_ID, fk_user_ID, seatRow, seatColumn FROM tickets\n" +
                    "WHERE fk_flight_ID = %s", flyghtId);
            statement = conn.createStatement();
            ResultSet resultSet =statement.executeQuery(ticketQuery);
            for(int i = 0; i < allFlightSeats.length; i++){
                for (int j = 0; j < allFlightSeats[0].length; j++){
                    allFlightSeats[i][j] = -1;
                }
            }
            while(resultSet.next()){
                int seatRow = resultSet.getInt("seatrow") - 1;
                int seatColumn = resultSet.getInt("seatcolumn") - 1;
                if (resultSet.getInt("fk_user_id") == userId){
                    allFlightSeats[seatRow][seatColumn] = 1;
                }
                else{
                    allFlightSeats[seatRow][seatColumn] = 0;
                }
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    public void addFlight(String flightName, LocalDateTime departureTime, LocalDateTime arrivalTime, int departureCountryId, int arrivalCountryId, int planeId, int price) {

    }
}