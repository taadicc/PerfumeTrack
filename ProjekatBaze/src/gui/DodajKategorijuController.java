package gui;

import application.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class DodajKategorijuController {

    @FXML
    private TextField txtNaziv;

    @FXML
    private void sacuvajKategoriju() {
        String naziv = txtNaziv.getText().trim();
        if (naziv.isEmpty()) {
            prikaziUpozorenje("Greška", "Naziv kategorije ne može biti prazan.");
            return;
        }

        if (naziv.length() > 100) {
            prikaziUpozorenje("Greška", "Naziv kategorije ne može biti duži od 100 karaktera.");
            return;
        }

        //da li kategorija već postoji
        if (kategorijaPostoji(naziv)) {
            prikaziUpozorenje("Greška", "Kategorija sa ovim nazivom već postoji.");
            return;
        }

        // dodaj u bazu
        String upit = "INSERT INTO kategorija (naziv) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(upit)) {

            pstmt.setString(1, naziv);
            int rezultat = pstmt.executeUpdate();

            if (rezultat > 0) {
                prikaziPoruku("Uspjeh", "Kategorija je uspješno dodana.");
                zatvoriFormu();
            } else {
                prikaziUpozorenje("Greška", "Kategorija nije dodana. Pokušajte ponovo.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            prikaziUpozorenje("Greška baze podataka", "Greška pri dodavanju kategorije: " + e.getMessage());
        }
    }

    private boolean kategorijaPostoji(String naziv) {
        String upit = "SELECT COUNT(*) FROM kategorija WHERE LOWER(naziv) = LOWER(?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(upit)) {
            
            pstmt.setString(1, naziv);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    @FXML
    private void otkazi() {
        zatvoriFormu();
    }

    private void zatvoriFormu() {
        Stage stage = (Stage) txtNaziv.getScene().getWindow();
        stage.close();
    }

    private void prikaziUpozorenje(String naslov, String poruka) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    private void prikaziPoruku(String naslov, String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}