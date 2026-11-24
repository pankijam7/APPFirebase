package com.devst.appfirebase.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.devst.appfirebase.model.Videojuego;
import com.devst.appfirebase.repositorio.VideojuegoRepositorio;

import java.util.List;

// Puente entre la interfaz gráfica (MainActivity) y los datos en Firebase (VideojuegoRepositorio)
public class MainViewModel extends ViewModel {

    private final VideojuegoRepositorio repositorio = new VideojuegoRepositorio();

    // LiveData permite que la pantalla escuche cambios y se actualice automáticamente
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Videojuego> juegoEncontrado = new MutableLiveData<>();
    private final MutableLiveData<List<Videojuego>> listaJuegos = new MutableLiveData<>();

    public LiveData<String> getMensaje() {
        return mensaje;
    }

    public LiveData<Videojuego> getJuegoEncontrado() {
        return juegoEncontrado;
    }

    public LiveData<List<Videojuego>> getListaJuegos() {
        return listaJuegos;
    }

    // Crea el objeto con el UID del usuario y al guardar recarga la lista automáticamente
    public void guardarJuego(String id, String titulo, String plataforma, String genero, String uid) {
        Videojuego nuevo = new Videojuego(id, titulo, plataforma, genero, uid);
        repositorio.guardar(nuevo, new VideojuegoRepositorio.OnOperationCallback() {
            @Override
            public void onSuccess(String msg) {
                mensaje.setValue(msg);
                cargarMisJuegos(uid);
            }

            @Override
            public void onError(String err) {
                mensaje.setValue(err);
            }
        });
    }

    // Solicita un registro por ID y actualiza juegoEncontrado para rellenar el formulario
    public void buscarJuego(String id, String uid) {
        repositorio.buscar(id, uid, new VideojuegoRepositorio.OnBuscarCallback() {
            @Override
            public void onFound(Videojuego videojuego) {
                juegoEncontrado.setValue(videojuego);
                mensaje.setValue("Registro encontrado");
            }

            @Override
            public void onError(String err) {
                mensaje.setValue(err);
            }
        });
    }

    // Envía los datos editados a Firestore y refresca la lista en pantalla
    public void actualizarJuego(String id, String titulo, String plataforma, String genero, String uid) {
        Videojuego actualizado = new Videojuego(id, titulo, plataforma, genero, uid);
        repositorio.actualizar(actualizado, new VideojuegoRepositorio.OnOperationCallback() {
            @Override
            public void onSuccess(String msg) {
                mensaje.setValue(msg);
                cargarMisJuegos(uid);
            }

            @Override
            public void onError(String err) {
                mensaje.setValue(err);
            }
        });
    }

    // Borra el registro en Firestore y actualiza la lista visible
    public void eliminarJuego(String id, String uid) {
        repositorio.eliminar(id, new VideojuegoRepositorio.OnOperationCallback() {
            @Override
            public void onSuccess(String msg) {
                mensaje.setValue(msg);
                cargarMisJuegos(uid);
            }

            @Override
            public void onError(String err) {
                mensaje.setValue(err);
            }
        });
    }

    // Obtiene todos los registros filtrados por el UID del usuario autenticado
    public void cargarMisJuegos(String uid) {
        repositorio.listarPorUsuario(uid, new VideojuegoRepositorio.OnListarCallback() {
            @Override
            public void onLoaded(List<Videojuego> lista) {
                listaJuegos.setValue(lista);
            }

            @Override
            public void onError(String err) {
                mensaje.setValue(err);
            }
        });
    }
}