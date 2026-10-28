INSERT INTO brend (brend_id, naziv, opis) VALUES
(1, 'Dior', 'Luksuzni francuski modni i parfemski brend'),
(2, 'Chanel', 'Ikonični francuski brend poznat po klasičnoj eleganciji'),
(3, 'Yves Saint Laurent', 'Prestižan brend prepoznatljiv po unikatnim mirisima'),
(4, 'Tom Ford', 'Luksuzna američka modna kuća i parfemski proizvođač'),
(5, 'Hugo Boss', 'Poznat po elegantnim i modernim muškim parfemima'),
(6, 'Guerlain', 'Jedan od najstarijih parfemskih brendova, poznat po rafiniranim i kompleksnim mirisima');

INSERT INTO dobavljac (dobavljac_id, naziv, kontakt, adresa) VALUES
(1, 'Aroma DOO', '065/065-065', 'Titova 12, Sarajevo'),
(2, 'ParfumEX', '065/111-111', 'Bulevar bb, Banja Luka');

INSERT INTO filijala (filijala_id, naziv, adresa, telefon) VALUES
(1, 'Aroma', 'Kralja Petra I 12, Beograd', '066/666-666'),
(2, 'LeMirage', 'Bulevar Oslobođenja 102, Beograd', '066/123-123'),
(3, 'Bellezza', 'Njegoševa 35, Podgorica', '066/123-111'),
(4, 'Essenza', 'Trg bana Jelačića 5, Zagreb', '066/123/222'),
(5, 'Luxe', 'Ulica Kralja Tomislava 48, Split', '066/123-333'),
(6, 'Secret', 'Glavna 1, Niš', '066/123-444');

INSERT INTO kategorija (kategorija_id, naziv) VALUES
(1, 'Muški parfem'),
(2, 'Ženski parfem'),
(3, 'Unisex parfem'),
(4, 'Poklon pakovanje');

INSERT INTO korisnik (korisnik_id, ime, prezime, korisnicko_ime, lozinka) VALUES
(1, 'Marija', 'Tadic', 'marija', 'marija'),
(2, 'Ana', 'Tadic', 'ana', 'ana'),
(4, 'Petar', 'Petrovic', 'petar', 'petar'),
(5, 'Milana', 'Milanovic', 'milana', 'milana'),
(6, 'Slavisa', 'Savić', 'slavisa', 'slavisa');

INSERT INTO prodavac (filijala_id, prodavac_id) VALUES 
(1, 1),
(2, 2),
(2, 5),
(3, 6);

INSERT INTO nabavljac (nabavljac_id, filijala_id) VALUES 
(4, 1);

INSERT INTO lojalti_kartica (kartica_id, broj_bodova, ime, prezime, kontakt) VALUES
(1, 37, 'Ivan', 'Ivanović', '066/111-111'),
(2, 18, 'Mila', 'Milić', '066/222-222'),
(3, 500, 'Janko', 'Janković', '066/333-333'),
(4, 400, 'Milica', 'Milić', '066/444-444'),
(5, 0, 'Jelena', 'Jelić', '066/555-555'),
(6, 550, 'Miloš', 'Milošević', '066/666-666'),
(7, 215, 'Danica', 'Danić', '066/123-127'),
(8, 0, 'Anastasija', 'Anić', '066/456-456'),
(9, 500, 'Miroslav', 'Mirić', '066/789-789'),
(10, 140, 'Goran', 'Garić', '066-159-159');

INSERT INTO nacin_placanja (nacin_placanja_id, naziv) VALUES
(1, 'Gotovina'),
(2, 'Kartica');

INSERT INTO nabavka (nabavka_id, datum, dobavljac_id, filijala_id, nabavljac_id) VALUES
(2, '2024-05-28', 2, 2, 4);

INSERT INTO proizvod (proizvod_id, naziv, brend_id, kategorija_id, cijena, zapremina) VALUES
(1, 'Sauvage EDT', 1, 1, 180, 100),
(2, 'Chanel No.5 EDP', 2, 2, 220, 50),
(3, 'Black Opium', 3, 2, 210, 90),
(4, 'Tom Ford Oud Wood', 4, 3, 300, 50),
(5, 'Boss Bottled', 5, 1, 150, 100),
(6, 'Addict', 1, 2, 200, 50),
(7, 'Lost Cherry', 4, 3, 250, 50),
(8, 'Coco Mademoiselle', 2, 2, 160, 90);

INSERT INTO promocija (promocija_id, naziv, opis, datum_pocetka, datum_zavrsetka, vrijednost_popusta) VALUES
(1, 'Ljetna rasprodaja', 'Veliki popusti na sve proizvode!', '2025-06-01', '2025-06-30', 15),
(2, 'Vikend akcija', 'Posebni popusti vikendom', '2025-06-14', '2025-06-16', 10),
(3, 'Clearance sale!', 'Rasprodaja starih zaliha', '2025-06-10', '2025-06-25', 25),
(4, 'Sredina mjeseca', 'Akcija sredinom mjeseca', '2025-06-12', '2025-06-18', 20),
(5, 'Jesen stiže', 'Priprema za jesenju sezonu', '2025-07-01', '2025-07-15', 12),
(6, 'Proljetna akcija', 'Dobrodoslo proljece!', '2025-03-01', '2025-03-31', 18);

INSERT INTO razlog_reklamacije (razlog_id, naziv) VALUES
(1, 'Oštećen proizvod'),
(2, 'Pogrešan artikl'),
(3, 'Drugo');

INSERT INTO reklamacija (reklamacija_id, datum, proizvod_id, razlog_id, stavka_prodaje_id, opis) VALUES
(12, '2025-06-16', 1, 1, 49, NULL),
(13, '2025-06-16', 2, 3, 50, 'Razlog'),
(14, '2025-06-18', 4, 1, 52, NULL),
(15, '2025-06-18', 2, 1, 54, NULL);

INSERT INTO stavka_nabavke (nabavka_id, proizvod_id, kolicina, cijena) VALUES
(2, 2, 10, 2200.00);

INSERT INTO stavka_prodaje (prodaja_id, proizvod_id, kolicina, cijena, popust) VALUES
(49, 1, 1, 180.00, 27.00),
(50, 2, 2, 220.00, 66.00),
(51, 1, 1, 180.00, 27.00),
(52, 4, 1, 300.00, 0.00),
(53, 2, 2, 220.00, 66.00),
(54, 2, 1, 220.00, 33.00);

INSERT INTO prodaja (prodaja_id, datum, prodavac_id, filijala_id, nacin_placanja_id, kartica_id, popust_od_bodova) VALUES
(49, '2025-06-16', 1, 1, 1, 1, 0.00),
(50, '2025-06-16', 1, 1, 2, 2, 12.00),
(51, '2025-06-16', 1, 1, 1, NULL, 0.00),
(52, '2025-06-18', 2, 2, 2, 1, 9.60),
(53, '2025-06-18', 2, 2, 1, 1, 0.00),
(54, '2025-06-18', 2, 2, 2, 2, 0.00);

INSERT INTO zaliha (proizvod_id, filijala_id, kolicina) VALUES
(1, 1, 2),
(1, 3, 12),
(2, 1, 3),
(2, 2, 7),
(3, 1, 3),
(4, 2, 11),
(5, 3, 10),
(8, 3, 5);