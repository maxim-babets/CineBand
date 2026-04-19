from sklearn.neighbors import NearestNeighbors  # Algorytm KNN

def create_knn_model(ratings_matrix):
    """
    Tworzy i dopasowuje model KNN na podstawie macierzy ocen.
    """
    knn_model = NearestNeighbors(metric='cosine', algorithm='brute')
    knn_model.fit(ratings_matrix)
    return knn_model