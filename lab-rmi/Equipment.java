import java.io.Serializable;

public class Equipment implements Serializable {
    private String codigo;
    private String nombre;
    private String laboratorio;
    private boolean reservado;

    public Equipment(String codigo, String nombre, String laboratorio) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.laboratorio = laboratorio;
        this.reservado = false;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getLaboratorio() { return laboratorio; }
    public boolean isReservado() { return reservado; }
    public void reservar() { this.reservado = true; }
    public void liberar() { this.reservado = false; }

    @Override
    public String toString() {
        return codigo + " | " + nombre + " | Lab: " + laboratorio
             + " | " + (reservado ? "RESERVADO" : "DISPONIBLE");
    }
}