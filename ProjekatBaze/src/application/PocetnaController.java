package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.application.Platform;
import java.net.URL;
import java.util.ResourceBundle;

public class PocetnaController implements Initializable {

	@FXML
	private Button btnPrijava;

	@FXML
	private Label welcomeLabel;

	@FXML
	private Label descriptionLabel;

	@FXML
	private VBox mainContent;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnPrijava.setOnAction(e -> {
			openLoginWindow();
		});
		initializeAnimations();
		setupButtonHoverEffects();
	}

	private void openLoginWindow() {
		try {
			ChangeScene.openPopup("/gui/Login.fxml", "Prijava korisnika", 900, 700);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeAnimations() {
		FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), mainContent);
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);
		fadeIn.play();

		Timeline welcomeAnimation = new Timeline(new KeyFrame(Duration.ZERO, e -> welcomeLabel.setOpacity(0)),
				new KeyFrame(Duration.millis(500), e -> welcomeLabel.setOpacity(1)));
		welcomeAnimation.play();

		Timeline descAnimation = new Timeline(new KeyFrame(Duration.ZERO, e -> descriptionLabel.setOpacity(0)),
				new KeyFrame(Duration.millis(800), e -> descriptionLabel.setOpacity(1)));
		descAnimation.play();
	}

	private void setupButtonHoverEffects() {
		btnPrijava.setOnMouseEntered(e -> {
			ScaleTransition hoverScale = new ScaleTransition(Duration.millis(200), btnPrijava);
			hoverScale.setToX(1.05);
			hoverScale.setToY(1.05);
			hoverScale.play();
		});

		btnPrijava.setOnMouseExited(e -> {
			ScaleTransition exitScale = new ScaleTransition(Duration.millis(200), btnPrijava);
			exitScale.setToX(1.0);
			exitScale.setToY(1.0);
			exitScale.play();
		});
	}
}