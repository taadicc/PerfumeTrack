package gui;

import application.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProizvodiNaPromocijiController {

    @FXML private TableView<ProizvodNaPromociji> tabelaPromocija;
    @FXML private TableColumn<ProizvodNaPromociji, String> kolonaNaziv;
    @FXML private TableColumn<ProizvodNaPromociji, Double> kolonaOriginalCijena;
    @FXML private TableColumn<ProizvodNaPromociji, Double> kolonaPopust;
    @FXML private TableColumn<ProizvodNaPromociji, Double> kolonaCijenaSaPopustom;
    @FXML private TableColumn<ProizvodNaPromociji, Double> kolonaUstedjevina;
    @FXML private TableColumn<ProizvodNaPromociji, Double> kolonaZapremina;
    @FXML private TableColumn<ProizvodNaPromociji, String> kolonaNazivPromocije;
    @FXML private TableColumn<ProizvodNaPromociji, String> kolonaOpisPromocije;
    @FXML private TableColumn<ProizvodNaPromociji, String> kolonaDatumKraja;
    @FXML private Label labelDatum;
    @FXML private Label labelBrojProizvoda;

    private ObservableList<ProizvodNaPromociji> listaProizvoda = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        updateDateLabel();
        ucitajProizvodeNaPromociji();
    }

    private void setupTableColumns() {
        kolonaNaziv.setCellValueFactory(new PropertyValueFactory<>("nazivProizvoda"));
        kolonaOriginalCijena.setCellValueFactory(new PropertyValueFactory<>("originalnaCijena"));
        kolonaPopust.setCellValueFactory(new PropertyValueFactory<>("vrijednostPopusta"));
        kolonaCijenaSaPopustom.setCellValueFactory(new PropertyValueFactory<>("cijenaSaPopustom"));
        kolonaUstedjevina.setCellValueFactory(new PropertyValueFactory<>("ustedjevina"));
        kolonaZapremina.setCellValueFactory(new PropertyValueFactory<>("zapremina"));
        kolonaNazivPromocije.setCellValueFactory(new PropertyValueFactory<>("nazivPromocije"));
        kolonaOpisPromocije.setCellValueFactory(new PropertyValueFactory<>("opisPromocije"));
        kolonaDatumKraja.setCellValueFactory(new PropertyValueFactory<>("datumZavrsetka"));
        formatCurrencyColumn(kolonaOriginalCijena);
        formatCurrencyColumn(kolonaCijenaSaPopustom);
        formatCurrencyColumn(kolonaUstedjevina);
        formatPercentageColumn(kolonaPopust);
    }

    private void formatCurrencyColumn(TableColumn<ProizvodNaPromociji, Double> column) {
        column.setCellFactory(col -> new TableCell<ProizvodNaPromociji, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f KM", item));
                }
            }
        });
    }

    private void formatPercentageColumn(TableColumn<ProizvodNaPromociji, Double> column) {
        column.setCellFactory(col -> new TableCell<ProizvodNaPromociji, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.0f%%", item));
                }
            }
        });
    }

    private void updateDateLabel() {
        LocalDate danas = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        labelDatum.setText("Aktivne promocije za " + danas.format(formatter) + ":");
    }

    @FXML
    public void ucitajProizvodeNaPromociji() {
        listaProizvoda.clear();
        
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL GetProizvodiNaPromociji()}")) { //poziv procedure
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ProizvodNaPromociji proizvod = new ProizvodNaPromociji(
                    rs.getInt("proizvod_id"),
                    rs.getString("naziv_proizvoda"),
                    rs.getDouble("originalna_cijena"),
                    rs.getDouble("zapremina"),
                    rs.getString("naziv_promocije"),
                    rs.getString("opis_promocije"),
                    rs.getDouble("vrijednost_popusta"),
                    rs.getDate("datum_pocetka").toLocalDate(),
                    rs.getDate("datum_zavrsetka").toLocalDate(),
                    rs.getDouble("cijena_sa_popustom"),
                    rs.getDouble("ustedjevina")
                );
                listaProizvoda.add(proizvod);
            }
            
            tabelaPromocija.setItems(listaProizvoda);
            updateBrojProizvodaLabel();
            
        } catch (SQLException e) {
            e.printStackTrace();
            prikaziUpozorenje("Greška pri učitavanju proizvoda na promociji: " + e.getMessage());
        }
    }

    private void updateBrojProizvodaLabel() {
        int brojProizvoda = listaProizvoda.size();
        labelBrojProizvoda.setText("Ukupno proizvoda na promociji: " + brojProizvoda);
    }

    @FXML
    public void nazad() {
        Stage stage = (Stage) tabelaPromocija.getScene().getWindow();
        stage.close();
    }

    private void prikaziUpozorenje(String poruka) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Upozorenje");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    // Inner class za model podataka
    public static class ProizvodNaPromociji {
        private int proizvodId;
        private String nazivProizvoda;
        private double originalnaCijena;
        private double zapremina;
        private String nazivPromocije;
        private String opisPromocije;
        private double vrijednostPopusta;
        private LocalDate datumPocetka;
        private LocalDate datumZavrsetka;
        private double cijenaSaPopustom;
        private double ustedjevina;

        public ProizvodNaPromociji(int proizvodId, String nazivProizvoda, double originalnaCijena,
                                 double zapremina, String nazivPromocije, String opisPromocije,
                                 double vrijednostPopusta, LocalDate datumPocetka, LocalDate datumZavrsetka,
                                 double cijenaSaPopustom, double ustedjevina) {
            this.proizvodId = proizvodId;
            this.nazivProizvoda = nazivProizvoda;
            this.originalnaCijena = originalnaCijena;
            this.zapremina = zapremina;
            this.nazivPromocije = nazivPromocije;
            this.opisPromocije = opisPromocije;
            this.vrijednostPopusta = vrijednostPopusta;
            this.datumPocetka = datumPocetka;
            this.datumZavrsetka = datumZavrsetka;
            this.cijenaSaPopustom = cijenaSaPopustom;
            this.ustedjevina = ustedjevina;
        }
        public int getProizvodId() { return proizvodId; }
        public String getNazivProizvoda() { return nazivProizvoda; }
        public double getOriginalnaCijena() { return originalnaCijena; }
        public double getZapremina() { return zapremina; }
        public String getNazivPromocije() { return nazivPromocije; }
        public String getOpisPromocije() { return opisPromocije; }
        public double getVrijednostPopusta() { return vrijednostPopusta; }
        public LocalDate getDatumPocetka() { return datumPocetka; }
        public LocalDate getDatumZavrsetka() { return datumZavrsetka; }
        public double getCijenaSaPopustom() { return cijenaSaPopustom; }
        public double getUstedjevina() { return ustedjevina; }

        public void setProizvodId(int proizvodId) { this.proizvodId = proizvodId; }
        public void setNazivProizvoda(String nazivProizvoda) { this.nazivProizvoda = nazivProizvoda; }
        public void setOriginalnaCijena(double originalnaCijena) { this.originalnaCijena = originalnaCijena; }
        public void setZapremina(double zapremina) { this.zapremina = zapremina; }
        public void setNazivPromocije(String nazivPromocije) { this.nazivPromocije = nazivPromocije; }
        public void setOpisPromocije(String opisPromocije) { this.opisPromocije = opisPromocije; }
        public void setVrijednostPopusta(double vrijednostPopusta) { this.vrijednostPopusta = vrijednostPopusta; }
        public void setDatumPocetka(LocalDate datumPocetka) { this.datumPocetka = datumPocetka; }
        public void setDatumZavrsetka(LocalDate datumZavrsetka) { this.datumZavrsetka = datumZavrsetka; }
        public void setCijenaSaPopustom(double cijenaSaPopustom) { this.cijenaSaPopustom = cijenaSaPopustom; }
        public void setUstedjevina(double ustedjevina) { this.ustedjevina = ustedjevina; }
    }
}