package gui;

import application.DBConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProdajaController {

    @FXML
    private VBox prodajeBox;
    @FXML
    private Label ukupnoLabel;
    @FXML
    private Label ukupanIznosLabel;
    @FXML
    private Label filijalaLabel;
    @FXML
    private Label korisnikLabel;
    @FXML
    private DatePicker datumOdPicker;
    @FXML
    private DatePicker datumDoPicker;
    @FXML
    private Button filterButton;
    @FXML
    private Button resetFilterButton;

    // mjesecni pregled
    @FXML
    private TabPane glavniTabPane;
    @FXML
    private Tab pojedinacneTab;
    @FXML
    private Tab mjesecniTab;
    @FXML
    private ScrollPane pojedinacneScrollPane;
    @FXML
    private ScrollPane mjesecniScrollPane;
    @FXML
    private VBox mjesecniBox;
    @FXML
    private ComboBox<String> godinaComboBox;
    @FXML
    private ComboBox<String> mjesecComboBox;
    @FXML
    private Button filterMjesecniButton;
    @FXML
    private Button resetMjesecniButton;
    @FXML
    private VBox pojedinacneStatsBox;
    @FXML
    private VBox mjesecneStatsBox;
    @FXML
    private Label ukupnoMjesecnoLabel;
    @FXML
    private Label ukupanIznosMjesecnoLabel;
    @FXML
    private Label prosjekMjesecnoLabel;

    @FXML
    public void initialize() {
        postaviInformacijeKorisnika();
        postaviListenere();
        pripremiMjesecComboBox();
        pripremiGodinaComboBox();
        prikaziSveProdaje();
    }

    private void postaviListenere() {
        filterButton.setOnAction(e -> filtrirajProdaje());
        resetFilterButton.setOnAction(e -> resetujFiltere());

        filterMjesecniButton.setOnAction(e -> filtrirajMjesecniPregled());
        resetMjesecniButton.setOnAction(e -> resetujMjesecneFiltere());

        // listener za promjenu taba
        glavniTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == mjesecniTab) {
                prikaziMjesecniPregled();
                pojedinacneScrollPane.setVisible(false);
                mjesecniScrollPane.setVisible(true);
                pojedinacneStatsBox.setVisible(false);
                mjesecneStatsBox.setVisible(true);
            } else {
                prikaziSveProdaje();
                pojedinacneScrollPane.setVisible(true);
                mjesecniScrollPane.setVisible(false);
                pojedinacneStatsBox.setVisible(true);
                mjesecneStatsBox.setVisible(false);
            }
        });
    }

    private void pripremiMjesecComboBox() {
        String[] mjeseci = {
            "Svi mjeseci", "Januar", "Februar", "Mart", "April", "Maj", "Juni",
            "Juli", "Avgust", "Septembar", "Oktobar", "Novembar", "Decembar"
        };
        mjesecComboBox.getItems().addAll(mjeseci);
        mjesecComboBox.setValue("Svi mjeseci");
    }

    private void pripremiGodinaComboBox() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT DISTINCT YEAR(datum) as godina FROM prodaja ORDER BY godina DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            List<String> godine = new ArrayList<>();
            godine.add("Sve godine");
            
            while (rs.next()) {
                godine.add(String.valueOf(rs.getInt("godina")));
            }

            godinaComboBox.getItems().addAll(godine);
            godinaComboBox.setValue("Sve godine");

        } catch (Exception e) {
            e.printStackTrace();
            godinaComboBox.getItems().add("Sve godine");
            godinaComboBox.setValue("Sve godine");
        }
    }

    private void filtrirajMjesecniPregled() {
        prikaziMjesecniPregled();
    }

    private void resetujMjesecneFiltere() {
        godinaComboBox.setValue("Sve godine");
        mjesecComboBox.setValue("Svi mjeseci");
        prikaziMjesecniPregled();
    }

    private void prikaziMjesecniPregled() {
        if (!UserSession.getInstance().isLoggedIn()) {
            new Alert(Alert.AlertType.WARNING, "Niste ulogovani.").showAndWait();
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("""
                SELECT * FROM v_mjesecna_prodaja 
                WHERE filijala = (SELECT naziv FROM filijala WHERE filijala_id = ?)
                """);

            // dodavanje filtera za godinu
            String selectedGodina = godinaComboBox.getValue();
            if (selectedGodina != null && !selectedGodina.equals("Sve godine")) {
                sql.append(" AND godina = ?");
            }

            // dodavanje filtera za mjesec
            String selectedMjesec = mjesecComboBox.getValue();
            if (selectedMjesec != null && !selectedMjesec.equals("Svi mjeseci")) {
                int mjesecBroj = getMjesecBroj(selectedMjesec);
                sql.append(" AND mjesec = ?");
            }

            sql.append(" ORDER BY godina DESC, mjesec DESC");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            ps.setInt(1, UserSession.getInstance().getFilijalaId());

            int paramIndex = 2;
            if (selectedGodina != null && !selectedGodina.equals("Sve godine")) {
                ps.setInt(paramIndex++, Integer.parseInt(selectedGodina));
            }
            if (selectedMjesec != null && !selectedMjesec.equals("Svi mjeseci")) {
                ps.setInt(paramIndex, getMjesecBroj(selectedMjesec));
            }

            ResultSet rs = ps.executeQuery();
            mjesecniBox.getChildren().clear();

            int ukupnoMjeseci = 0;
            double ukupanIznos = 0;

            while (rs.next()) {
                int godina = rs.getInt("godina");
                int mjesec = rs.getInt("mjesec");
                String filijala = rs.getString("filijala");
                int brojRacuna = rs.getInt("broj_racuna");
                double brutoProdaja = rs.getDouble("ukupna_bruto_prodaja");
                double popust = rs.getDouble("ukupan_popust");
                double netoProdaja = rs.getDouble("ukupna_neto_prodaja");
                double prosjekRacuna = rs.getDouble("prosjecna_vrijednost_racuna");
                int brojKartica = rs.getInt("broj_kartica_koristenja");

                VBox mjesecnaKarta = kreirajMjesecnuKartu(godina, mjesec, filijala, brojRacuna,
                        brutoProdaja, popust, netoProdaja, prosjekRacuna, brojKartica);
                mjesecniBox.getChildren().add(mjesecnaKarta);

                ukupnoMjeseci++;
                ukupanIznos += netoProdaja;
            }

            // Azuriranje
            ukupnoMjesecnoLabel.setText("Ukupno mjeseci: " + ukupnoMjeseci);
            ukupanIznosMjesecnoLabel.setText(String.format("Ukupan iznos: %.2f KM", ukupanIznos));
            
            if (ukupnoMjeseci > 0) {
                double prosjek = ukupanIznos / ukupnoMjeseci;
                prosjekMjesecnoLabel.setText(String.format("Prosjek po mjesecu: %.2f KM", prosjek));
            } else {
                prosjekMjesecnoLabel.setText("Prosjek po mjesecu: 0.00 KM");
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Greška pri učitavanju mjesečnog pregleda.").showAndWait();
        }
    }

    private VBox kreirajMjesecnuKartu(int godina, int mjesec, String filijala, int brojRacuna,
                                     double brutoProdaja, double popust, double netoProdaja,
                                     double prosjekRacuna, int brojKartica) {
        VBox karta = new VBox();
        karta.setSpacing(15);
        karta.setPadding(new Insets(20));
        karta.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1;");

        // header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Rectangle ikona = new Rectangle(50, 50);
        ikona.setFill(Color.web("#9b59b6"));
        ikona.setArcWidth(8);
        ikona.setArcHeight(8);

        VBox ikonaBox = new VBox();
        ikonaBox.setAlignment(Pos.CENTER);
        ikonaBox.getChildren().add(ikona);
        Label ikonaText = new Label("📊");
        ikonaText.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
        ikonaBox.getChildren().add(ikonaText);
        ikonaBox.setSpacing(-40);

        VBox infoBox = new VBox();
        infoBox.setSpacing(5);

        Label naslov = new Label(getMjesecNaziv(mjesec) + " " + godina);
        naslov.setFont(Font.font("System", FontWeight.BOLD, 18));
        naslov.setStyle("-fx-text-fill: #2c3e50;");

        Label filijalaInfo = new Label("Filijala: " + filijala);
        filijalaInfo.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        infoBox.getChildren().addAll(naslov, filijalaInfo);

        Label statusBadge = new Label("MJESEČNI IZVJEŠTAJ");
        statusBadge.setStyle("-fx-background-color: #9b59b6;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 5 10 5 10;" +
                "-fx-background-radius: 15;" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(ikonaBox, infoBox, spacer, statusBadge);

       
        GridPane statistike = new GridPane();
        statistike.setHgap(30);
        statistike.setVgap(15);
        statistike.setStyle("-fx-padding: 20; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // prva kolona
        statistike.add(kreirajStatistikuLabel("Broj računa:", String.valueOf(brojRacuna), "#3498db"), 0, 0);
        statistike.add(kreirajStatistikuLabel("Korišćene kartice:", String.valueOf(brojKartica), "#f39c12"), 0, 1);
        statistike.add(kreirajStatistikuLabel("Ukupan popust:", String.format("%.2f KM", popust), "#e74c3c"), 0, 2);

        // druga kolona
        statistike.add(kreirajStatistikuLabel("Neto prodaja:", String.format("%.2f KM", netoProdaja), "#27ae60"), 1, 0);
        statistike.add(kreirajStatistikuLabel("Prosjek po računu:", String.format("%.2f KM", prosjekRacuna), "#9b59b6"), 1, 1);
        

        karta.getChildren().addAll(header, new Separator(), statistike);
        return karta;
    }

    private VBox kreirajStatistikuLabel(String naziv, String vrijednost, String boja) {
        VBox box = new VBox();
        box.setSpacing(3);

        Label nazivLabel = new Label(naziv);
        nazivLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

        Label vrijednostLabel = new Label(vrijednost);
        vrijednostLabel.setStyle("-fx-text-fill: " + boja + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        box.getChildren().addAll(nazivLabel, vrijednostLabel);
        return box;
    }

    private int getMjesecBroj(String mjesecNaziv) {
        String[] mjeseci = {
            "", "Januar", "Februar", "Mart", "April", "Maj", "Juni",
            "Juli", "Avgust", "Septembar", "Oktobar", "Novembar", "Decembar"
        };
        
        for (int i = 1; i < mjeseci.length; i++) {
            if (mjeseci[i].equals(mjesecNaziv)) {
                return i;
            }
        }
        return 1;
    }

    private String getMjesecNaziv(int mjesecBroj) {
        String[] mjeseci = {
            "", "Januar", "Februar", "Mart", "April", "Maj", "Juni",
            "Juli", "Avgust", "Septembar", "Oktobar", "Novembar", "Decembar"
        };
        
        if (mjesecBroj >= 1 && mjesecBroj <= 12) {
            return mjeseci[mjesecBroj];
        }
        return "Nepoznat";
    }

    
    private void filtrirajProdaje() {
        LocalDate datumOd = datumOdPicker.getValue();
        LocalDate datumDo = datumDoPicker.getValue();

        if (datumOd != null && datumDo != null && datumOd.isAfter(datumDo)) {
            new Alert(Alert.AlertType.WARNING, "Datum 'od' ne može biti posle datuma 'do'").showAndWait();
            return;
        }

        prikaziSveProdaje();
    }

    private void resetujFiltere() {
        datumOdPicker.setValue(null);
        datumDoPicker.setValue(null);
        prikaziSveProdaje();
    }

    private void postaviInformacijeKorisnika() {
        if (!UserSession.getInstance().isLoggedIn()) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT naziv FROM filijala WHERE filijala_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, UserSession.getInstance().getFilijalaId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String nazivFilijale = rs.getString("naziv");
                if (filijalaLabel != null) {
                    filijalaLabel.setText("Filijala: " + nazivFilijale);
                    filijalaLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                }
            }

            if (korisnikLabel != null) {
                korisnikLabel.setText("Korisnik: " + UserSession.getInstance().getImePrezime());
                korisnikLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (filijalaLabel != null) {
                filijalaLabel.setText("Filijala: Nepoznata");
            }
        }
    }

    private void prikaziSveProdaje() {
        if (!UserSession.getInstance().isLoggedIn()) {
            new Alert(Alert.AlertType.WARNING, "Niste ulogovani.").showAndWait();
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("""
                SELECT p.prodaja_id, p.datum, k.ime, k.prezime, f.naziv AS filijala,
                       np.naziv AS nacin_placanja, lk.kartica_id, p.popust_od_bodova
                FROM prodaja p
                JOIN korisnik k ON p.prodavac_id = k.korisnik_id
                JOIN filijala f ON p.filijala_id = f.filijala_id
                JOIN nacin_placanja np ON p.nacin_placanja_id = np.nacin_placanja_id
                LEFT JOIN lojalti_kartica lk ON p.kartica_id = lk.kartica_id
                WHERE p.filijala_id = ?
                """);

            LocalDate datumOd = datumOdPicker.getValue();
            LocalDate datumDo = datumDoPicker.getValue();

            if (datumOd != null) {
                sql.append(" AND p.datum >= ?");
            }
            if (datumDo != null) {
                sql.append(" AND p.datum <= ?");
            }

            sql.append(" ORDER BY p.datum DESC");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            ps.setInt(1, UserSession.getInstance().getFilijalaId());

            int paramIndex = 2;
            if (datumOd != null) {
                ps.setDate(paramIndex++, Date.valueOf(datumOd));
            }
            if (datumDo != null) {
                ps.setDate(paramIndex, Date.valueOf(datumDo));
            }

            ResultSet rs = ps.executeQuery();
            prodajeBox.getChildren().clear();
            
            int brojProdaja = 0;
            double ukupanIznos = 0;

            while (rs.next()) {
                int prodajaId = rs.getInt("prodaja_id");
                String datum = rs.getString("datum");
                String prodavac = rs.getString("ime") + " " + rs.getString("prezime");
                String filijala = rs.getString("filijala");
                String nacinPlacanja = rs.getString("nacin_placanja");
                Integer karticaId = rs.getObject("kartica_id") != null ? rs.getInt("kartica_id") : null;
                double popustOdBodova = rs.getDouble("popust_od_bodova");

                VBox racunKarta = kreirajRacunKartu(conn, prodajaId, datum, prodavac, filijala, nacinPlacanja,
                        karticaId, popustOdBodova);
                prodajeBox.getChildren().add(racunKarta);

                brojProdaja++;
                ukupanIznos += izracunajFinalnuCijenu(conn, prodajaId, popustOdBodova);
            }

            if (ukupnoLabel != null) {
                ukupnoLabel.setText("Ukupno prodaja: " + brojProdaja);
            }
            if (ukupanIznosLabel != null) {
                ukupanIznosLabel.setText(String.format("Ukupan iznos: %.2f KM", ukupanIznos));
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Greška pri učitavanju prodaja.").showAndWait();
        }
    }

    private double izracunajFinalnuCijenu(Connection conn, int prodajaId, double popustOdBodova) throws SQLException {
        String sql = """
                SELECT SUM((s.kolicina * s.cijena) - s.popust) as ukupno_stavke
                FROM stavka_prodaje s
                WHERE s.prodaja_id = ?
                """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, prodajaId);
        ResultSet rs = ps.executeQuery();

        double ukupnoStavke = 0;
        if (rs.next()) {
            ukupnoStavke = rs.getDouble("ukupno_stavke");
        }

        return ukupnoStavke - popustOdBodova;
    }


    private VBox kreirajRacunKartu(Connection conn, int prodajaId, String datum, String prodavac, String filijala,
            String nacinPlacanja, Integer karticaId, double popustOdBodova) throws SQLException {
        VBox karta = new VBox();
        karta.setSpacing(15);
        karta.setPadding(new Insets(20));
        karta.setStyle("-fx-background-color: white;" + "-fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);" + "-fx-border-color: #e0e0e0;"
                + "-fx-border-radius: 10;" + "-fx-border-width: 1;");

        HBox header = kreirajHeaderRacuna(prodajaId, datum, prodavac, filijala, nacinPlacanja, karticaId);

        Separator separator1 = new Separator();
        separator1.setStyle("-fx-background-color: #bdc3c7;");
        VBox stavkeBox = kreirajStavkeBox(conn, prodajaId);
        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: #bdc3c7;");
        HBox footer = kreirajFooterRacuna(conn, prodajaId, karticaId, popustOdBodova);
        karta.getChildren().addAll(header, separator1, stavkeBox, separator2, footer);

        return karta;
    }

    private HBox kreirajHeaderRacuna(int prodajaId, String datum, String prodavac, String filijala,
            String nacinPlacanja, Integer karticaId) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Rectangle ikona = new Rectangle(40, 40);
        ikona.setFill(Color.web("#3498db"));
        ikona.setArcWidth(8);
        ikona.setArcHeight(8);
        VBox ikonaBox = new VBox();
        ikonaBox.setAlignment(Pos.CENTER);
        ikonaBox.getChildren().add(ikona);
        Label ikonaText = new Label("R");
        ikonaText.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        ikonaBox.getChildren().add(ikonaText);
        ikonaBox.setSpacing(-30);

        VBox infoBox = new VBox();
        infoBox.setSpacing(5);
        Label racunBroj = new Label("Račun #" + prodajaId);
        racunBroj.setFont(Font.font("System", FontWeight.BOLD, 16));
        racunBroj.setStyle("-fx-text-fill: #2c3e50;");
        Label datumProdaje = new Label("Datum: " + datum);
        datumProdaje.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        Label zaposleni = new Label("Prodavac: " + prodavac);
        zaposleni.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        Label filijalaLabel = new Label("Filijala: " + filijala);
        filijalaLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        HBox placanjeBox = new HBox();
        placanjeBox.setSpacing(5);
        placanjeBox.setAlignment(Pos.CENTER_LEFT);
        Label placanjeIkona = new Label();
        placanjeIkona.setStyle("-fx-font-size: 16px;");
        Label placanjeLabel = new Label("Plaćanje: " + nacinPlacanja);

        if (nacinPlacanja.equalsIgnoreCase("gotovina")) {
            placanjeIkona.setText("💰");
            placanjeLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else if (nacinPlacanja.equalsIgnoreCase("kartica")) {
            placanjeIkona.setText("💳");
            placanjeLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            placanjeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        }

        placanjeBox.getChildren().addAll(placanjeIkona, placanjeLabel);
        infoBox.getChildren().addAll(racunBroj, datumProdaje, zaposleni, filijalaLabel, placanjeBox);

        if (karticaId != null) {
            Label karticaLabel = new Label("Lojalti kartica: #" + karticaId);
            karticaLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px; -fx-font-weight: bold;");
            infoBox.getChildren().add(karticaLabel);
        }

        Label statusBadge = new Label("ZAVRŠENO");
        statusBadge.setStyle("-fx-background-color: #27ae60;" + "-fx-text-fill: white;" + "-fx-padding: 5 10 5 10;"
                + "-fx-background-radius: 15;" + "-fx-font-size: 10px;" + "-fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(ikonaBox, infoBox, spacer, statusBadge);
        return header;
    }

    private VBox kreirajStavkeBox(Connection conn, int prodajaId) throws SQLException {
        VBox stavkeBox = new VBox();
        stavkeBox.setSpacing(8);

        HBox headerTabele = new HBox();
        headerTabele.setSpacing(10);
        headerTabele.setAlignment(Pos.CENTER_LEFT);
        headerTabele.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 8; -fx-background-radius: 5;");
        Label proizvod = new Label("Proizvod");
        proizvod.setFont(Font.font("System", FontWeight.BOLD, 12));
        proizvod.setPrefWidth(180);
        Label kolicina = new Label("Količina");
        kolicina.setFont(Font.font("System", FontWeight.BOLD, 12));
        kolicina.setPrefWidth(70);
        kolicina.setAlignment(Pos.CENTER);
        Label osnovna = new Label("Osnovna");
        osnovna.setFont(Font.font("System", FontWeight.BOLD, 12));
        osnovna.setPrefWidth(70);
        osnovna.setAlignment(Pos.CENTER_RIGHT);
        Label popust = new Label("Popust");
        popust.setFont(Font.font("System", FontWeight.BOLD, 12));
        popust.setPrefWidth(150);
        popust.setAlignment(Pos.CENTER_RIGHT);
        Label ukupno = new Label("Ukupno");
        ukupno.setFont(Font.font("System", FontWeight.BOLD, 12));
        ukupno.setPrefWidth(80);
        ukupno.setAlignment(Pos.CENTER_RIGHT);
        headerTabele.getChildren().addAll(proizvod, kolicina, osnovna, popust, ukupno);
        stavkeBox.getChildren().add(headerTabele);

        String stavkeSql = """
                SELECT s.prodaja_id, s.kolicina, s.cijena, s.popust, pr.naziv
                FROM stavka_prodaje s
                JOIN proizvod pr ON s.proizvod_id = pr.proizvod_id
                WHERE s.prodaja_id = ?
                """;

        PreparedStatement stavkePs = conn.prepareStatement(stavkeSql);
        stavkePs.setInt(1, prodajaId);
        ResultSet stavkeRs = stavkePs.executeQuery();

        while (stavkeRs.next()) {
            int stavkaId = stavkeRs.getInt("prodaja_id");
            String nazivProizvoda = stavkeRs.getString("naziv");
            int kolicinaStavke = stavkeRs.getInt("kolicina");
            double cijenaUStavci = stavkeRs.getDouble("cijena");
            double popustKM = stavkeRs.getDouble("popust");

            double osnovnaVrijednost = kolicinaStavke * cijenaUStavci;
            double ukupnaStavka = osnovnaVrijednost - popustKM;

            HBox stavkaRow = kreirajStavkuRed(nazivProizvoda, kolicinaStavke, cijenaUStavci, popustKM,
                    osnovnaVrijednost, ukupnaStavka);
            stavkeBox.getChildren().add(stavkaRow);

            String reklamacijaSql = """
                    SELECT rr.naziv AS razlog, r.opis
                    FROM reklamacija r
                    JOIN razlog_reklamacije rr ON r.razlog_id = rr.razlog_id
                    WHERE r.stavka_prodaje_id = ?
                    """;

            PreparedStatement reklamacijaPs = conn.prepareStatement(reklamacijaSql);
            reklamacijaPs.setInt(1, stavkaId);
            ResultSet reklamacijaRs = reklamacijaPs.executeQuery();

            if (reklamacijaRs.next()) {
                String razlog = reklamacijaRs.getString("razlog");
                String opis = reklamacijaRs.getString("opis");

                HBox reklamacijaBox = new HBox();
                reklamacijaBox.setSpacing(10);
                reklamacijaBox.setAlignment(Pos.CENTER_LEFT);
                reklamacijaBox.setStyle("-fx-padding: 5 5 5 30; -fx-background-color: #fff9e6;");

                Label reklamacijaLabel = new Label("🚨 Reklamacija: " + razlog);
                reklamacijaLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

                if (opis != null && !opis.isEmpty()) {
                    reklamacijaLabel.setText(reklamacijaLabel.getText() + " (" + opis + ")");
                }

                reklamacijaBox.getChildren().add(reklamacijaLabel);
                stavkeBox.getChildren().add(reklamacijaBox);
            }
        }

        return stavkeBox;
    }

    private HBox kreirajStavkuRed(String nazivProizvoda, int kolicina, double cijena, double popustKM,
            double osnovnaVrijednost, double ukupno) {
        HBox red = new HBox();
        red.setSpacing(10);
        red.setAlignment(Pos.CENTER_LEFT);
        red.setStyle("-fx-padding: 5 5 5 15;");

        Label proizvod = new Label(nazivProizvoda);
        proizvod.setPrefWidth(180);
        proizvod.setStyle("-fx-text-fill: #2c3e50;");

        Label kolicinaLabel = new Label(String.valueOf(kolicina) + "x");
        kolicinaLabel.setPrefWidth(70);
        kolicinaLabel.setAlignment(Pos.CENTER);
        kolicinaLabel.setStyle("-fx-text-fill: #7f8c8d;");

        Label osnovnaLabel = new Label(String.format("%.2f", osnovnaVrijednost));
        osnovnaLabel.setPrefWidth(70);
        osnovnaLabel.setAlignment(Pos.CENTER_RIGHT);
        osnovnaLabel.setStyle("-fx-text-fill: #7f8c8d;");

        Label popustLabel;
        if (popustKM > 0) {
            double popustProcenat = (popustKM / osnovnaVrijednost) * 100;
            popustLabel = new Label(String.format("-%.1f%% (%.2f KM)", popustProcenat, popustKM));
            popustLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
        } else {
            popustLabel = new Label("-");
            popustLabel.setStyle("-fx-text-fill: #7f8c8d;");
        }
        popustLabel.setPrefWidth(150);
        popustLabel.setAlignment(Pos.CENTER_RIGHT);

        Label ukupnoLabel = new Label(String.format("%.2f KM", ukupno));
        ukupnoLabel.setPrefWidth(80);
        ukupnoLabel.setAlignment(Pos.CENTER_RIGHT);
        ukupnoLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        red.getChildren().addAll(proizvod, kolicinaLabel, osnovnaLabel, popustLabel, ukupnoLabel);

        return red;
    }

    private HBox kreirajFooterRacuna(Connection conn, int prodajaId, Integer karticaId, double popustOdBodova)
            throws SQLException {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setSpacing(20);

        VBox ukupnoBox = new VBox();
        ukupnoBox.setAlignment(Pos.CENTER_RIGHT);
        ukupnoBox.setSpacing(5);

        String detaljSql = """
                SELECT
                    SUM(s.kolicina * s.cijena) as osnovnaVrednost,
                    SUM(s.popust) as promocijskiPopust,
                    SUM((s.kolicina * s.cijena) - s.popust) as vrijednostNakonPromocija
                FROM stavka_prodaje s
                WHERE s.prodaja_id = ?
                """;

        PreparedStatement ps = conn.prepareStatement(detaljSql);
        ps.setInt(1, prodajaId);
        ResultSet rs = ps.executeQuery();

        double osnovnaVrednost = 0;
        double promocijskiPopust = 0;
        double vrijednostNakonPromocija = 0;

        if (rs.next()) {
            osnovnaVrednost = rs.getDouble("osnovnaVrednost");
            promocijskiPopust = rs.getDouble("promocijskiPopust");
            vrijednostNakonPromocija = rs.getDouble("vrijednostNakonPromocija");
        }

        double finalnaCena = vrijednostNakonPromocija - popustOdBodova;

        Label osnovnaLabel = new Label(String.format("Osnovna vrijednost: %.2f KM", osnovnaVrednost));
        osnovnaLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        ukupnoBox.getChildren().add(osnovnaLabel);

        if (promocijskiPopust > 0) {
            Label promocijskiLabel = new Label(String.format("Promocijski popust: -%.2f KM", promocijskiPopust));
            promocijskiLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 11px; -fx-font-weight: bold;");
            ukupnoBox.getChildren().add(promocijskiLabel);
        }

        if (popustOdBodova > 0) {
            Label bodoviLabel = new Label(String.format("Popust od bodova: -%.2f KM", popustOdBodova));
            bodoviLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px; -fx-font-weight: bold;");
            ukupnoBox.getChildren().add(bodoviLabel);
        }

        Label ukupnoText = new Label("UKUPNO ZA PLAĆANJE");
        ukupnoText.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label finalnaCenaLabel = new Label(String.format("%.2f KM", finalnaCena));
        finalnaCenaLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        finalnaCenaLabel.setStyle("-fx-text-fill: #e74c3c;");

        ukupnoBox.getChildren().addAll(ukupnoText, finalnaCenaLabel);
        footer.getChildren().add(ukupnoBox);

        return footer;
    }
}