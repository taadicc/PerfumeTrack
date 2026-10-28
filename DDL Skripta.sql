-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema perfume_track
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema perfume_track
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `perfume_track` DEFAULT CHARACTER SET utf8 ;
USE `perfume_track` ;

-- -----------------------------------------------------
-- Table `perfume_track`.`PROMOCIJA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`PROMOCIJA` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`PROMOCIJA` (
  `promocija_id` INT NOT NULL AUTO_INCREMENT,
  `naziv` VARCHAR(100) NOT NULL,
  `opis` VARCHAR(255) NULL,
  `datum_pocetka` DATE NOT NULL,
  `datum_zavrsetka` DATE NOT NULL,
  `vrijednost_popusta` DOUBLE NOT NULL,
  PRIMARY KEY (`promocija_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`BREND`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`BREND` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`BREND` (
  `brend_id` INT NOT NULL AUTO_INCREMENT,
  `naziv` VARCHAR(100) NOT NULL,
  `opis` VARCHAR(255) NULL,
  PRIMARY KEY (`brend_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`KATEGORIJA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`KATEGORIJA` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`KATEGORIJA` (
  `kategorija_id` INT NOT NULL AUTO_INCREMENT,
  `naziv` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`kategorija_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`PROIZVOD`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`PROIZVOD` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`PROIZVOD` (
  `proizvod_id` INT NOT NULL AUTO_INCREMENT,
  `naziv` VARCHAR(100) NOT NULL,
  `brend_id` INT NOT NULL,
  `kategorija_id` INT NOT NULL,
  `cijena` DOUBLE NOT NULL,
  `zapremina` INT NOT NULL,
  PRIMARY KEY (`proizvod_id`),
  INDEX `fk_proizvod_brend1_idx` (`brend_id` ASC) VISIBLE,
  INDEX `fk_proizvod_kategorija1_idx` (`kategorija_id` ASC) VISIBLE,
  CONSTRAINT `fk_proizvod_brend1`
    FOREIGN KEY (`brend_id`)
    REFERENCES `perfume_track`.`BREND` (`brend_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proizvod_kategorija1`
    FOREIGN KEY (`kategorija_id`)
    REFERENCES `perfume_track`.`KATEGORIJA` (`kategorija_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`PROMOCIJA_PROIZVOD`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`PROMOCIJA_PROIZVOD` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`PROMOCIJA_PROIZVOD` (
  `promocija_id` INT NOT NULL,
  `proizvod_id` INT NOT NULL,
  PRIMARY KEY (`promocija_id`, `proizvod_id`),
  INDEX `fk_promocija_proizvod_Promocija_idx` (`promocija_id` ASC) VISIBLE,
  INDEX `fk_promocija_proizvod_proizvod1_idx` (`proizvod_id` ASC) VISIBLE,
  CONSTRAINT `fk_promocija_proizvod_Promocija`
    FOREIGN KEY (`promocija_id`)
    REFERENCES `perfume_track`.`PROMOCIJA` (`promocija_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_promocija_proizvod_proizvod1`
    FOREIGN KEY (`proizvod_id`)
    REFERENCES `perfume_track`.`PROIZVOD` (`proizvod_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`FILIJALA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`FILIJALA` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`FILIJALA` (
  `filijala_id` INT NOT NULL AUTO_INCREMENT,
  `naziv` VARCHAR(100) NOT NULL,
  `adresa` VARCHAR(255) NOT NULL,
  `telefon` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`filijala_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`ZALIHA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`ZALIHA` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`ZALIHA` (
  `proizvod_id` INT NOT NULL,
  `filijala_id` INT NOT NULL,
  `kolicina` INT NOT NULL,
  INDEX `fk_zaliha_proizvod1_idx` (`proizvod_id` ASC) VISIBLE,
  INDEX `fk_zaliha_filijala1_idx` (`filijala_id` ASC) VISIBLE,
  PRIMARY KEY (`proizvod_id`, `filijala_id`),
  CONSTRAINT `fk_zaliha_proizvod1`
    FOREIGN KEY (`proizvod_id`)
    REFERENCES `perfume_track`.`PROIZVOD` (`proizvod_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_zaliha_filijala1`
    FOREIGN KEY (`filijala_id`)
    REFERENCES `perfume_track`.`FILIJALA` (`filijala_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`KORISNIK`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`KORISNIK` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`KORISNIK` (
  `korisnik_id` INT NOT NULL AUTO_INCREMENT,
  `ime` VARCHAR(50) NOT NULL,
  `prezime` VARCHAR(50) NOT NULL,
  `korisnicko_ime` VARCHAR(50) NOT NULL,
  `lozinka` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`korisnik_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`LOJALTI_KARTICA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`LOJALTI_KARTICA` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`LOJALTI_KARTICA` (
  `kartica_id` INT NOT NULL AUTO_INCREMENT,
  `broj_bodova` INT NOT NULL,
  `ime` VARCHAR(45) NOT NULL,
  `prezime` VARCHAR(45) NOT NULL,
  `kontakt` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`kartica_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`NACIN_PLACANJA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`NACIN_PLACANJA` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`NACIN_PLACANJA` (
  `nacin_placanja_id` INT NOT NULL AUTO_INCREMENT,
  `naziv` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`nacin_placanja_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`PRODAVAC`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`PRODAVAC` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`PRODAVAC` (
  `filijala_id` INT NOT NULL,
  `prodavac_id` INT NOT NULL,
  PRIMARY KEY (`prodavac_id`),
  INDEX `fk_PRODAVAC_KORISNIK1_idx` (`prodavac_id` ASC) VISIBLE,
  INDEX `fk_PRODAVAC_FILIJALA1_idx` (`filijala_id` ASC) VISIBLE,
  CONSTRAINT `fk_PRODAVAC_KORISNIK1`
    FOREIGN KEY (`prodavac_id`)
    REFERENCES `perfume_track`.`KORISNIK` (`korisnik_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PRODAVAC_FILIJALA1`
    FOREIGN KEY (`filijala_id`)
    REFERENCES `perfume_track`.`FILIJALA` (`filijala_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`PRODAJA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`PRODAJA` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`PRODAJA` (
  `prodaja_id` INT NOT NULL AUTO_INCREMENT,
  `datum` DATE NOT NULL,
  `prodavac_id` INT NOT NULL,
  `filijala_id` INT NOT NULL,
  `nacin_placanja_id` INT NOT NULL,
  `kartica_id` INT NULL DEFAULT NULL,
  `popust_od_bodova` DECIMAL(10,2) NULL DEFAULT "0.00",
  PRIMARY KEY (`prodaja_id`),
  INDEX `fk_prodaja_filijala1_idx` (`filijala_id` ASC) VISIBLE,
  INDEX `fk_prodaja_nacin_placanja1_idx` (`nacin_placanja_id` ASC) VISIBLE,
  INDEX `fk_PRODAJA_PRODAVAC1_idx` (`prodavac_id` ASC) VISIBLE,
  INDEX `fk_PRODAJA_LOJALTI_KARTICA1_idx` (`kartica_id` ASC) VISIBLE,
  CONSTRAINT `fk_prodaja_filijala1`
    FOREIGN KEY (`filijala_id`)
    REFERENCES `perfume_track`.`FILIJALA` (`filijala_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_prodaja_nacin_placanja1`
    FOREIGN KEY (`nacin_placanja_id`)
    REFERENCES `perfume_track`.`NACIN_PLACANJA` (`nacin_placanja_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PRODAJA_PRODAVAC1`
    FOREIGN KEY (`prodavac_id`)
    REFERENCES `perfume_track`.`PRODAVAC` (`prodavac_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PRODAJA_LOJALTI_KARTICA1`
    FOREIGN KEY (`kartica_id`)
    REFERENCES `perfume_track`.`LOJALTI_KARTICA` (`kartica_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`STAVKA_PRODAJE`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`STAVKA_PRODAJE` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`STAVKA_PRODAJE` (
  `prodaja_id` INT NOT NULL,
  `proizvod_id` INT NOT NULL,
  `kolicina` VARCHAR(45) NULL,
  `cijena` DECIMAL(10,2) NOT NULL,
  `popust` DECIMAL(10,2) NULL,
  PRIMARY KEY (`prodaja_id`, `proizvod_id`),
  INDEX `fk_stavka_prodaje_prodaja1_idx` (`prodaja_id` ASC) VISIBLE,
  INDEX `fk_stavka_prodaje_proizvod1_idx` (`proizvod_id` ASC) VISIBLE,
  CONSTRAINT `fk_stavka_prodaje_prodaja1`
    FOREIGN KEY (`prodaja_id`)
    REFERENCES `perfume_track`.`PRODAJA` (`prodaja_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_stavka_prodaje_proizvod1`
    FOREIGN KEY (`proizvod_id`)
    REFERENCES `perfume_track`.`PROIZVOD` (`proizvod_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`RAZLOG_REKLAMACIJE`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`RAZLOG_REKLAMACIJE` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`RAZLOG_REKLAMACIJE` (
  `razlog_id` INT NOT NULL AUTO_INCREMENT,
  `naziv` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`razlog_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`REKLAMACIJA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`REKLAMACIJA` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`REKLAMACIJA` (
  `reklamacija_id` INT NOT NULL AUTO_INCREMENT,
  `datum` DATE NOT NULL,
  `proizvod_id` INT NOT NULL,
  `razlog_id` INT NOT NULL,
  `stavka_prodaje_id` INT NOT NULL,
  `opis` VARCHAR(255) NULL,
  PRIMARY KEY (`reklamacija_id`),
  INDEX `fk_reklamacija_razlog_reklamacije1_idx` (`razlog_id` ASC) VISIBLE,
  INDEX `fk_REKLAMACIJA_STAVKA_PRODAJE1_idx` (`stavka_prodaje_id` ASC) VISIBLE,
  CONSTRAINT `fk_reklamacija_razlog_reklamacije1`
    FOREIGN KEY (`razlog_id`)
    REFERENCES `perfume_track`.`RAZLOG_REKLAMACIJE` (`razlog_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_REKLAMACIJA_STAVKA_PRODAJE1`
    FOREIGN KEY (`stavka_prodaje_id`)
    REFERENCES `perfume_track`.`STAVKA_PRODAJE` (`prodaja_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`DOBAVLJAC`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`DOBAVLJAC` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`DOBAVLJAC` (
  `dobavljac_id` INT NOT NULL AUTO_INCREMENT,
  `naziv` VARCHAR(45) NOT NULL,
  `kontakt` VARCHAR(45) NOT NULL,
  `adresa` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`dobavljac_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`NABAVLJAC`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`NABAVLJAC` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`NABAVLJAC` (
  `nabavljac_id` INT NOT NULL,
  `filijala_id` INT NOT NULL,
  PRIMARY KEY (`nabavljac_id`),
  INDEX `fk_NABAVLJAC_KORISNIK1_idx` (`nabavljac_id` ASC) VISIBLE,
  INDEX `fk_NABAVLJAC_FILIJALA1_idx` (`filijala_id` ASC) VISIBLE,
  CONSTRAINT `fk_NABAVLJAC_KORISNIK1`
    FOREIGN KEY (`nabavljac_id`)
    REFERENCES `perfume_track`.`KORISNIK` (`korisnik_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_NABAVLJAC_FILIJALA1`
    FOREIGN KEY (`filijala_id`)
    REFERENCES `perfume_track`.`FILIJALA` (`filijala_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`NABAVKA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`NABAVKA` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`NABAVKA` (
  `nabavka_id` INT NOT NULL AUTO_INCREMENT,
  `datum` DATE NOT NULL,
  `dobavljac_id` INT NOT NULL,
  `filijala_id` INT NOT NULL,
  `nabavljac_id` INT NOT NULL,
  PRIMARY KEY (`nabavka_id`),
  INDEX `fk_NABAVKA_DOBAVLJAC1_idx` (`dobavljac_id` ASC) VISIBLE,
  INDEX `fk_NABAVKA_FILIJALA1_idx` (`filijala_id` ASC) VISIBLE,
  INDEX `fk_NABAVKA_NABAVLJAC1_idx` (`nabavljac_id` ASC) VISIBLE,
  CONSTRAINT `fk_NABAVKA_DOBAVLJAC1`
    FOREIGN KEY (`dobavljac_id`)
    REFERENCES `perfume_track`.`DOBAVLJAC` (`dobavljac_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_NABAVKA_FILIJALA1`
    FOREIGN KEY (`filijala_id`)
    REFERENCES `perfume_track`.`FILIJALA` (`filijala_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_NABAVKA_NABAVLJAC1`
    FOREIGN KEY (`nabavljac_id`)
    REFERENCES `perfume_track`.`NABAVLJAC` (`nabavljac_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `perfume_track`.`STAVKA_NABAVKE`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `perfume_track`.`STAVKA_NABAVKE` ;

CREATE TABLE IF NOT EXISTS `perfume_track`.`STAVKA_NABAVKE` (
  `nabavka_id` INT NOT NULL AUTO_INCREMENT,
  `proizvod_id` INT NOT NULL,
  `kolicina` INT NOT NULL,
  `cijena` DECIMAL(10,2) NULL,
  INDEX `fk_STAVKA_NABAVKE_NABAVKA1_idx` (`nabavka_id` ASC) VISIBLE,
  PRIMARY KEY (`nabavka_id`, `proizvod_id`),
  INDEX `fk_STAVKA_NABAVKE_PROIZVOD1_idx` (`proizvod_id` ASC) VISIBLE,
  CONSTRAINT `fk_STAVKA_NABAVKE_NABAVKA1`
    FOREIGN KEY (`nabavka_id`)
    REFERENCES `perfume_track`.`NABAVKA` (`nabavka_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_STAVKA_NABAVKE_PROIZVOD1`
    FOREIGN KEY (`proizvod_id`)
    REFERENCES `perfume_track`.`PROIZVOD` (`proizvod_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
