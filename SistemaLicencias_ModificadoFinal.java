import java.sql.*;
import java.util.*;

// Clase abstracta Persona
abstract class Persona {
    protected String nombre;
    protected String apellido;

    public Persona(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public abstract void mostrarInformacion();
}

// Clase Licencia que extiende Persona
class Licencia extends Persona {
    private String categoria;
    private String fechaInicio;
    private String fechaFin;
    private String nombreMedico;
    private String nombreServicio;
    private String estado;

    public Licencia(String nombre, String apellido, String categoria, String fechaInicio, String fechaFin,
                    String nombreMedico, String nombreServicio, String estado) {
        super(nombre, apellido);
        this.categoria = categoria;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.nombreMedico = nombreMedico;
        this.nombreServicio = nombreServicio;
        this.estado = estado;
    }

    @Override
    public void mostrarInformacion() {
        System.out.println("Licencia [Empleado: " + nombre + " " + apellido + ", Categoría: " + categoria +
                ", Inicio: " + fechaInicio + ", Fin: " + fechaFin +
                ", Médico: " + nombreMedico + ", Servicio: " + nombreServicio + ", Estado: " + estado + "]");
    }
}

// DAO para manejar licencias
class LicenciaDAO {
// Método para mostrar todas las licencias guardadas en la base de datos
    public ArrayList<Licencia> obtenerLicencias(Connection conn) {
        ArrayList<Licencia> licencias = new ArrayList<>();
        String query = "SELECT e.nombre, e.apellido, lc.Categoria, l.FechaInicio, l.FechaFin, " +
                "IFNULL(lm.NombreMed, 'Sin médico') AS Medico, " +
                "IFNULL(ls.NombreSer, 'Sin servicio') AS Servicio, " +
                "IFNULL(est.Estado, 'Sin estado') AS Estado " +
                "FROM licencia l " +
                "LEFT JOIN empleado e ON l.Legajo = e.Legajo " +
                "LEFT JOIN licenciacategoria lc ON l.idLicenciaCategoria = lc.idCatLic " +
                "LEFT JOIN licenciamedica lm ON l.IdMed = lm.idMed " +
                "LEFT JOIN licenciaservicio ls ON l.IdSer = ls.idSer " +
                "LEFT JOIN estado est ON l.IdEstado = est.idEst";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Licencia licencia = new Licencia(
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("Categoria"),
                        rs.getString("FechaInicio"),
                        rs.getString("FechaFin"),
                        rs.getString("Medico"),
                        rs.getString("Servicio"),
                        rs.getString("Estado")
                );
                licencias.add(licencia);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener licencias: " + e.getMessage());
        }
        return licencias;
    }
 // Método para insertar una nueva licencia en la base de datos.
    public void registrarLicencia(Connection conn, int idLicencia, int legajo, int idCatLic, Integer idMed, Integer idSer,
                                  int idEstado, String fechaInicio, String fechaFin, int diasTotal) {
        String query = "INSERT INTO licencia (idLicencia, Legajo, idLicenciaCategoria, IdMed, IdSer, IdEstado, FechaInicio, FechaFin, DiasTotal) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idLicencia);
            ps.setInt(2, legajo);
            ps.setInt(3, idCatLic);
            if (idMed != null) ps.setInt(4, idMed); else ps.setNull(4, Types.INTEGER);
            if (idSer != null) ps.setInt(5, idSer); else ps.setNull(5, Types.INTEGER);
            ps.setInt(6, idEstado);
            ps.setString(7, fechaInicio);
            ps.setString(8, fechaFin);
            ps.setInt(9, diasTotal);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al registrar licencia: " + e.getMessage());
        }
    }
