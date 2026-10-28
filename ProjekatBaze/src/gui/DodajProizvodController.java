package gui;

import application.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class DodajProizvodController {

    @FXML private TextField nazivField;
    @FXML private ComboBox<String> brendCombo;
    @FXML private ComboBox<String> kategorijaCombo;
    @FXML private TextField cijenaField;
    @FXML private TextField zapreminaField;

    @FXML
    public void initialize() {
        popuniBrendove();
        popuniKategorije();
    }

    private void popuniBrendove() {
        ObservableList<String> brendovi = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT naziv FROM brend")) {
            while (rs.next()) {
                brendovi.add(rs.getString("naziv"));
            }
            brendCombo.setItems(brendovi);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void popuniKategorije() {
        ObservableList<String> kategorije = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT naziv FROM kategorija")) {
            while (rs.next()) {
                kategorije.add(rs.getString("naziv"));
            }
            kategorijaCombo.setItems(kategorije);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sacuvajProizvod() {
        String naziv = nazivField.getText();
        String brend = brendCombo.getValue();
        String kategorija = kategorijaCombo.getValue();
        String cijenaText = cijenaField.getText();
        String zapreminaText = zapreminaField.getText();

        if (naziv.isEmpty() || brend == null || kategorija == null || cijenaText.isEmpty() || zapreminaText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Greška", "Popunite sva polja!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            int brendId = getIdZaNaziv(conn, "brend", brend);
            int kategorijaId = getIdZaNaziv(conn, "kategorija", kategorija);

            String sql = "INSERT INTO proizvod (naziv, brend_id, kategorija_id, cijena, zapremina) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, naziv);
            ps.setInt(2, brendId);
            ps.setInt(3, kategorijaId);
            ps.setDouble(4, Double.parseDouble(cijenaText));
            ps.setInt(5, Integer.parseInt(zapreminaText));
            ps.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Uspjeh", "Proizvod je uspješno dodat!");

            // reset polja
            nazivField.clear();
            cijenaField.clear();
            zapreminaField.clear();
            brendCombo.getSelectionModel().clearSelection();
            kategorijaCombo.getSelectionModel().clearSelection();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Greška", "Došlo je do greške prilikom unosa.");
        }
    }

    private int getIdZaNaziv(Connection conn, String tabela, String naziv) throws SQLException {
        String sql = "SELECT " + tabela + "_id FROM " + tabela + " WHERE naziv = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, naziv);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Nije pronađen zapis za: " + naziv);
            }
        }
    }

    private void showAlert(Alert.AlertType tip, String naslov, String poruka) {
        Alert alert = new Alert(tip);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
