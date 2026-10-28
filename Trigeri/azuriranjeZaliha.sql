CREATE DEFINER=`root`@`localhost` TRIGGER `stavka_prodaje_AFTER_INSERT` AFTER INSERT ON `stavka_prodaje` FOR EACH ROW BEGIN
 UPDATE zaliha
    SET kolicina = kolicina - NEW.kolicina
    WHERE proizvod_id = NEW.proizvod_id
      AND filijala_id = (
          SELECT filijala_id
          FROM prodaja
          WHERE prodaja_id = NEW.prodaja_id
      );
END