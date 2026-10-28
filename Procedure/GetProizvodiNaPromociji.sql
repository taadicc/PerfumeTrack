CREATE DEFINER=`root`@`localhost` PROCEDURE `GetProizvodiNaPromociji`()
BEGIN
    SELECT 
        p.proizvod_id,
        p.naziv AS naziv_proizvoda,
        p.cijena AS originalna_cijena,
        p.zapremina,
        pr.naziv AS naziv_promocije,
        pr.opis AS opis_promocije,
        pr.vrijednost_popusta,
        pr.datum_pocetka,
        pr.datum_zavrsetka,
        ROUND(p.cijena * (1 - pr.vrijednost_popusta / 100), 2) AS cijena_sa_popustom,
        ROUND(p.cijena * pr.vrijednost_popusta / 100, 2) AS ustedjevina
    FROM proizvod p
    INNER JOIN promocija_proizvod pp ON p.proizvod_id = pp.proizvod_id
    INNER JOIN promocija pr ON pp.promocija_id = pr.promocija_id
    WHERE CURDATE() BETWEEN pr.datum_pocetka AND pr.datum_zavrsetka
    ORDER BY pr.vrijednost_popusta DESC, p.naziv;
END