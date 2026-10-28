package gui;

import application.ChangeScene;
import gui.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

public class LoginController {

    @FXML private TextField txtKorisnickoIme;
    @FXML private PasswordField txtLozinka;
    @FXML private Button btnPrijaviSe;
    @FXML private Label lblRegistrujSe;

    @FXML
    public void initialize() {
        btnPrijaviSe.setOnAction(e -> {
            String korisnik = txtKorisnickoIme.getText().trim();
            String lozinka = txtLozinka.getText().trim();

            UserDAO.ProdavacInfo prodavac = UserDAO.prijavi(korisnik, lozinka);
            
            if (prodavac != null) {
                // postavi sesiju - prodavac koji radi u filijali
                UserSession.getInstance().setProdavac(
                    prodavac.prodavacId, 
                    prodavac.filijalaId, 
                    prodavac.imePrezime, 
                    prodavac.korisnickoIme
                );
                
                ChangeScene.openPopup("/gui/Meni.fxml", "Glavni meni - " + prodavac.imePrezime, 900, 700);
            } else {
                prikaziUpozorenje("Neispravni kredencijali",
                    "❌ Podaci nisu tačni ili niste registrovani kao prodavac.");
            }
        });

        lblRegistrujSe.setOnMouseClicked(e -> {
            ChangeScene.openPopup("/gui/Registration.fxml", "Registracija korisnika", 400, 700);
        });
    }

    private void prikaziUpozorenje(String naslov, String poruka) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}