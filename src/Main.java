import java.sql.*;
import java.util.Scanner;

class Main {
    public static final String S = ".";
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "Admin@123";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM");
                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservation");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservation");
                System.out.println("5. Delete Reservation");
                System.out.println("0. Exit");
                System.out.print("\nChoose an Option: ");
                int choice = sc.nextInt();
                sc.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        reserveRoom(connection, sc);
                        break;
                    case 2:
                        viewReservation(connection);
                        break;
                    case 3:
                        getRoomNumber(connection, sc);
                        break;
                    case 4:
                        updateReservation(connection, sc);
                        break;
                    case 5:
                        deleteReservation(connection, sc);
                        break;
                    case 0:
                        exit();
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid Option!");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reserveRoom(Connection connection, Scanner sc) {
        try {
            System.out.print("Enter guest name: ");
            String guestName = sc.nextLine();
            System.out.print("Enter Room Number: ");
            int roomNumber = sc.nextInt();
            sc.nextLine(); // Consume newline
            System.out.print("Enter Contact Number: ");
            String contactNumber = sc.nextLine();

            String sql = "INSERT INTO reservations (guest_name, room_number, contact_number) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, guestName);
                preparedStatement.setInt(2, roomNumber);
                preparedStatement.setString(3, contactNumber);
                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Reservation Successful!");
                } else {
                    System.out.println("Reservation Failed!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewReservation(Connection connection) {
        String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date FROM reservations";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Current Reservations:");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");
            System.out.println("| Reservation ID | Guest           | Room Number   | Contact Number       | Reservation Date        |");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");

            while (resultSet.next()) {
                int reservationId = resultSet.getInt("reservation_id");
                String name = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_number");
                String contact = resultSet.getString("contact_number");
                String reservationDate = resultSet.getString("reservation_date");

                System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-19s   |\n",
                        reservationId, name, roomNumber, contact, reservationDate);
            }

            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void getRoomNumber(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter Reservation ID: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            System.out.print("Enter Guest Name: ");
            String guestName = scanner.nextLine();

            String sql = "SELECT room_number FROM reservations WHERE reservation_id = ? AND guest_name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, reservationId);
                preparedStatement.setString(2, guestName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int roomNumber = resultSet.getInt("room_number");
                        System.out.println("Room Number for Reservation ID " + reservationId + " and guest " + guestName + " is: " + roomNumber);
                    } else {
                        System.out.println("Reservation not found for the given ID and guest name.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateReservation(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter Reservation ID to update: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            System.out.print("Enter new Guest Name: ");
            String newGuestName = scanner.nextLine();
            System.out.print("Enter new Room Number: ");
            int newRoomNumber = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            System.out.print("Enter new Contact Number: ");
            String newContactNumber = scanner.nextLine();

            String sql = "UPDATE reservations SET guest_name = ?, room_number = ?, contact_number = ? WHERE reservation_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, newGuestName);
                preparedStatement.setInt(2, newRoomNumber);
                preparedStatement.setString(3, newContactNumber);
                preparedStatement.setInt(4, reservationId);

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Reservation updated successfully!");
                } else {
                    System.out.println("Reservation update failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteReservation(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter reservation ID to delete: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            String sql = "DELETE FROM reservations WHERE reservation_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, reservationId);

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Reservation deleted successfully!");
                } else {
                    System.out.println("Reservation deletion failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean reservationExists(Connection connection, int reservationId) {
        String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, reservationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void exit() throws InterruptedException {
        char[] s = {'E', 'x', 'i', 't', 'i', 'n', 'g'};
        for (char c : s) {
            System.out.print(c);
            Thread.sleep(300);
        }
        for (int j = 0; j < 3; j++) {
            System.out.print(S);
            Thread.sleep(400);
        }
        System.out.println("\nThanks For Using Hotel Reservation Application!");
    }
}
