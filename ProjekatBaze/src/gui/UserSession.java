package gui;

public class UserSession {  //cuva informacije o trenutno ulogovanom korisniku
	private static UserSession instance;
	private int prodavacId;
	private int filijalaId;
	private String imePrezime;
	private String korisnickoIme;

	private UserSession() {
	}

	public static UserSession getInstance() {  //provjerava da li je neko uloogovan
		if (instance == null) {
			instance = new UserSession();
		}
		return instance;
	}

	public void setProdavac(int prodavacId, int filijalaId, String imePrezime, String korisnickoIme) {
		this.prodavacId = prodavacId;
		this.filijalaId = filijalaId;
		this.imePrezime = imePrezime;
		this.korisnickoIme = korisnickoIme;
	}

	public int getProdavacId() {
		return prodavacId;
	}

	public int getFilijalaId() {
		return filijalaId;
	}

	public String getImePrezime() {
		return imePrezime;
	}

	public String getKorisnickoIme() {
		return korisnickoIme;
	}

	public void clearSession() { //kada se korisnik odjavljuje
		prodavacId = 0;
		filijalaId = 0;
		imePrezime = null;
		korisnickoIme = null;
	}

	public boolean isLoggedIn() {
		return prodavacId > 0;
	}
}