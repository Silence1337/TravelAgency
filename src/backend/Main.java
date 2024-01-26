package backend;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static backend.Model.*;

public class Main {
    public static void main(String[] args) throws Exception {


        //Model model = new Model();
        Model.initConnection();
        //Model.printSortedFlights("ticketsDown");
        String departureCountryName = "Tel Aviv (TLV)";
        String arrivalCountryName = "Moscow (SVO)";
        Date departureDate = new Date();


        LocalDateTime arrivalDateTime = LocalDateTime.now();



        String[][] matchingRows = Model.checkingBackFlihgts(departureCountryName, arrivalCountryName , arrivalDateTime);

        for (String[] row : matchingRows) {
            for (String value : row) {
                System.out.print(value + "\t");
            }
            System.out.println();

        //Model model = new Model();


        }
    }
}