// Método para modificar los datos de una licencia existente.
    public void modificarLicencia(Connection conn, int idLicencia, int idEstado, String fechaFin, int diasTotal) {
        String query = "UPDATE licencia SET IdEstado = ?, FechaFin = ?, DiasTotal = ? WHERE idLicencia = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idEstado);
            ps.setString(2, fechaFin);
            ps.setInt(3, diasTotal);
            ps.setInt(4, idLicencia);
            ps.executeUpdate();
            System.out.println("Licencia modificada correctamente.");
        } catch (SQLException e) {
            System.err.println("Error al modificar licencia: " + e.getMessage());
        }
    }
  // Método para eliminar una licencia por su ID.
    public void eliminarLicencia(Connection conn, int idLicencia) {
        String query = "DELETE FROM licencia WHERE idLicencia = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idLicencia);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar licencia: " + e.getMessage());
        }
    }
}
// Clase principal que maneja todo el sistema
public class SistemaLicencias_ModificadoFinal {
// Función para conectar con la base de datos.
    public static Connection conectarBD() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/licenciasdb";// URL de la base de datos 
        String usuario = "root";// Usuario de la base.
        String contrasena = "Luca1905+";// Contraseña del usuario 
        return DriverManager.getConnection(url, usuario, contrasena);// Se devuelve la conexión.
    }
    // Método principal
    public static void main(String[] args) {
       // Scanner para leer entrada del usuario

        Scanner sc = new Scanner(System.in);

        try (Connection conn = conectarBD()) {
            if (conn == null) {
                System.out.println("No se pudo conectar a la base de datos.");
                return;
            }
            System.out.println("Conexión exitosa.");
// Instancia de la clase DAO para acceder a la base de datos
            LicenciaDAO dao = new LicenciaDAO();
 // Menú de opciones
            while (true) {
                System.out.println("\n--- MENÚ ---");
                System.out.println("1. Ver licencias");
                System.out.println("2. Registrar nueva licencia");
                System.out.println("3. Modificar licencia existente");
                System.out.println("4. Eliminar licencia");
                System.out.println("5. Salir");

                int opcion = sc.nextInt();
                sc.nextLine();

                switch (opcion) {
                    case 1:
                        ArrayList<Licencia> licencias = dao.obtenerLicencias(conn);
                        if (licencias.isEmpty()) {
                            System.out.println("No hay licencias registradas.");
                        } else {
                            for (Licencia l : licencias) l.mostrarInformacion();
                        }
                        break;
                    case 2:
                        try {
                            System.out.println("ID de licencia:");
                            int idLicencia = sc.nextInt();
                            System.out.println("Legajo del empleado:");
                            int legajo = sc.nextInt();
                            System.out.println("ID Categoría de licencia:");
                            int idCat = sc.nextInt();

                            Integer idMed = null;
                            Integer idSer = null;
                            sc.nextLine();
                            System.out.println("¿Qué tipo de licencia es? (1 = Médica, 2 = Servicio):");
                            int tipo = sc.nextInt();

                            if (tipo == 1) {
                                System.out.println("ID Médico:");
                                idMed = sc.nextInt();
                            } else if (tipo == 2) {
                                System.out.println("ID Servicio:");
                                idSer = sc.nextInt();
                            }

                            System.out.println("ID Estado:");
                            int idEstado = sc.nextInt();
                            sc.nextLine();
                            System.out.println("Fecha de inicio (YYYY-MM-DD):");
                            String fi = sc.nextLine();
                            System.out.println("Fecha de fin (YYYY-MM-DD):");
                            String ff = sc.nextLine();
                            System.out.println("Cantidad de días totales:");
                            int dias = sc.nextInt();

                            dao.registrarLicencia(conn, idLicencia, legajo, idCat, idMed, idSer, idEstado, fi, ff, dias);
                            System.out.println("Licencia registrada correctamente.");
                        } catch (Exception ex) {
                            System.err.println("Error al registrar datos: " + ex.getMessage());
                        }
                        break;
                    case 3:
                        System.out.println("ID de la licencia a modificar:");
                        int idLicMod = sc.nextInt();
                        System.out.println("Nuevo ID Estado:");
                        int nuevoEstado = sc.nextInt();
                        sc.nextLine();
                        System.out.println("Nueva fecha de fin (YYYY-MM-DD):");
                        String nuevaFechaFin = sc.nextLine();
                        System.out.println("Nuevos días totales:");
                        int nuevosDias = sc.nextInt();
                        dao.modificarLicencia(conn, idLicMod, nuevoEstado, nuevaFechaFin, nuevosDias);
                        break;
                    case 4:
                        System.out.println("Ingrese el ID de la licencia a eliminar:");
                        int idLic = sc.nextInt();
                        dao.eliminarLicencia(conn, idLic);
                        System.out.println("Licencia eliminada.");
                        break;
                    case 5:
                        System.out.println("Saliendo...");
                        return;
                    default:
                        System.out.println("Opción inválida.");
                        break;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error general: " + e.getMessage());
        }
    }
}
