package gui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import application.DBConnection;

public class UserDAO {
	public static class ProdavacInfo {
		public int prodavacId;
		public int filijalaId;
		public String imePrezime;
		public String korisnickoIme;

		public ProdavacInfo(int prodavacId, int filijalaId, String imePrezime, String korisnickoIme) {
			this.prodavacId = prodavacId;
			this.filijalaId = filijalaId;
			this.imePrezime = imePrezime;
			this.korisnickoIme = korisnickoIme;
		}
	}

	public static ProdavacInfo prijavi(String korisnickoIme, String lozinka) {
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement stmt = conn
						.prepareStatement("SELECT p.prodavac_id, p.filijala_id, k.ime, k.prezime, k.korisnicko_ime "
								+ "FROM prodavac p " + "JOIN korisnik k ON p.prodavac_id = k.korisnik_id "
								+ "WHERE k.korisnicko_ime = ? AND k.lozinka = ?")) {

			stmt.setString(1, korisnickoIme);
			stmt.setString(2, lozinka);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return new ProdavacInfo(rs.getInt("prodavac_id"), rs.getInt("filijala_id"),
						rs.getString("ime") + " " + rs.getString("prezime"), rs.getString("korisnicko_ime"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean proveriProdavca(String korisnickoIme, String lozinka) {
		return prijavi(korisnickoIme, lozinka) != null;
	}
}