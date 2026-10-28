package gui;

import application.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.sql.*;
import java.util.*;

public class RegistrationController implements Initializable {

    @FXML private TextField txtIme;
    @FXML private TextField txtPrezime;
    @FXML private TextField txtKorisnickoIme;
    @FXML private PasswordField txtLozinka;
    @FXML private PasswordField txtPotvrdaLozinke;
    @FXML private ComboBox<String> cmbTipKorisnika;
    @FXML private ComboBox<String> cmbFilijala;
    @FXML private Button btnRegistrujSe;

    private Map<String, Integer> mapaFilijala = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbTipKorisnika.getItems().addAll("Prodavac", "Nabavljač");

        try (Connection konekcija = DBConnection.getConnection()) {
            Statement stmt = konekcija.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT filijala_id, naziv FROM filijala");

            while (rs.next()) {
                int id = rs.getInt("filijala_id");
                String naziv = rs.getString("naziv");
                mapaFilijala.put(naziv, id);
                cmbFilijala.getItems().add(naziv);
            }

        } catch (SQLException e) {
            prikaziAlert("Greška", "Greška pri učitavanju filijala iz baze.", AlertType.ERROR);
            e.printStackTrace();
        }

        btnRegistrujSe.setOnAction(e -> registrujKorisnika());
    }

    private void registrujKorisnika() {
        String ime = txtIme.getText().trim();
        String prezime = txtPrezime.getText().trim();
        String korisnickoIme = txtKorisnickoIme.getText().trim();
        String lozinka = txtLozinka.getText().trim();
        String potvrda = txtPotvrdaLozinke.getText().trim();
        String tip = cmbTipKorisnika.getValue();
        String izabranaFilijala = cmbFilijala.getValue();

        if (ime.isEmpty() || prezime.isEmpty() || korisnickoIme.isEmpty() ||
            lozinka.isEmpty() || potvrda.isEmpty() || tip == null || izabranaFilijala == null) {
            prikaziAlert("Greška", "Sva polja moraju biti popunjena.", AlertType.WARNING);
            return;
        }

        if (!lozinka.equals(potvrda)) {
            prikaziAlert("Greška", "Lozinke se ne poklapaju.", AlertType.WARNING);
            return;
        }

        int filijalaId = mapaFilijala.get(izabranaFilijala);

        try (Connection konekcija = DBConnection.getConnection()) {
            // da li korisnicko ime vec postoji
            PreparedStatement provera = konekcija.prepareStatement(
                "SELECT korisnik_id FROM korisnik WHERE korisnicko_ime = ?");
            provera.setString(1, korisnickoIme);
            ResultSet rs = provera.executeQuery();
            if (rs.next()) {
                prikaziAlert("Greška", "Korisničko ime je već zauzeto.", AlertType.WARNING);
                return;
            }

            // unesi u tabelu korisnik
            PreparedStatement insertKorisnik = konekcija.prepareStatement(
                "INSERT INTO korisnik (ime, prezime, korisnicko_ime, lozinka) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            insertKorisnik.setString(1, ime);
            insertKorisnik.setString(2, prezime);
            insertKorisnik.setString(3, korisnickoIme);
            insertKorisnik.setString(4, lozinka);
            insertKorisnik.executeUpdate();

            ResultSet keys = insertKorisnik.getGeneratedKeys();
            if (!keys.next()) {
                prikaziAlert("Greška", "Neuspešno dodavanje korisnika.", AlertType.ERROR);
                return;
            }

            int korisnikId = keys.getInt(1);

            // prodavac ili nabavljac
            if (tip.equals("Prodavac")) {
                PreparedStatement insertProdavac = konekcija.prepareStatement(
                    "INSERT INTO prodavac (prodavac_id, filijala_id) VALUES (?, ?)");
                insertProdavac.setInt(1, korisnikId);
                insertProdavac.setInt(2, filijalaId);
                insertProdavac.executeUpdate();
            } else if (tip.equals("Nabavljač")) {
                PreparedStatement insertNabavljac = konekcija.prepareStatement(
                    "INSERT INTO nabavljac (nabavljac_id, filijala_id) VALUES (?, ?)");
                insertNabavljac.setInt(1, korisnikId);
                insertNabavljac.setInt(2, filijalaId);
                insertNabavljac.executeUpdate();
            }

            prikaziAlert("Uspeh", "Korisnik je uspešno registrovan!", AlertType.INFORMATION);
            ocistiPolja();

        } catch (SQLException e) {
            prikaziAlert("Greška", "Greška prilikom upisa u bazu.", AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void prikaziAlert(String naslov, String poruka, AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    private void ocistiPolja() {
        txtIme.clear();
        txtPrezime.clear();
        txtKorisnickoIme.clear();
        txtLozinka.clear();
        txtPotvrdaLozinke.clear();
        cmbTipKorisnika.getSelectionModel().clearSelection();
        cmbFilijala.getSelectionModel().clearSelection();
    }
}
