def generate_recommendations(user_id, ratings_matrix, knn_model, movies_df, ratings_df, k=5):
    """
    Funkcja generująca rekomendacje filmów dla danego użytkownika.
    """
    distances, indices = knn_model.kneighbors(ratings_matrix.loc[user_id].values.reshape(1, -1), n_neighbors=k)
    recommended_movie_ids = ratings_matrix.columns[indices.flatten()]

    print(f"\nOdległości: {distances}")  # Debugowanie: Wyświetl odległości
    print(f"Indeksy: {indices}")  # Debugowanie: Wyświetl indeksy
    print(f"Rekomendowane ID filmów: {recommended_movie_ids}")  # Debugowanie: Wyświetl rekomendowane ID filmów

    print(f"\nRekomendacje filmów dla użytkownika {user_id} (film i średnia ocena):")

    # Sprawdź, czy recommended_movie_ids nie jest puste
    if len(recommended_movie_ids) == 0:
        print("Brak rekomendacji dla tego użytkownika.")
        return

    for movie_id in recommended_movie_ids:
        movie_title = movies_df.loc[movies_df['movieId'] == movie_id]['title'].values[0]
        # Oblicz średnią ocenę dla danego filmu
        avg_rating = ratings_df.loc[ratings_df['movieId'] == movie_id]['rating'].mean()
        print(f"- {movie_title}: {avg_rating:.2f}/10")