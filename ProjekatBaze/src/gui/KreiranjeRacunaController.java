package gui;

import gui.UserSession;
import application.DBConnection;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class KreiranjeRacunaController {

	@FXML
	private ComboBox<String> proizvodComboBox;
	@FXML
	private TextField kolicinaField;
	@FXML
	private TableView<Stavka> stavkeTable;
	@FXML
	private TextField karticaIdField;
	@FXML
	private CheckBox koristiBodoveCheckBox;
	@FXML
	private Label ukupnoLabel;
	@FXML
	private Label bodoviLabel;
	@FXML
	private Label popustBodoviLabel;
	@FXML
	private Label finalnaCijenaLabel;
	@FXML
	private ToggleGroup nacinPlacanjaGroup;

	private ObservableList<Stavka> stavke = FXCollections.observableArrayList();
	private int trenutniBodovi = 0;

	private int getFilijalaId() {
		return UserSession.getInstance().getFilijalaId();
	}

	private int getProdavacId() {
		return UserSession.getInstance().getProdavacId();
	}

	@FXML
	public void initialize() {
		ucitajProizvode();
		postaviTabelu();
		ukupnoLabel.setText("Ukupno: 0.00 KM");
		bodoviLabel.setText("Bodovi: -");
		popustBodoviLabel.setText("Popust od bodova: 0.00 KM");
		finalnaCijenaLabel.setText("Finalna cijena: 0.00 KM");

		karticaIdField.textProperty().addListener((observable, oldValue, newValue) -> {
			provjeriKarticu();
		});

		koristiBodoveCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			azurirajPrikaz();
		});
	}

	private void ucitajProizvode() {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT p.proizvod_id, p.naziv " + "FROM proizvod p "
						+ "JOIN zaliha z ON p.proizvod_id = z.proizvod_id "
						+ "WHERE z.filijala_id = ? AND z.kolicina > 0")) {

			stmt.setInt(1, getFilijalaId());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				proizvodComboBox.getItems().add(rs.getInt("proizvod_id") + " - " + rs.getString("naziv"));
			}
		} catch (SQLException e) {
			prikaziUpozorenje("Greška pri učitavanju proizvoda.");
		}
	}

	private void postaviTabelu() {
		TableColumn<Stavka, Integer> proizvodIdCol = new TableColumn<>("Proizvod ID");
		proizvodIdCol.setCellValueFactory(new PropertyValueFactory<>("proizvodId"));

		TableColumn<Stavka, Integer> kolicinaCol = new TableColumn<>("Količina");
		kolicinaCol.setCellValueFactory(new PropertyValueFactory<>("kolicina"));

		TableColumn<Stavka, Double> cijenaCol = new TableColumn<>("Cijena");
		cijenaCol.setCellValueFactory(new PropertyValueFactory<>("cijena"));

		TableColumn<Stavka, Double> popustCol = new TableColumn<>("Popust %");
		popustCol.setCellValueFactory(new PropertyValueFactory<>("popust"));

		TableColumn<Stavka, Double> ukupnoCol = new TableColumn<>("Ukupno");
		ukupnoCol.setCellValueFactory(new PropertyValueFactory<>("ukupnoZaStavku"));

		TableColumn<Stavka, String> reklamacijaCol = new TableColumn<>("Reklamacija");
		reklamacijaCol.setCellValueFactory(new PropertyValueFactory<>("reklamacija"));

		stavkeTable.getColumns().addAll(proizvodIdCol, kolicinaCol, cijenaCol, popustCol, ukupnoCol, reklamacijaCol);
		stavkeTable.setItems(stavke);
	}

	@FXML
	public void dodajStavkuNaRacun() {
		String selected = proizvodComboBox.getValue();
		String kolicinaText = kolicinaField.getText().trim();

		if (selected == null || kolicinaText.isEmpty()) {
			prikaziUpozorenje("Odaberite proizvod i unesite količinu.");
			return;
		}

		int proizvodId = Integer.parseInt(selected.split(" - ")[0]);
		int kolicina = Integer.parseInt(kolicinaText);

		if (!provjeriZalihe(proizvodId, kolicina))
			return;

		double osnovnaCijena = dohvatiCijenuProizvoda(proizvodId);
		if (osnovnaCijena == 0) {
			prikaziUpozorenje("Greška pri dohvaćanju cijene proizvoda.");
			return;
		}

		double popustProcenat = dohvatiPromocionPopust(proizvodId);
		String reklamacija = prikaziDialogZaReklamaciju();

		Stavka novaStavka = new Stavka(proizvodId, kolicina, osnovnaCijena, popustProcenat, reklamacija);
		stavke.add(novaStavka);

		kolicinaField.clear();
		azurirajPrikaz();
	}

	private double dohvatiCijenuProizvoda(int proizvodId) {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT cijena FROM proizvod WHERE proizvod_id = ?")) {

			stmt.setInt(1, proizvodId);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getDouble("cijena");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private double dohvatiPromocionPopust(int proizvodId) {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT p.vrijednost_popusta " + "FROM promocija p "
						+ "JOIN promocija_proizvod pp ON p.promocija_id = pp.promocija_id "
						+ "WHERE pp.proizvod_id = ? AND ? BETWEEN p.datum_pocetka AND p.datum_zavrsetka")) {

			stmt.setInt(1, proizvodId);
			stmt.setDate(2, Date.valueOf(LocalDate.now()));
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getDouble("vrijednost_popusta");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private String prikaziDialogZaReklamaciju() {
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Reklamacija");
		dialog.setHeaderText("Izaberite razlog reklamacije:");

		ButtonType potvrdiButton = new ButtonType("Potvrdi", ButtonBar.ButtonData.OK_DONE);
		ButtonType preskociButton = new ButtonType("Preskoči", ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes().addAll(potvrdiButton, preskociButton);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		//postojeci razlozi
		ComboBox<String> razloziComboBox = new ComboBox<>();
		ucitajRazlogeReklamacija(razloziComboBox);
		razloziComboBox.setPromptText("Izaberite razlog...");

		//custom reklamacija
		TextField customReklamacijaField = new TextField();
		customReklamacijaField.setPromptText("Unesite opis...");
		customReklamacijaField.setDisable(true);

		razloziComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null && newVal.startsWith("3:")) { // ID 3 je "Drugo"
				customReklamacijaField.setDisable(false);
			} else {
				customReklamacijaField.setDisable(true);
				customReklamacijaField.clear();
			}
		});

		grid.add(new Label("Razlog reklamacije:"), 0, 0);
		grid.add(razloziComboBox, 1, 0);
		grid.add(new Label("Opis (za 'Drugo'):"), 0, 1);
		grid.add(customReklamacijaField, 1, 1);

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == potvrdiButton) {
				if (razloziComboBox.getValue() != null) {
					if (razloziComboBox.getValue().startsWith("3:")
							&& !customReklamacijaField.getText().trim().isEmpty()) {
						return "3:" + customReklamacijaField.getText().trim();
					}
					return razloziComboBox.getValue();
				}
			}
			return "Nema";
		});

		Optional<String> result = dialog.showAndWait();
		return result.orElse("Nema");
	}

	private void ucitajRazlogeReklamacija(ComboBox<String> comboBox) {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT razlog_id, naziv FROM razlog_reklamacije");
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				comboBox.getItems().add(rs.getInt("razlog_id") + ":" + rs.getString("naziv"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void provjeriKarticu() {
		String karticaText = karticaIdField.getText().trim();

		if (karticaText.isEmpty()) {
			bodoviLabel.setText("Bodovi: -");
			trenutniBodovi = 0;
			azurirajPrikaz();
			return;
		}

		try {
			int karticaId = Integer.parseInt(karticaText);

			try (Connection conn = DBConnection.getConnection();
					PreparedStatement stmt = conn
							.prepareStatement("SELECT broj_bodova FROM lojalti_kartica WHERE kartica_id = ?")) {

				stmt.setInt(1, karticaId);
				ResultSet rs = stmt.executeQuery();

				if (rs.next()) {
					trenutniBodovi = rs.getInt("broj_bodova");
					bodoviLabel.setText("Bodovi: " + trenutniBodovi);
					bodoviLabel.setStyle("-fx-text-fill: green;");
				} else {
					bodoviLabel.setText("Bodovi: Kartica ne postoji!");
					bodoviLabel.setStyle("-fx-text-fill: red;");
					trenutniBodovi = 0;
				}

				azurirajPrikaz();

			} catch (SQLException e) {
				bodoviLabel.setText("Bodovi: Greška pri provjeri");
				bodoviLabel.setStyle("-fx-text-fill: red;");
				trenutniBodovi = 0;
			}

		} catch (NumberFormatException e) {
			bodoviLabel.setText("Bodovi: Neispravan ID");
			bodoviLabel.setStyle("-fx-text-fill: red;");
			trenutniBodovi = 0;
		}
	}

	private void azurirajPrikaz() {
		double ukupno = 0;
		for (Stavka s : stavke) {
			ukupno += s.getUkupnoZaStavku();
		}

		ukupnoLabel.setText(String.format("Ukupno: %.2f KM", ukupno));

		double popustOdBodova = 0;
		double finalnaCijena = ukupno;

		if (koristiBodoveCheckBox.isSelected() && trenutniBodovi > 0) {
			int maxPopustBodova = Math.min(trenutniBodovi, (int) (ukupno * 10));
			popustOdBodova = maxPopustBodova * 0.10;
			finalnaCijena = ukupno - popustOdBodova;

			popustBodoviLabel.setText(String.format("Popust od bodova: %.2f KM (koristit će se %d bodova)",
					popustOdBodova, maxPopustBodova));
			popustBodoviLabel.setStyle("-fx-text-fill: green;");
		} else {
			popustBodoviLabel.setText("Popust od bodova: 0.00 KM");
			popustBodoviLabel.setStyle("-fx-text-fill: black;");

			if (!karticaIdField.getText().trim().isEmpty() && trenutniBodovi >= 0) {
				int bodoviZaDodati = (int) (ukupno / 10);
				popustBodoviLabel
						.setText(String.format("Popust od bodova: 0.00 KM (dobit ćete %d bodova)", bodoviZaDodati));
			}
		}

		finalnaCijenaLabel.setText(String.format("Finalna cijena: %.2f KM", finalnaCijena));
		finalnaCijenaLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF5722;");
	}

	private boolean provjeriZalihe(int proizvodId, int kolicina) {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn
						.prepareStatement("SELECT kolicina FROM zaliha WHERE proizvod_id = ? AND filijala_id = ?")) {

			stmt.setInt(1, proizvodId);
			stmt.setInt(2, getFilijalaId());
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				int dostupno = rs.getInt("kolicina");
				if (kolicina > dostupno) {
					prikaziUpozorenje("Nema dovoljno zaliha. Dostupno: " + dostupno);
					return false;
				}
			} else {
				prikaziUpozorenje("Nema podataka o zalihama za izabrani proizvod.");
				return false;
			}
		} catch (SQLException e) {
			prikaziUpozorenje("Greška pri provjeri zaliha.");
			return false;
		}
		return true;
	}

	@FXML
	public void zavrsiProdaju() {
		if (stavke.isEmpty()) {
			prikaziUpozorenje("Račun je prazan.");
			return;
		}

		RadioButton selectedRadioButton = (RadioButton) nacinPlacanjaGroup.getSelectedToggle();
		String nacinPlacanja = selectedRadioButton.getText();
		int nacinPlacanjaId = nacinPlacanja.equals("Gotovina") ? 1 : 2;

		double popustOdBodova = 0;
		Integer karticaId = null;
		boolean koristiBodove = koristiBodoveCheckBox.isSelected();

		if (!karticaIdField.getText().trim().isEmpty()) {
			try {
				karticaId = Integer.parseInt(karticaIdField.getText().trim());
			} catch (NumberFormatException e) {
				prikaziUpozorenje("Neispravan ID kartice.");
				return;
			}
		}

		try (Connection conn = DBConnection.getConnection()) {
			conn.setAutoCommit(false);

			double ukupno = 0;
			for (Stavka s : stavke) {
				ukupno += s.getUkupnoZaStavku();
			}

			if (koristiBodove && karticaId != null && trenutniBodovi > 0) {
				int maxPopustBodova = Math.min(trenutniBodovi, (int) (ukupno * 10));
				popustOdBodova = maxPopustBodova * 0.10;
				ukupno -= popustOdBodova;
			}

			// (trigger će umanjiti bodove ako se koriste)
			PreparedStatement psProdaja = conn.prepareStatement(
					"INSERT INTO prodaja (datum, prodavac_id, filijala_id, nacin_placanja_id, kartica_id, popust_od_bodova) VALUES (?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			psProdaja.setDate(1, Date.valueOf(LocalDate.now()));
			psProdaja.setInt(2, getProdavacId());
			psProdaja.setInt(3, getFilijalaId());
			psProdaja.setInt(4, nacinPlacanjaId);
			if (karticaId != null) {
				psProdaja.setInt(5, karticaId);
			} else {
				psProdaja.setNull(5, Types.INTEGER);
			}
			psProdaja.setDouble(6, popustOdBodova);
			psProdaja.executeUpdate();

			ResultSet rs = psProdaja.getGeneratedKeys();
			rs.next();
			int prodajaId = rs.getInt(1);

			for (Stavka s : stavke) {
				PreparedStatement psStavka = conn.prepareStatement(
						"INSERT INTO stavka_prodaje (prodaja_id, proizvod_id, kolicina, cijena, popust) "
								+ "VALUES (?, ?, ?, ?, ?)");
				psStavka.setInt(1, prodajaId);
				psStavka.setInt(2, s.getProizvodId());
				psStavka.setInt(3, s.getKolicina());
				psStavka.setDouble(4, s.getCijena());
				double popustKM = (s.getCijena() * s.getKolicina()) * (s.getPopust() / 100.0);
				psStavka.setDouble(5, popustKM);
				psStavka.executeUpdate();

				if (s.getReklamacija() != null && !s.getReklamacija().equals("Nema")) {
					String reklamacijaText = s.getReklamacija();
					int razlogId = Integer.parseInt(reklamacijaText.split(":")[0]);
					String opis = null;

					// ako je razlog "Drugo" (ID 3) i ima dodatni opis
					if (razlogId == 3 && reklamacijaText.contains(":") && reklamacijaText.split(":").length > 1) {
						opis = reklamacijaText.substring(reklamacijaText.indexOf(":") + 1);
					}

					PreparedStatement psStavkaId = conn.prepareStatement("SELECT LAST_INSERT_ID() as stavka_id");
					ResultSet rsStavka = psStavkaId.executeQuery();
					if (rsStavka.next()) {
						int stavkaId = rsStavka.getInt("stavka_id");

						PreparedStatement psReklamacija = conn.prepareStatement(
								"INSERT INTO reklamacija (datum, proizvod_id, razlog_id, stavka_prodaje_id, opis) "
										+ "VALUES (?, ?, ?, ?, ?)");
						psReklamacija.setDate(1, Date.valueOf(LocalDate.now()));
						psReklamacija.setInt(2, s.getProizvodId());
						psReklamacija.setInt(3, razlogId);
						psReklamacija.setInt(4, stavkaId);

						if (opis != null) {
							psReklamacija.setString(5, opis);
						} else {
							psReklamacija.setNull(5, Types.VARCHAR);
						}

						psReklamacija.executeUpdate();
					}
				}
			}

			if (karticaId != null && popustOdBodova == 0) {
				// ukupna vrijednost prodaje
				double ukupnaVrijednostZaBodove = 0;
				for (Stavka s : stavke) {
					ukupnaVrijednostZaBodove += s.getUkupnoZaStavku();
				}

				// 1 bod za svakih 10 KM
				int bodoviZaDodati = (int) Math.floor(ukupnaVrijednostZaBodove / 10);

				// dodaj bodove na karticu ako ima bodova za dodati  - procedura
				if (bodoviZaDodati > 0) {
					try (CallableStatement cs = conn.prepareCall("{call dodaj_bodove_na_karticu(?, ?, ?)}")) {
						cs.setInt(1, karticaId);
						cs.setDouble(2, ukupnaVrijednostZaBodove);
						cs.registerOutParameter(3, Types.INTEGER);
						cs.execute();

						int noviBodovi = cs.getInt(3);
						System.out.println("Novi broj bodova na kartici: " + noviBodovi);
					}
				}
			}

			conn.commit();
			prikaziUpozorenje("Prodaja uspješno završena!");
			stavke.clear();
			azurirajPrikaz();

			karticaIdField.clear();
			koristiBodoveCheckBox.setSelected(false);
			trenutniBodovi = 0;

		} catch (SQLException e) {
			e.printStackTrace();
			prikaziUpozorenje("Greška pri završavanju prodaje.");
		}
	}

	private void prikaziUpozorenje(String poruka) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Obavještenje");
		alert.setHeaderText(null);
		alert.setContentText(poruka);
		alert.showAndWait();
	}

	public static class Stavka {
		private final int proizvodId;
		private final int kolicina;
		private final double cijena;
		private final double popust;
		private final String reklamacija;

		public Stavka(int proizvodId, int kolicina, double cijena, double popust, String reklamacija) {
			this.proizvodId = proizvodId;
			this.kolicina = kolicina;
			this.cijena = cijena;
			this.popust = popust;
			this.reklamacija = reklamacija;
		}

		public int getProizvodId() {
			return proizvodId;
		}

		public int getKolicina() {
			return kolicina;
		}

		public double getCijena() {
			return cijena;
		}

		public double getPopust() {
			return popust;
		}

		public String getReklamacija() {
			return reklamacija;
		}

		public double getUkupnoZaStavku() {
			double osnovnoUkupno = cijena * kolicina;
			double popustIznos = osnovnoUkupno * (popust / 100.0);
			return osnovnoUkupno - popustIznos;
		}
	}
}