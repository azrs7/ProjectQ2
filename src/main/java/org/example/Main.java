package org.example;
import java.sql.*;
import java.util.Scanner;

        class ProjectQ2 {
            static double balance = 0.0;
            static Connection conn;

            public static void main(String[] args) {
                try {
                    Class.forName("org.sqlite.JDBC");

                    connectDatabase();
                    Scanner input = new Scanner(System.in);
                    int choice;

                    while (true) {
                        System.out.println("\n==Transaction Menu==");
                        System.out.println("1. Debit\n2. Credit\n3. History\n4. Savings\n5. Credit Loan\n6. Deposit Interest Predictor\n7. Logout");
                        System.out.print("> ");
                        choice = input.nextInt();
                        input.nextLine();

                        switch (choice) {
                            case 1:
                                handleDebit(input);
                                break;
                            case 2:
                                handleCredit(input);
                                break;
                            case 3:
                                showHistory();
                                break;
                            case 7:
                                System.out.println("Logging out...");
                                disconnectDatabase();
                                input.close();
                                return;
                            default:
                                System.out.println("Feature under development or invalid choice.");
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("SQLite JDBC driver not found. Add the driver to your project.");
                    e.printStackTrace();
                } catch (SQLException e) {
                    System.err.println("Database error occurred.");
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("An unexpected error occurred.");
                    e.printStackTrace();
                }
            }

            public static void handleDebit(Scanner input) {
                System.out.println("==Debit==");
                System.out.print("Enter Debit Amount: ");
                double amount = input.nextDouble();
                input.nextLine();
                System.out.print("Enter description: ");
                String desc = input.nextLine();

                if (amount <= 0 || amount > 1000000 || desc.length() > 100) {
                    System.out.println("Invalid input.");
                    return;
                }

                if (amount > balance) {
                    System.out.println("Insufficient balance for this debit.");
                    return;
                }

                balance -= amount;
                saveTransaction("Debit", amount, desc);
                System.out.println("Debit successfully recorded! Current balance: " + balance);
            }

            public static void handleCredit(Scanner input) {
                System.out.println("==Credit==");
                System.out.print("Enter Credit Amount: ");
                double amount = input.nextDouble();
                input.nextLine();
                System.out.print("Enter description: ");
                String desc = input.nextLine();

                if (amount <= 0 || desc.length() > 100) {
                    System.out.println("Invalid input.");
                    return;
                }

                balance += amount;
                saveTransaction("Credit", amount, desc);
                System.out.println("Credit successfully recorded! Current balance: " + balance);
            }

            public static void showHistory() {
                System.out.println("==Transaction History==");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM transactions ORDER BY id DESC")) {

                    System.out.println("ID | Type   | Amount  | Description");
                    System.out.println("-----------------------------------");
                    while (rs.next()) {
                        System.out.printf("%-2d | %-6s | %7.2f | %s\n",
                                rs.getInt("id"),
                                rs.getString("type"),
                                rs.getDouble("amount"),
                                rs.getString("description"));
                    }
                } catch (SQLException e) {
                    System.err.println("Error retrieving transaction history:");
                    e.printStackTrace();
                }
            }

            public static void connectDatabase() throws SQLException {
                conn = DriverManager.getConnection("jdbc:sqlite:transactions.db");

                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    amount REAL NOT NULL,
                    description TEXT NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                );
                """);

                    ResultSet rs = stmt.executeQuery("""
                SELECT SUM(CASE WHEN type='Credit' THEN amount ELSE -amount END) AS balance 
                FROM transactions
                """);
                    if (rs.next()) {
                        balance = rs.getDouble("balance");
                    }
                }
            }

            public static void saveTransaction(String type, double amount, String description) {
                String sql = "INSERT INTO transactions(type, amount, description) VALUES(?,?,?)";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, type);
                    ps.setDouble(2, amount);
                    ps.setString(3, description);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Error saving transaction:");
                    e.printStackTrace();
                }
            }

            public static void disconnectDatabase() throws SQLException {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("Database connection closed.");
                }
            }
        }
