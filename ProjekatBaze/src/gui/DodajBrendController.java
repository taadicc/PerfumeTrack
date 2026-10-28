package gui;

import application.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DodajBrendController {

    @FXML
    private TextField nazivField;

    @FXML
    private TextField opisField;

    @FXML
    public void sacuvajBrend() {
        String naziv = nazivField.getText().trim();
        String opis = opisField.getText().trim();

        if (naziv.isEmpty() || opis.isEmpty()) {
            prikaziUpozorenje("Molimo unesite naziv i opis.");
            return;
        }

        String upit = "INSERT INTO brend (naziv, opis) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(upit)) {

            stmt.setString(1, naziv);
            stmt.setString(2, opis);
            stmt.executeUpdate();

            zatvoriProzor();

        } catch (SQLException e) {
            e.printStackTrace();
            prikaziUpozorenje("Greška pri dodavanju brenda u bazu.");
        }
    }

    private void zatvoriProzor() {
        Stage stage = (Stage) nazivField.getScene().getWindow();
        stage.close();
    }

    private void prikaziUpozorenje(String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Obaveštenje");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
