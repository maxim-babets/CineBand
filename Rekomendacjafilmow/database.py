import mysql.connector  # Biblioteka do połączenia z MySQL

def fetch_data_from_db(conn):
    """
    Funkcja pobierająca dane z tabel 'Filmy', 'Uzytkownicy' i 'Oceny' z bazy danych.
    """
    cursor = conn.cursor()

    cursor.execute("SELECT * FROM filmy")
    films = cursor.fetchall()

    cursor.execute("SELECT * FROM uzytkownicy")
    users = cursor.fetchall()

    cursor.execute("SELECT * FROM oceny")
    ratings = cursor.fetchall()

    return films, users, ratings