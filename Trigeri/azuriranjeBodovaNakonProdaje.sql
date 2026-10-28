CREATE DEFINER=`root`@`localhost` TRIGGER `azuriraj_bodove_nakon_prodaje` AFTER INSERT ON `prodaja` FOR EACH ROW BEGIN
    -- postoji kartica vezana za prodaju i koristen je popust od bod
    IF NEW.kartica_id IS NOT NULL AND NEW.popust_od_bodova > 0 THEN
        UPDATE lojalti_kartica 
        SET broj_bodova = broj_bodova - (NEW.popust_od_bodova / 0.10) -- 1 bod = 0.10 KM
        WHERE kartica_id = NEW.kartica_id;
    END IF;
END