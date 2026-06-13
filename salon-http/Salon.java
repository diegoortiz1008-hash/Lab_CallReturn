public class Salon {
    private String codigo;
    private boolean reservado;

    public Salon(String codigo) {
        this.codigo = codigo;
        this.reservado = false;
    }

    public String getCodigo() { return codigo; }
    public boolean isReservado() { return reservado; }
    public void reservar() { this.reservado = true; }
    public void liberar() { this.reservado = false; }

    public String getEstado() {
        return reservado ? "SALON_RESERVADO" : "SALON_DISPONIBLE";
    }
}