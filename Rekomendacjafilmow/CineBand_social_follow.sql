-- Follow graph (Instagram-style): who follows whom
USE system_rekomendacji;

CREATE TABLE IF NOT EXISTS Obserwuje (
    id_obserwacji INT PRIMARY KEY AUTO_INCREMENT,
    id_obserwujacy INT NOT NULL COMMENT 'follower',
    id_obserwowany INT NOT NULL COMMENT 'followed user',
    utworzono DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_follow_pair (id_obserwujacy, id_obserwowany),
    FOREIGN KEY (id_obserwujacy) REFERENCES Uzytkownicy(id_uzytkownika),
    FOREIGN KEY (id_obserwowany) REFERENCES Uzytkownicy(id_uzytkownika)
);
