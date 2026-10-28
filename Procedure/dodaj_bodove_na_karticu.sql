CREATE DEFINER=`root`@`localhost` PROCEDURE `dodaj_bodove_na_karticu`(
    IN p_kartica_id INT,
    IN p_iznos_racuna DECIMAL(10,2),
    OUT p_novi_bodovi INT
)
BEGIN
    DECLARE bodovi_za_dodati INT;
    -- 1 bod za svakih 10 KM
    SET bodovi_za_dodati = FLOOR(p_iznos_racuna / 10);
    -- aazuriranje kartice
    IF bodovi_za_dodati > 0 THEN
        UPDATE lojalti_kartica 
        SET broj_bodova = broj_bodova + bodovi_za_dodati 
        WHERE kartica_id = p_kartica_id;
    END IF;
    -- novi broj bodova
    SELECT broj_bodova INTO p_novi_bodovi
    FROM lojalti_kartica
    WHERE kartica_id = p_kartica_id;
END