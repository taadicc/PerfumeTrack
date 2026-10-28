package gui;

import application.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.sql.*;

public class KategorijeController {

    @FXML
    private TableView<ObservableList<String>> tableKategorije;

    @FXML
    private TableColumn<ObservableList<String>, String> colId;

    @FXML
    private TableColumn<ObservableList<String>, String> colNaziv;

    @FXML
    public void initialize() {
        ucitajKategorije();
    }

    public void ucitajKategorije() {
        tableKategorije.getColumns().clear();
        tableKategorije.getItems().clear();

        String upit = "SELECT kategorija_id, naziv FROM kategorija ORDER BY kategorija_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(upit)) {

            // ID
            TableColumn<ObservableList<String>, String> idCol = new TableColumn<>("ID kategorije");
            idCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
            idCol.setCellFactory(TextFieldTableCell.forTableColumn());
            idCol.setPrefWidth(150);

            // Naziv
            TableColumn<ObservableList<String>, String> nazivCol = new TableColumn<>("Naziv kategorije");
            nazivCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
            nazivCol.setCellFactory(TextFieldTableCell.forTableColumn());
            nazivCol.setPrefWidth(500);

            tableKategorije.getColumns().addAll(idCol, nazivCol);

            while (rs.next()) {
                ObservableList<String> red = FXCollections.observableArrayList();
                red.add(rs.getString("kategorija_id"));
                red.add(rs.getString("naziv"));
                tableKategorije.getItems().add(red);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            prikaziUpozorenje("Greška pri učitavanju kategorija iz baze.");
        }
    }

    @FXML
    private void dodajKategoriju() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DodajKategoriju.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Dodaj novu kategoriju");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // azuriraj tabelu
            ucitajKategorije();

        } catch (Exception e) {
            e.printStackTrace();
            prikaziUpozorenje("Greška pri otvaranju forme za dodavanje kategorije.");
        }
    }
    @FXML
    private void obrisiKategoriju() {
        ObservableList<String> selektovaniRed = tableKategorije.getSelectionModel().getSelectedItem();
        
        if (selektovaniRed == null) {
            prikaziUpozorenje("Molimo selektujte kategoriju koju želite da obrišete.");
            return;
        }
        
        String kategorijaId = selektovaniRed.get(0);
        String nazivKategorije = selektovaniRed.get(1);
        
        // potvrda brisanja
        Alert potvrda = new Alert(Alert.AlertType.CONFIRMATION);
        potvrda.setTitle("Potvrda brisanja");
        potvrda.setHeaderText("Brisanje kategorije");
        potvrda.setContentText("Da li ste sigurni da želite da obrišete kategoriju: " + nazivKategorije + "?");
        
        if (potvrda.showAndWait().get() != ButtonType.OK) {
            return;
        }
        
        // da li kategorija ima proizvode
        if (kategorijaImaProizvode(kategorijaId)) {
            prikaziUpozorenje("Brisanje nije moguće! Kategorija '" + nazivKategorije + 
                             "' se koristi u tabeli proizvoda. Prvo uklonite sve proizvode ove kategorije.");
            return;
        }
        
        // Obriši kategoriju
        if (obrisiKategorijuIzBaze(kategorijaId)) {
            prikaziUpozorenje("Kategorija '" + nazivKategorije + "' je uspešno obrisana.");
            ucitajKategorije(); // Osveži tabelu
        } else {
            prikaziUpozorenje("Greška pri brisanju kategorije.");
        }
    }

    private boolean kategorijaImaProizvode(String kategorijaId) {
        String upit = "SELECT COUNT(*) FROM proizvod WHERE kategorija_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(upit)) {
            
            pstmt.setInt(1, Integer.parseInt(kategorijaId));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0; // true ako ima proizvode
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    private boolean obrisiKategorijuIzBaze(String kategorijaId) {
        String upit = "DELETE FROM kategorija WHERE kategorija_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(upit)) {
            
            pstmt.setInt(1, Integer.parseInt(kategorijaId));
            int obrisano = pstmt.executeUpdate();
            
            return obrisano > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void prikaziUpozorenje(String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Obaveštenje");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}