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
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ProizvodiController {

    @FXML
    private TableView<ObservableList<String>> tabelaProizvoda;

    @FXML
    private TextField searchField;
    
    @FXML
    private Label fillijalaLabel; // trenutna filijala
    
    // ID tren filijale - (UserSession)
    private int trenutnaFilijalaId;
    private String nazivFilijale;
    
    // inicijalizacija filijale
    public ProizvodiController() {
        this.trenutnaFilijalaId = getCurrentKorisnikFilijalId();
        this.nazivFilijale = getNazivFilijale(trenutnaFilijalaId);
    }
    
    // filijala tren prijavljenog korisnika (UserSession)
    private int getCurrentKorisnikFilijalId() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            return session.getFilijalaId();
        }
        return 1;
    }
    
    private String getNazivFilijale(int filijalId) {
        String upit = "SELECT naziv FROM filijala WHERE filijala_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(upit)) {
            
            stmt.setInt(1, filijalId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("naziv");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Nepoznata filijala";
    }

    @FXML
    public void initialize() {
        this.trenutnaFilijalaId = getCurrentKorisnikFilijalId();
        this.nazivFilijale = getNazivFilijale(trenutnaFilijalaId);
        
        if (fillijalaLabel != null) {
            UserSession session = UserSession.getInstance();
            String tekst = "Filijala: " + nazivFilijale;
            if (session.isLoggedIn()) {
                tekst += " | Prodavac: " + session.getImePrezime();
            }
            fillijalaLabel.setText(tekst);
        }
        
        ucitajProizvode();
    }

    @FXML
    public void ucitajProizvode() {
        String upit = "SELECT p.proizvod_id, p.naziv, b.naziv as brend_naziv, k.naziv as kategorija_naziv, " +
                     "p.cijena, p.zapremina, " +
                     "COALESCE(z.kolicina, 0) as trenutna_kolicina, " +
                     "CASE WHEN z.kolicina > 0 THEN 'Dostupan' ELSE 'Nije dostupan' END as dostupnost " +
                     "FROM proizvod p " +
                     "LEFT JOIN brend b ON p.brend_id = b.brend_id " +
                     "LEFT JOIN kategorija k ON p.kategorija_id = k.kategorija_id " +
                     "LEFT JOIN zaliha z ON p.proizvod_id = z.proizvod_id AND z.filijala_id = ? " +
                     "ORDER BY p.proizvod_id";
        
        ucitajPodatke(upit, null, true);
    }

    @FXML
    public void pretraziProizvode() {
        String pretraga = searchField.getText().trim(); //trim uklanja razmake 
        if (pretraga.isEmpty()) {
            ucitajProizvode();
            return;
        }
        
        String upit = "SELECT p.proizvod_id, p.naziv, b.naziv as brend_naziv, k.naziv as kategorija_naziv, " +
                     "p.cijena, p.zapremina, " +
                     "COALESCE(z.kolicina, 0) as trenutna_kolicina, " +
                     "CASE WHEN z.kolicina > 0 THEN 'Dostupan' ELSE 'Nije dostupan' END as dostupnost " +
                     "FROM proizvod p " +
                     "LEFT JOIN brend b ON p.brend_id = b.brend_id " +
                     "LEFT JOIN kategorija k ON p.kategorija_id = k.kategorija_id " +
                     "LEFT JOIN zaliha z ON p.proizvod_id = z.proizvod_id AND z.filijala_id = ? " +
                     "WHERE LOWER(p.naziv) LIKE LOWER(?) " + 
                     "ORDER BY p.proizvod_id";
        //lower da bi mogla i mala i velika slova
        String pattern = "%" + pretraga + "%";  //wildcard karakter u sqlu %...nebitno sta je prije i poslije
        ucitajPodatke(upit, pattern, true);
    }

    private void ucitajPodatke(String upit, String pretragaPattern, boolean ukluciFilijalId) {
        tabelaProizvoda.getColumns().clear();
        tabelaProizvoda.getItems().clear();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(upit);
            
            int paramIndex = 1;
            
            // filijala_id kao prvi parametar
            if (ukluciFilijalId) {
                stmt.setInt(paramIndex++, trenutnaFilijalaId);
            }
            
            // ako ima pretraga pattern, dodajemo ga kao sledeći parametar
            if (pretragaPattern != null) {
                stmt.setString(paramIndex, pretragaPattern);
            }
            
            ResultSet rs = stmt.executeQuery();

           
            String[] naziviKolona = {"ID", "Naziv", "Brend", "Kategorija", "Cijena", "Zapremina", "Dostupnost", "Količina"};
            String[] originalniNazivi = {"proizvod_id", "naziv", "brend_naziv", "kategorija_naziv", "cijena", "zapremina", "dostupnost", "trenutna_kolicina"};

            for (int i = 0; i < naziviKolona.length; i++) {
                final int kolonaIndex = i;
                TableColumn<ObservableList<String>, String> kolona = new TableColumn<>(naziviKolona[i]);
                
                kolona.setCellValueFactory(param -> {
                    String vrednost = param.getValue().get(kolonaIndex);
                    if (vrednost == null) vrednost = "";
                    
                    if (originalniNazivi[kolonaIndex].equals("cijena") && !vrednost.isEmpty()) {
                        try {
                            double cijena = Double.parseDouble(vrednost);
                            vrednost = String.format("%.0f KM", cijena);
                        } catch (NumberFormatException e) {
                        }
                    } else if (originalniNazivi[kolonaIndex].equals("zapremina") && !vrednost.isEmpty()) {
                        vrednost = vrednost + " ml";
                    } else if (originalniNazivi[kolonaIndex].equals("dostupnost")) {
                        kolona.setCellFactory(column -> new TableCell<ObservableList<String>, String>() {
                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setStyle("");
                                } else {
                                    setText(item);
                                    if ("Dostupan".equals(item)) {
                                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                                    } else {
                                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                                    }
                                }
                            }
                        });
                    }
                    
                    return new javafx.beans.property.SimpleStringProperty(vrednost);
                });
                
                //sirina kolona
                switch (i) {
                    case 0: kolona.setPrefWidth(50); break;  // ID
                    case 1: kolona.setPrefWidth(150); break; // Naziv
                    case 2: kolona.setPrefWidth(100); break; // Brend
                    case 3: kolona.setPrefWidth(120); break; // Kategorija
                    case 4: kolona.setPrefWidth(80); break;  // Cijena
                    case 5: kolona.setPrefWidth(90); break;  // Zapremina
                    case 6: kolona.setPrefWidth(100); break; // Dostupnost
                    case 7: kolona.setPrefWidth(80); break;  // Količina
                }
                
                tabelaProizvoda.getColumns().add(kolona);
            }

            //dodavanje redova
            while (rs.next()) {
                ObservableList<String> red = FXCollections.observableArrayList();
                red.add(rs.getString("proizvod_id"));
                red.add(rs.getString("naziv"));
                red.add(rs.getString("brend_naziv"));
                red.add(rs.getString("kategorija_naziv"));
                red.add(rs.getString("cijena"));
                red.add(rs.getString("zapremina"));
                red.add(rs.getString("dostupnost"));
                red.add(rs.getString("trenutna_kolicina"));
                
                tabelaProizvoda.getItems().add(red);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            prikazGresku("Greška pri učitavanju podataka: " + e.getMessage());
        }
    }

    @FXML
    public void dodajProizvod() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DodajProizvod.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Dodaj proizvod");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            ucitajProizvode();
        } catch (IOException e) {
            e.printStackTrace();
            prikazGresku("Greška pri otvaranju forme za dodavanje proizvoda.");
        }
    }

    @FXML
    public void obrisiProizvod() {
        ObservableList<String> selektovaniRed = tabelaProizvoda.getSelectionModel().getSelectedItem();
        if (selektovaniRed == null) {
            prikaziInfo("Molimo selektujte proizvod koji želite obrisati.");
            return;
        }

        String id = selektovaniRed.get(0);
        String naziv = selektovaniRed.get(1);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potvrda brisanja");
        confirm.setHeaderText(null);
        confirm.setContentText("Da li ste sigurni da želite obrisati proizvod: " + naziv + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String upit = "DELETE FROM proizvod WHERE proizvod_id = ?";

                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(upit)) {

                    stmt.setString(1, id);
                    int rezultat = stmt.executeUpdate();

                    if (rezultat > 0) {
                        tabelaProizvoda.getItems().remove(selektovaniRed);
                        prikaziInfo("Proizvod je uspešno obrisan.");
                    } else {
                        prikazGresku("Brisanje nije uspelo.");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    prikazGresku("Greška pri brisanju: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    public void prikaziProizvodeNaPromociji() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProizvodiNaPromociji.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Proizvodi na promociji");
            stage.setScene(new Scene(root));
            stage.setWidth(900);
            stage.setHeight(600);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            prikazGresku("Greška pri otvaranju prozora za proizvode na promociji.");
        }
    }

    public void setTrenutnaFilijalaId(int filijalId) {
        this.trenutnaFilijalaId = filijalId;
        this.nazivFilijale = getNazivFilijale(filijalId);
        
        if (fillijalaLabel != null) {
            UserSession session = UserSession.getInstance();
            String tekst = "Filijala: " + nazivFilijale;
            if (session.isLoggedIn()) {
                tekst += " | Prodavac: " + session.getImePrezime();
            }
            fillijalaLabel.setText(tekst);
        }
        
        ucitajProizvode(); 
    }

    public int getTrenutnaFilijalaId() {
        return trenutnaFilijalaId;
    }
    
    public String getNazivFilijale() {
        return nazivFilijale;
    }
    
    public void refreshFilijalaInfo() {
        this.trenutnaFilijalaId = getCurrentKorisnikFilijalId();
        this.nazivFilijale = getNazivFilijale(trenutnaFilijalaId);
        
        if (fillijalaLabel != null) {
            UserSession session = UserSession.getInstance();
            String tekst = "Filijala: " + nazivFilijale;
            if (session.isLoggedIn()) {
                tekst += " | Prodavac: " + session.getImePrezime();
            }
            fillijalaLabel.setText(tekst);
        }
        
        ucitajProizvode();
    }

    private void prikaziInfo(String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacija");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    private void prikazGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}