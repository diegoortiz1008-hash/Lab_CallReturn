import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabServiceImpl extends UnicastRemoteObject implements LabService {
    private Map<String, Equipment> equipos = new HashMap<>();

    public LabServiceImpl() throws RemoteException {
        equipos.put("PC01", new Equipment("PC01", "Computador Dell", "Lab A"));
        equipos.put("PC02", new Equipment("PC02", "Computador HP",   "Lab A"));
        equipos.put("AR01", new Equipment("AR01", "Arduino Uno",     "Lab B"));
        equipos.put("AR02", new Equipment("AR02", "Raspberry Pi",    "Lab B"));
        equipos.put("OS01", new Equipment("OS01", "Osciloscopio",    "Lab C"));
    }

    @Override
    public List<String> consultarEquipos() throws RemoteException {
        List<String> lista = new ArrayList<>();
        for (Equipment e : equipos.values()) {
            lista.add(e.toString());
        }
        return lista;
    }

    @Override
    public String consultarEquipo(String codigo) throws RemoteException {
        Equipment e = equipos.get(codigo);
        if (e == null) return "ERROR: equipo no encontrado";
        return e.toString();
    }

    @Override
    public boolean reservarEquipo(String codigo) throws RemoteException {
        Equipment e = equipos.get(codigo);
        if (e == null || e.isReservado()) return false;
        e.reservar();
        return true;
    }

    @Override
    public boolean liberarEquipo(String codigo) throws RemoteException {
        Equipment e = equipos.get(codigo);
        if (e == null || !e.isReservado()) return false;
        e.liberar();
        return true;
    }
}