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

public class BrendoviController {

    @FXML
    private TableView<ObservableList<String>> tableBrendovi; //svaki red tabele lista stringova

    @FXML
    private TableColumn<ObservableList<String>, String> colId;

    @FXML
    private TableColumn<ObservableList<String>, String> colNaziv;

    @FXML
    private TableColumn<ObservableList<String>, String> colOpis;

    @FXML
    public void initialize() {
        ucitajBrendove();
    }

    public void ucitajBrendove() {
        tableBrendovi.getColumns().clear();
        tableBrendovi.getItems().clear();

        String upit = "SELECT brend_id, naziv, opis FROM brend ORDER BY brend_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(upit)) {

            // ID
            TableColumn<ObservableList<String>, String> idCol = new TableColumn<>("ID brenda");
            idCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(0)));
            idCol.setCellFactory(TextFieldTableCell.forTableColumn());
            idCol.setPrefWidth(100);

            // Naziv
            TableColumn<ObservableList<String>, String> nazivCol = new TableColumn<>("Naziv brenda");
            nazivCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(1)));
            nazivCol.setCellFactory(TextFieldTableCell.forTableColumn());
            nazivCol.setPrefWidth(220);

            // Opis
            TableColumn<ObservableList<String>, String> opisCol = new TableColumn<>("Opis");
            opisCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().get(2)));
            opisCol.setCellFactory(TextFieldTableCell.forTableColumn());
            opisCol.setPrefWidth(400);

            tableBrendovi.getColumns().addAll(idCol, nazivCol, opisCol);

            while (rs.next()) {
                ObservableList<String> red = FXCollections.observableArrayList();
                red.add(rs.getString("brend_id"));
                red.add(rs.getString("naziv"));
                red.add(rs.getString("opis"));
                tableBrendovi.getItems().add(red);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            prikaziUpozorenje("Greška pri učitavanju brendova iz baze.");
        }
    }

    @FXML
    private void dodajBrend() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DodajBrend.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Dodaj novi brend");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // azuriranje tabele
            ucitajBrendove();

        } catch (Exception e) {
            e.printStackTrace();
            prikaziUpozorenje("Greška pri otvaranju forme za dodavanje brenda.");
        }
    }
    
    @FXML
    private void obrisiBrend() {
        ObservableList<String> selektovaniRed = tableBrendovi.getSelectionModel().getSelectedItem();
        
        if (selektovaniRed == null) {
            prikaziUpozorenje("Molimo selektujte brend koji želite da obrišete.");
            return;
        }
        
        String brendId = selektovaniRed.get(0);
        String nazivBrenda = selektovaniRed.get(1);
        
        // potvrda brisanja
        Alert potvrda = new Alert(Alert.AlertType.CONFIRMATION);
        potvrda.setTitle("Potvrda brisanja");
        potvrda.setHeaderText("Brisanje brenda");
        potvrda.setContentText("Da li ste sigurni da želite da obrišete brend: " + nazivBrenda + "?");
        
        if (potvrda.showAndWait().get() != ButtonType.OK) {
            return;
        }
        
        // da li brend ima proizvode
        if (brendImaProizvode(brendId)) {
            prikaziUpozorenje("Brisanje nije moguće! Brend '" + nazivBrenda + 
                             "' se koristi u tabeli proizvoda. Prvo uklonite sve proizvode ovog brenda.");
            return;
        }
        
        // obrisi brend
        if (obrisiBrendIzBaze(brendId)) {
            prikaziUpozorenje("Brend '" + nazivBrenda + "' je uspešno obrisan.");
            ucitajBrendove(); // azuriranje tabele
        } else {
            prikaziUpozorenje("Greška pri brisanju brenda.");
        }
    }

    private boolean brendImaProizvode(String brendId) {
        String upit = "SELECT COUNT(*) FROM proizvod WHERE brend_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(upit)) {
            
            pstmt.setInt(1, Integer.parseInt(brendId));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0; // true ako ima proizvode
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    private boolean obrisiBrendIzBaze(String brendId) {
        String upit = "DELETE FROM brend WHERE brend_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(upit)) {
            
            pstmt.setInt(1, Integer.parseInt(brendId));
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