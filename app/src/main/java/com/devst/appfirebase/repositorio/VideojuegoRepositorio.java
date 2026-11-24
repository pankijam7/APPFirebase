package com.devst.appfirebase.repositorio;

import com.devst.appfirebase.model.Videojuego;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Clase encargada de toda la comunicación directa (CRUD) con Cloud Firestore
public class VideojuegoRepositorio {

    // Nombre de la colección en Firestore (se crea automáticamente al guardar el primer registro)
    private static final String COLLECTION_NAME = "videojuegos";
    // Instancia de conexión a la base de datos de Firebase
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Interfaces para avisar al ViewModel cuando Firebase responde (operaciones asíncronas)
    public interface OnOperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface OnBuscarCallback {
        void onFound(Videojuego videojuego);
        void onError(String error);
    }

    public interface OnListarCallback {
        void onLoaded(List<Videojuego> lista);
        void onError(String error);
    }

    // CREATE: Guarda un nuevo documento usando el ID del juego como identificador
    public void guardar(Videojuego juego, OnOperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(juego.getId())
                .set(juego)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Videojuego guardado correctamente"))
                .addOnFailureListener(e -> callback.onError("Error al guardar: " + e.getMessage()));
    }

    // READ: Busca un videojuego por su ID y verifica que pertenezca al usuario conectado
    public void buscar(String id, String uidActual, OnBuscarCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Videojuego juego = documentSnapshot.toObject(Videojuego.class);
                        // Validación de seguridad: solo devuelve el juego si el UID coincide con el dueño
                        if (juego != null && uidActual.equals(juego.getUidUsuario())) {
                            callback.onFound(juego);
                        } else {
                            callback.onError("Este registro pertenece a otro usuario");
                        }
                    } else {
                        callback.onError("No existe un videojuego con el ID: " + id);
                    }
                })
                .addOnFailureListener(e -> callback.onError("Error al buscar: " + e.getMessage()));
    }

    // UPDATE: Primero comprueba que el documento exista y luego sobrescribe sus datos
    public void actualizar(Videojuego juego, OnOperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(juego.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        db.collection(COLLECTION_NAME)
                                .document(juego.getId())
                                .set(juego)
                                .addOnSuccessListener(aVoid -> callback.onSuccess("Registro actualizado con éxito"))
                                .addOnFailureListener(e -> callback.onError("Error al actualizar: " + e.getMessage()));
                    } else {
                        callback.onError("El ID no existe. Usa Guardar primero.");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Error al verificar ID: " + e.getMessage()));
    }

    // DELETE: Elimina el documento indicado de la colección en Firestore
    public void eliminar(String id, OnOperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess("Videojuego eliminado correctamente"))
                .addOnFailureListener(e -> callback.onError("Error al eliminar: " + e.getMessage()));
    }

    // READ (Lista): Filtra la colección para traer únicamente los videojuegos del usuario actual
    public void listarPorUsuario(String uidUsuario, OnListarCallback callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("uidUsuario", uidUsuario)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Videojuego> lista = new ArrayList<>();
                    // Recorre cada documento encontrado y lo convierte en un objeto Videojuego
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Videojuego v = doc.toObject(Videojuego.class);
                        lista.add(v);
                    }
                    callback.onLoaded(lista);
                })
                .addOnFailureListener(e -> callback.onError("Error al cargar lista: " + e.getMessage()));
    }
}