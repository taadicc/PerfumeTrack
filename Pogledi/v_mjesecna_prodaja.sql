CREATE VIEW v_mjesecna_prodaja AS
SELECT 
    YEAR(p.datum) AS godina,
    MONTH(p.datum) AS mjesec,
    f.naziv AS filijala,
    COUNT(DISTINCT p.prodaja_id) AS broj_racuna,
    SUM(sp.kolicina * sp.cijena) AS ukupna_bruto_prodaja,
    SUM(sp.popust) AS ukupan_popust,
    SUM(sp.kolicina * sp.cijena - sp.popust) AS ukupna_neto_prodaja,
    AVG(sp.kolicina * sp.cijena - sp.popust) AS prosjecna_vrijednost_racuna,
    COUNT(DISTINCT CASE WHEN p.kartica_id IS NOT NULL THEN p.kartica_id END) AS broj_kartica_koristenja
FROM prodaja p
JOIN filijala f ON p.filijala_id = f.filijala_id
JOIN stavka_prodaje sp ON p.prodaja_id = sp.prodaja_id
GROUP BY YEAR(p.datum), MONTH(p.datum), f.filijala_id, f.naziv
ORDER BY godina DESC, mjesec DESC;