package viewmodel;

import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import model.Person.Major;
import service.MyLogger;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class DB_GUI_Controller implements Initializable {

    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    ComboBox<Major> major;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    @FXML
    private Button addBtn, editBtn, deleteBtn;
    @FXML
    private MenuItem editItem, deleteItem, importCSVItem, exportCSVItem;
    
    @FXML
    private Label statusLabel;
    
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    
    // Regular expressions for field validation
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s-]{2,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DEPARTMENT_PATTERN = Pattern.compile("^[A-Za-z\\s-]{2,50}$");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);
            
            // Initialize Major ComboBox with enum values
            major.setItems(FXCollections.observableArrayList(Major.values()));
            major.getSelectionModel().selectFirst();
            
            // Setup CSV import/export menu actions
            importCSVItem.setOnAction(event -> importCSV());
            exportCSVItem.setOnAction(event -> exportCSV());
            
            // Initial UI State - Disable Edit and Delete buttons
            updateUIState();
            
            // Add listeners to form fields to validate and update UI state
            first_name.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
            last_name.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
            department.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
            major.valueProperty().addListener((observable, oldValue, newValue) -> validateInput());
            email.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
            
            // Listen for table selection changes
            tv.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                updateUIState();
            });
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void updateUIState() {
        boolean isItemSelected = tv.getSelectionModel().getSelectedItem() != null;
        
        // Update buttons based on selection state
        if (editBtn != null) editBtn.setDisable(!isItemSelected);
        if (deleteBtn != null) deleteBtn.setDisable(!isItemSelected);
        
        // Update menu items based on selection state
        if (editItem != null) editItem.setDisable(!isItemSelected);
        if (deleteItem != null) deleteItem.setDisable(!isItemSelected);
        
        // Validate input fields for Add button
        validateInput();
    }
    
    private void validateInput() {
        boolean isFirstNameValid = NAME_PATTERN.matcher(first_name.getText()).matches();
        boolean isLastNameValid = NAME_PATTERN.matcher(last_name.getText()).matches();
        boolean isDepartmentValid = DEPARTMENT_PATTERN.matcher(department.getText()).matches();
        boolean isEmailValid = EMAIL_PATTERN.matcher(email.getText()).matches();
        boolean isMajorValid = major.getValue() != null;
        
        // Enable Add button only if all fields are valid
        boolean allFieldsValid = isFirstNameValid && isLastNameValid && isDepartmentValid && 
                                isMajorValid && isEmailValid;
        
        if (addBtn != null) {
            addBtn.setDisable(!allFieldsValid);
        }
        
        // Show validation status in the status bar for user feedback
        if (!allFieldsValid) {
            StringBuilder message = new StringBuilder("Please correct: ");
            if (!isFirstNameValid) message.append("First Name, ");
            if (!isLastNameValid) message.append("Last Name, ");
            if (!isDepartmentValid) message.append("Department, ");
            if (!isEmailValid) message.append("Email, ");
            if (!isMajorValid) message.append("Major, ");
            
            // Remove trailing comma and space
            if (message.length() > 15) {
                message.setLength(message.length() - 2);
            }
            
            showStatusMessage(message.toString());
        }
    }

    @FXML
    protected void addNewRecord() {
        try {
            Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    major.getValue().toString(), email.getText(), imageURL.getText());
            cnUtil.insertUser(p);
            cnUtil.retrieveId(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();
            
            // Show success message to user
            showStatusMessage("Record added successfully!");
        } catch (Exception e) {
            showStatusMessage("Error: Could not add record");
        }
    }
    
    @FXML
    protected void importCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(selectedFile));
                String line;
                
                // Skip header
                reader.readLine();
                
                int recordsImported = 0;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length >= 5) { // At least FirstName,LastName,Department,Major,Email
                        String firstName = fields[0].trim();
                        String lastName = fields[1].trim();
                        String department = fields[2].trim();
                        String majorStr = fields[3].trim();
                        String email = fields[4].trim();
                        String imageURL = fields.length > 5 ? fields[5].trim() : "";
                        
                        // Create and add the person
                        Person p = new Person(firstName, lastName, department, majorStr, email, imageURL);
                        cnUtil.insertUser(p);
                        p.setId(cnUtil.retrieveId(p));
                        data.add(p);
                        recordsImported++;
                    }
                }
                
                reader.close();
                showStatusMessage("Successfully imported " + recordsImported + " records from CSV");
            } catch (Exception e) {
                showStatusMessage("Error importing CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    protected void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                java.io.PrintWriter writer = new java.io.PrintWriter(selectedFile);
                
                // Write header
                writer.println("FirstName,LastName,Department,Major,Email,ImageURL");
                
                // Write data
                for (Person person : data) {
                    writer.println(
                        person.getFirstName() + "," +
                        person.getLastName() + "," + 
                        person.getDepartment() + "," +
                        person.getMajor() + "," +
                        person.getEmail() + "," +
                        person.getImageURL()
                    );
                }
                
                writer.close();
                showStatusMessage("Successfully exported " + data.size() + " records to CSV");
            } catch (Exception e) {
                showStatusMessage("Error exporting CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void showStatusMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            
            // Clear the message after 5 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Platform.runLater(() -> statusLabel.setText(""));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        major.getSelectionModel().selectFirst();
        email.setText("");
        imageURL.setText("");
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
            showStatusMessage("Error: " + e.getMessage());
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        try {
            Person p = tv.getSelectionModel().getSelectedItem();
            int index = data.indexOf(p);
            Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                    major.getValue().toString(), email.getText(), imageURL.getText());
            cnUtil.editUser(p.getId(), p2);
            data.remove(p);
            data.add(index, p2);
            tv.getSelectionModel().select(index);
            
            // Show success message to user
            showStatusMessage("Record updated successfully!");
        } catch (Exception e) {
            showStatusMessage("Error: Could not update record");
        }
    }

    @FXML
    protected void deleteRecord() {
        try {
            Person p = tv.getSelectionModel().getSelectedItem();
            int index = data.indexOf(p);
            cnUtil.deleteRecord(p);
            data.remove(index);
            tv.getSelectionModel().select(index);
            
            // Show success message to user
            showStatusMessage("Record deleted successfully!");
        } catch (Exception e) {
            showStatusMessage("Error: Could not delete record");
        }
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            // Set the major ComboBox value based on the string in the Person object
            for (Major majorOption : Major.values()) {
                if (majorOption.toString().equals(p.getMajor())) {
                    major.setValue(majorOption);
                    break;
                }
            }
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
            updateUIState();
        }
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ComboBox<Major> comboBox = new ComboBox<>(FXCollections.observableArrayList(Major.values()));
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    private static class Results {
        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }
}