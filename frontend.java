import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentOutpassSystem implements ActionListener {

    private JFrame frame;
    private JTextField nameField, reasonField, dateField, daysField;
    private JButton submitButton;
    private Connection connection;

    public StudentOutpassSystem() {
        // Establish database connection
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/student_outpass_db", "your_username", "your_password");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database connection failed!");
            return;
        }

        // Create the frame
        frame = new JFrame("Student Outpass System");
        frame.setLayout(new GridLayout(5, 2));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create input fields
        JLabel nameLabel = new JLabel("Name:");
        frame.add(nameLabel);
        nameField = new JTextField();
        frame.add(nameField);

        JLabel reasonLabel = new JLabel("Reason:");
        frame.add(reasonLabel);
        reasonField = new JTextField();
        frame.add(reasonField);

        JLabel dateLabel = new JLabel("Date:");
        frame.add(dateLabel);
        dateField = new JTextField();
        frame.add(dateField);

        JLabel daysLabel = new JLabel("Number of Days:");
        frame.add(daysLabel);
        daysField = new JTextField();
        frame.add(daysField);

        // Submit button
        submitButton = new JButton("Submit");
        submitButton.addActionListener(this);
        frame.add(new JLabel()); // Empty label for spacing
        frame.add(submitButton);

        // Display the window
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            String name = nameField.getText();
            String reason = reasonField.getText();
            String date = dateField.getText();
            String days = daysField.getText();

            // Store the request in the database
            storeOutpassRequest(name, reason, date, Integer.parseInt(days));
            approveOrDenyRequest(name, reason, date, days);
        }
    }

    private void storeOutpassRequest(String studentName, String reason, String date, int days) {
        String query = "INSERT INTO outpass_requests (student_name, reason, date, days, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentName);
            pstmt.setString(2, reason);
            pstmt.setDate(3, Date.valueOf(date));
            pstmt.setInt(4, days);
            pstmt.setString(5, "Pending");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to store outpass request.");
        }
    }

    private void approveOrDenyRequest(String studentName, String reason, String date, String days) {
        JFrame approvalFrame = new JFrame("Approve or Deny Request");
        approvalFrame.setLayout(new GridLayout(4, 1));

        approvalFrame.add(new JLabel("Student Name: " + studentName));
        approvalFrame.add(new JLabel("Reason: " + reason));
        approvalFrame.add(new JLabel("Date: " + date));
        approvalFrame.add(new JLabel("Number of Days: " + days));

        JButton approveButton = new JButton("Approve");
        approveButton.addActionListener(e -> {
            updateRequestStatus(studentName, "Approved");
            approvalFrame.dispose();
        });
        approvalFrame.add(approveButton);

        JButton denyButton = new JButton("Deny");
        denyButton.addActionListener(e -> {
            updateRequestStatus(studentName, "Denied");
            approvalFrame.dispose();
        });
        approvalFrame.add(denyButton);

        // Display the dialog
        approvalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        approvalFrame.pack();
        approvalFrame.setVisible(true);
    }

    private void updateRequestStatus(String studentName, String status) {
        String query = "UPDATE outpass_requests SET status = ? WHERE student_name = ? AND status = 'Pending'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setString(2, studentName);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(frame, "The outpass request for " + studentName + " has been " + status + ".");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to update request status.");
        }
    }

    public static void main(String[] args) {
        new StudentOutpassSystem();
    }
}
