-- CineBand: auth columns + social wall + pick history (run against system_rekomendacji)
USE system_rekomendacji;

ALTER TABLE Uzytkownicy
  ADD COLUMN haslo_hash VARCHAR(255) NULL AFTER email,
  ADD COLUMN nick VARCHAR(64) NULL,
  ADD UNIQUE KEY uq_uzytkownicy_nick (nick);

CREATE TABLE IF NOT EXISTS WpisyWall (
  id_wpisu INT PRIMARY KEY AUTO_INCREMENT,
  id_uzytkownika INT NOT NULL,
  tresc VARCHAR(2000) NOT NULL,
  utworzono DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_uzytkownika) REFERENCES Uzytkownicy(id_uzytkownika)
);

CREATE TABLE IF NOT EXISTS ReakcjeWpisy (
  id_reakcji INT PRIMARY KEY AUTO_INCREMENT,
  id_wpisu INT NOT NULL,
  id_uzytkownika INT NOT NULL,
  typ ENUM('LIKE', 'DISLIKE') NOT NULL,
  UNIQUE KEY uq_post_user (id_wpisu, id_uzytkownika),
  FOREIGN KEY (id_wpisu) REFERENCES WpisyWall(id_wpisu) ON DELETE CASCADE,
  FOREIGN KEY (id_uzytkownika) REFERENCES Uzytkownicy(id_uzytkownika)
);

CREATE TABLE IF NOT EXISTS HistoriaWyborow (
  id_wyboru INT PRIMARY KEY AUTO_INCREMENT,
  id_uzytkownika INT NOT NULL,
  id_filmu INT NOT NULL,
  moment DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_uzytkownika) REFERENCES Uzytkownicy(id_uzytkownika),
  FOREIGN KEY (id_filmu) REFERENCES Filmy(id_filmu)
);
