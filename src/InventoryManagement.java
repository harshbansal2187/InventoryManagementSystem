
import java.sql.*;
import java.util.Scanner;

public class InventoryManagement {

    static final String URL = "jdbc:mysql://localhost:3306/retail_db";
    static final String USER = "root";
    static final String PASSWORD = "Harsh@2187";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            Connection con = DriverManager.getConnection(
                    URL, USER, PASSWORD);

            System.out.println("Database Connected Successfully!");

            int choice;

            do {
                System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert Product Records");
                System.out.println("2. Retrieve Products by Category");
                System.out.println("3. Update Product Stock");
                System.out.println("4. Search and Update Product Price");
                System.out.println("5. Display Inventory Value and Delete Product");
                System.out.println("6. Exit");

                System.out.print("Enter your choice: ");
                choice = sc.nextInt();

                switch (choice) {

                    case 1:
                        insertProducts(con);
                        break;

                    case 2:
                        retrieveProducts(con);
                        break;

                    case 3:
                        updateStock(con);
                        break;

                    case 4:
                        updatePrice(con);
                        break;

                    case 5:
                        inventoryAndDelete(con);
                        break;

                    case 6:
                        System.out.println("Thank you!");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 6);

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // (a) Insert at least three products
    static void insertProducts(Connection con) {

        String sql = "INSERT INTO Product VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {

                System.out.println("\nEnter Product " + i + " details:");

                System.out.print("Product ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Product Name: ");
                String name = sc.nextLine();

                System.out.print("Category: ");
                String category = sc.nextLine();

                System.out.print("Quantity: ");
                int quantity = sc.nextInt();

                System.out.print("Price: ");
                double price = sc.nextDouble();

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, category);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);

                try {
                    ps.executeUpdate();
                    System.out.println("Product inserted successfully!");

                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Product ID! Record not inserted.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (b) Conditional retrieval and sorting
    static void retrieveProducts(Connection con) {

        String sql = "SELECT * FROM Product " +
                     "WHERE category = ? AND quantity < 20 " +
                     "ORDER BY price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter category: ");
            sc.nextLine();
            String category = sc.nextLine();

            ps.setString(1, category);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                displayProduct(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (c) Update product stock
    static void updateStock(Connection con) {

        String sql = "UPDATE Product SET quantity = quantity + ? " +
                     "WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Product ID: ");
            int id = sc.nextInt();

            System.out.print("Enter quantity to add: ");
            int quantity = sc.nextInt();

            if (quantity <= 0) {
                System.out.println("Enter a positive quantity!");
                return;
            }

            ps.setInt(1, quantity);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Stock updated successfully!");
            else
                System.out.println("Product does not exist!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (d) Search and update product price
    static void updatePrice(Connection con) {

        System.out.print("Enter Product ID: ");
        int id = sc.nextInt();

        String select = "SELECT * FROM Product WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(select)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                System.out.println("Product Found:");
                displayProduct(rs);

                System.out.print("Enter new price: ");
                double price = sc.nextDouble();

                if (price <= 0) {
                    System.out.println("Price must be greater than zero!");
                    return;
                }

                String update = "UPDATE Product SET price = ? " +
                                "WHERE product_id = ?";

                try (PreparedStatement ups = con.prepareStatement(update)) {

                    ups.setDouble(1, price);
                    ups.setInt(2, id);
                    ups.executeUpdate();

                    System.out.println("Price updated successfully!");

                    try (PreparedStatement check = con.prepareStatement(select)) {
                        check.setInt(1, id);
                        ResultSet updated = check.executeQuery();

                        if (updated.next()) {
                            System.out.println("Updated Product Details:");
                            displayProduct(updated);
                        }
                    }
                }

            } else {
                System.out.println("Product not found!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (e) Aggregate query and conditional deletion
    static void inventoryAndDelete(Connection con) {

        String sql = "SELECT SUM(quantity * price) AS total FROM Product";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Total Inventory Value: " +
                        rs.getDouble("total"));
            }

            System.out.print("Enter Product ID to delete: ");
            int id = sc.nextInt();

            String delete = "DELETE FROM Product " +
                            "WHERE product_id = ? AND quantity = 0";

            try (PreparedStatement ps = con.prepareStatement(delete)) {

                ps.setInt(1, id);

                int rows = ps.executeUpdate();

                if (rows > 0)
                    System.out.println("Product deleted successfully!");
                else
                    System.out.println("Product cannot be deleted. " +
                            "It does not exist or quantity is not zero!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Display product details
    static void displayProduct(ResultSet rs) throws SQLException {

        System.out.println("-----------------------------");
        System.out.println("Product ID: " + rs.getInt("product_id"));
        System.out.println("Name: " + rs.getString("product_name"));
        System.out.println("Category: " + rs.getString("category"));
        System.out.println("Quantity: " + rs.getInt("quantity"));
        System.out.println("Price: " + rs.getDouble("price"));
        System.out.println("-----------------------------");
    }
}
