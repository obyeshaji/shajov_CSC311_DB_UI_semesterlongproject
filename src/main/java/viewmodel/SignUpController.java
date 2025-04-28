package viewmodel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserSession;

import java.util.prefs.Preferences;
import java.util.regex.Pattern;

public class SignUpController {

    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField emailField;
    
    // Regex pattern for email validation
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // Regex for password validation (at least 8 chars, one digit, one uppercase, one lowercase)
    private static final Pattern PASSWORD_PATTERN = 
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");
    
    @FXML
    public void createNewAccount(ActionEvent actionEvent) {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String email = emailField.getText().trim();
        
        // Store user credentials in preferences
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.put("USERNAME", username);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("EMAIL", email);
        
        // Create user session
        UserSession.getInstance(username, password, "USER");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Created");
        alert.setHeaderText("Account Creation Successful");
        alert.setContentText("Your account has been created successfully. You can now log in.");
        alert.showAndWait();
        
        // Navigate back to login
        goBack(actionEvent);
    }
    
    private boolean validateInputs() {
        StringBuilder errorMsg = new StringBuilder();
        
        // Validate username
        if (usernameField.getText().trim().isEmpty()) {
            errorMsg.append("- Username cannot be empty\n");
        } else if (usernameField.getText().trim().length() < 4) {
            errorMsg.append("- Username must be at least 4 characters\n");
        }
        
        // Validate password
        if (passwordField.getText().isEmpty()) {
            errorMsg.append("- Password cannot be empty\n");
        } else if (!PASSWORD_PATTERN.matcher(passwordField.getText()).matches()) {
            errorMsg.append("- Password must be at least 8 characters and include one digit, one uppercase letter, and one lowercase letter\n");
        }
        
        // Confirm passwords match
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorMsg.append("- Passwords do not match\n");
        }
        
        // Validate email
        if (emailField.getText().trim().isEmpty()) {
            errorMsg.append("- Email cannot be empty\n");
        } else if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            errorMsg.append("- Email is not valid\n");
        }
        
        // If there are validation errors, show them
        if (errorMsg.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMsg.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
