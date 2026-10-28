package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChangeScene {

    public static void openPopup(String fxmlFileName, String windowTitle, int width, int height) {
        try {
            // ucitavanje fxml fajla
            FXMLLoader loader = new FXMLLoader(ChangeScene.class.getResource(fxmlFileName));
            Parent root = loader.load();

            // kreiranje nove scene
            Stage popupStage = new Stage();
            popupStage.setTitle(windowTitle);
            popupStage.initModality(Modality.APPLICATION_MODAL); // blokira glavni prozor dok je popup otvoren
            popupStage.setScene(new Scene(root, width, height));
            popupStage.setResizable(false);
            popupStage.showAndWait(); // prikazuje popup i ceka da se zatvori
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

