package gui;
import application.ChangeScene;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MeniController {
    @FXML
    private Button btnProizvodi;
    @FXML
    private Button btnBrendovi;
    @FXML
    private Button btnKategorije;
    @FXML
    private Button btnProdaja;
    @FXML
    private Button btnKreirajRacun;
    @FXML
    private Button btnOdjaviSe;
    
    @FXML
    public void initialize() {
        btnProizvodi.setOnAction(e ->
                ChangeScene.openPopup("/gui/Proizvodi.fxml", "Pregled proizvoda", 900, 700));
        
        btnBrendovi.setOnAction(e ->
                ChangeScene.openPopup("/gui/Brendovi.fxml", "Lista brendova", 900, 500));
        
        btnKategorije.setOnAction(e ->
                ChangeScene.openPopup("/gui/Kategorije.fxml", "Kategorije parfema", 700, 500));
        
        btnProdaja.setOnAction(e ->
                ChangeScene.openPopup("/gui/Prodaja.fxml", "Prodaja parfema", 850, 700));
        
        btnKreirajRacun.setOnAction(e ->
                ChangeScene.openPopup("/gui/KreiranjeRacuna.fxml", "Dodaj korisnika", 850, 700));
        
        btnOdjaviSe.setOnAction(e ->
                ChangeScene.openPopup("/gui/Login.fxml", "Prijava", 900, 700));
    }
}