-- Banners + optional unique rating per user/film for learning (run on system_rekomendacji)
USE system_rekomendacji;

ALTER TABLE Filmy ADD COLUMN banner_url VARCHAR(768) NULL AFTER rok_wydania;

-- Stable banner art per title (picsum seeds — replace with TMDB posters later)
UPDATE Filmy SET banner_url = CONCAT('https://picsum.photos/seed/cineband', id_filmu, '/880/495') WHERE id_filmu BETWEEN 1 AND 20;

CREATE TABLE IF NOT EXISTS SygnalyRekomendacji (
    id_sygnalu INT PRIMARY KEY AUTO_INCREMENT,
    id_uzytkownika INT NOT NULL,
    id_filmu INT NOT NULL,
    sentiment VARCHAR(16) NOT NULL,
    zrodlo VARCHAR(32) NOT NULL,
    utworzono DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_uzytkownika) REFERENCES Uzytkownicy(id_uzytkownika),
    FOREIGN KEY (id_filmu) REFERENCES Filmy(id_filmu),
    INDEX idx_user_created (id_uzytkownika, utworzono)
);
