import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// Clase para representar una licencia
class Licencia {
    private int id;
    private int legajoEmpleado;
    private String categoria;
    private String tipo;
    private String estado;
    private int dias;
    private String documentoAdjunto;
    private String empleado;
    private String fechaInicio;
    private String fechaFin;
    private LocalDateTime createdAt;

    public Licencia(int id, int legajoEmpleado, String categoria, String tipo, int dias, 
                   String empleado, String fechaInicio, String fechaFin) {
        this.id = id;
        this.legajoEmpleado = legajoEmpleado;
        this.categoria = categoria;
        this.tipo = tipo;
        this.dias = dias;
        this.empleado = empleado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = "Solicitada";
        this.documentoAdjunto = "";
        this.createdAt = LocalDateTime.now();
    }

    // Getters y Setters
    public int getId() { return id; }
    public int getLegajoEmpleado() { return legajoEmpleado; }
    public String getCategoria() { return categoria; }
    public String getTipo() { return tipo; }
    public String getEstado() { return estado; }
    public int getDias() { return dias; }
    public String getDocumentoAdjunto() { return documentoAdjunto; }
    public String getEmpleado() { return empleado; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setEstado(String estado) { this.estado = estado; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setDias(int dias) { this.dias = dias; }
    public void setDocumentoAdjunto(String documento) { this.documentoAdjunto = documento; }

    public String getResumen() {
        return String.format("ID: %d | %s | %s - %s | %d días | Estado: %s | Empleado: %s", 
                           id, categoria, tipo, estado, dias, estado, empleado);
    }

    public String getDetalle() {
        return String.format("""
            ═══════════════════════════════════════
            LICENCIA ID: %d
            ═══════════════════════════════════════
            Empleado: %s (Legajo: %d)
            Categoría: %s
            Tipo: %s
            Estado: %s
            Días: %d
            Fecha Inicio: %s
            Fecha Fin: %s
            Documento Adjunto: %s
            Creada: %s
            ═══════════════════════════════════════
            """, id, empleado, legajoEmpleado, categoria, tipo, estado, dias, 
                 fechaInicio, fechaFin, 
                 documentoAdjunto.isEmpty() ? "Sin documento" : documentoAdjunto,
                 createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
}

// Clase para representar un usuario
class Usuario {
    private int id;
    private String username;
    private String password;
    private String role;
    private int legajo;

    public Usuario(int id, String username, String password, String role, int legajo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.legajo = legajo;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public int getLegajo() { return legajo; }
}

// Clase para gestionar el almacenamiento en memoria
class StorageManager {
    private Map<Integer, Usuario> usuarios;
    private Map<Integer, Licencia> licencias;
    private int currentUserId;
    private int currentLicenseId;

    public StorageManager() {
        this.usuarios = new HashMap<>();
        this.licencias = new HashMap<>();
        this.currentUserId = 1;
        this.currentLicenseId = 1;
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        createUser("camila", "1234", "empleado", 100);
        createUser("admin", "adminpass", "administrativo", 101);
        createUser("auditor", "auditpass", "auditor", 102);
    }

    public Usuario createUser(String username, String password, String role, int legajo) {
        Usuario user = new Usuario(currentUserId++, username, password, role, legajo);
        usuarios.put(user.getId(), user);
        return user;
    }

    public Usuario authenticateUser(String username, String password) {
        return usuarios.values().stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public Licencia createLicense(int legajoEmpleado, String categoria, String tipo, int dias,
                                String empleado, String fechaInicio, String fechaFin) {
        Licencia license = new Licencia(currentLicenseId++, legajoEmpleado, categoria, tipo, 
                                      dias, empleado, fechaInicio, fechaFin);
        licencias.put(license.getId(), license);
        return license;
    }

    public List<Licencia> getAllLicenses() {
        return new ArrayList<>(licencias.values());
    }

    public List<Licencia> getLicensesByEmployee(int legajo) {
        return licencias.values().stream()
                .filter(l -> l.getLegajoEmpleado() == legajo)
                .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
                .toList();
    }

    public Licencia getLicenseById(int id) {
        return licencias.get(id);
    }

    public boolean updateLicense(int id, String tipo, int dias, String estado) {
        Licencia license = licencias.get(id);
        if (license == null) return false;
        
        if (tipo != null) license.setTipo(tipo);
        if (dias > 0) license.setDias(dias);
        if (estado != null) license.setEstado(estado);
        
        return true;
    }

    public boolean attachDocument(int id, String documento) {
        Licencia license = licencias.get(id);
        if (license == null) return false;
        
        license.setDocumentoAdjunto(documento);
        return true;
    }
}

// Clase principal del sistema
public class SistemaLicenciasCompleto {
    private static StorageManager storage = new StorageManager();
    private static Scanner scanner = new Scanner(System.in);
    private static final Map<String, String[]> TIPOS_LICENCIA = Map.of(
        "Servicio", new String[]{"Vacaciones", "Adopción", "Maternidad", "Nacimiento", "Neonatología"},
        "Médica", new String[]{"Enfermedad", "Familiar enfermo", "Donación de sangre", "Prevención"}
    );

    public static void main(String[] args) {
        mostrarBienvenida();
        
        Usuario usuario = null;
        do {
            usuario = login();
            if (usuario == null) {
                System.out.println(" Credenciales incorrectas. Intente nuevamente.\n");
            }
        } while (usuario == null);

        menuPrincipal(usuario);
    }

    private static void mostrarBienvenida() {
        System.out.println("""
            ╔═══════════════════════════════════════════════════════════════╗
            ║                SISTEMA DE GESTIÓN Y ADMINSITRACIÓN DE LICENCIAS			    ║
            ║                           	Versión 2.0                        		    ║
            ╚═══════════════════════════════════════════════════════════════╝
            """);
    }

    private static Usuario login() {
        System.out.println(" INICIO DE SESIÓN");
        System.out.println("─".repeat(50));
        System.out.print("Usuario: ");
        String username = scanner.nextLine().trim();
        System.out.print("Contraseña: ");
        String password = scanner.nextLine().trim();
        
        return storage.authenticateUser(username, password);
    }

    private static void menuPrincipal(Usuario usuario) {
        int opcion;
        do {
            mostrarMenu(usuario);
            opcion = leerOpcion();
            
            switch (usuario.getRole()) {
                case "empleado" -> procesarMenuEmpleado(opcion, usuario);
                case "administrativo" -> procesarMenuAdministrativo(opcion, usuario);
                case "auditor" -> procesarMenuAuditor(opcion, usuario);
            }
            
            if (opcion == 9) {
                System.out.println(" Regresando al login...\n");
                main(new String[]{});
                return;
            }
        } while (opcion != 0);
        
        System.out.println(" ¡Hasta luego! Sistema cerrado.");
    }

    private static void mostrarMenu(Usuario usuario) {
        System.out.println("\n" + "═".repeat(60));
        System.out.printf(" Bienvenido: %s (%s) - Legajo: %d%n", 
                         usuario.getUsername(), 
                         usuario.getRole().toUpperCase(), 
                         usuario.getLegajo());
        System.out.println("═".repeat(60));
        
        switch (usuario.getRole()) {
            case "empleado" -> {
                System.out.println("1️⃣  Solicitar Nueva Licencia");
                System.out.println("2️⃣  Ver Mis Licencias");
            }
            case "administrativo" -> {
                System.out.println("1️⃣  Solicitar Nueva Licencia");
                System.out.println("2️⃣  Ver Todas las Licencias");
                System.out.println("3️⃣  Modificar Licencia");
                System.out.println("4️⃣  Adjuntar Documento");
                System.out.println("5️⃣  Estadísticas del Sistema");
            }
            case "auditor" -> {
                System.out.println("1️⃣  Ver Todas las Licencias");
                System.out.println("2️⃣  Modificar Licencia");
                System.out.println("3️⃣  Validar Licencia");
                System.out.println("4️⃣  Adjuntar Documento");
                System.out.println("5️⃣  Estadísticas del Sistema");
            }
        }
        
        System.out.println("9️⃣  Volver al Login");
        System.out.println("0️⃣  Salir del Sistema");
        System.out.println("─".repeat(60));
        System.out.print("Seleccione una opción: ");
    }

    private static int leerOpcion() {
        try {
            int opcion = Integer.parseInt(scanner.nextLine().trim());
            return opcion;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void procesarMenuEmpleado(int opcion, Usuario usuario) {
        switch (opcion) {
            case 1 -> solicitarLicencia(usuario);
            case 2 -> verMisLicencias(usuario);
            case 9, 0 -> {} // Manejado en menuPrincipal
            default -> System.out.println(" Opción inválida.");
        }
    }

    private static void procesarMenuAdministrativo(int opcion, Usuario usuario) {
        switch (opcion) {
            case 1 -> solicitarLicencia(usuario);
            case 2 -> verTodasLasLicencias();
            case 3 -> modificarLicencia(usuario.getRole());
            case 4 -> adjuntarDocumento();
            case 5 -> mostrarEstadisticas();
            case 9, 0 -> {} // Manejado en menuPrincipal
            default -> System.out.println(" Opción inválida.");
        }
    }

    private static void procesarMenuAuditor(int opcion, Usuario usuario) {
        switch (opcion) {
            case 1 -> verTodasLasLicencias();
            case 2 -> modificarLicencia(usuario.getRole());
            case 3 -> validarLicencia();
            case 4 -> adjuntarDocumento();
            case 5 -> mostrarEstadisticas();
            case 9, 0 -> {} // Manejado en menuPrincipal
            default -> System.out.println(" Opción inválida.");
        }
    }

    private static void solicitarLicencia(Usuario usuario) {
        System.out.println("\n SOLICITAR NUEVA LICENCIA");
        System.out.println("─".repeat(50));
        
        // Seleccionar categoría
        System.out.println("Categorías disponibles:");
        System.out.println("1) Servicio");
        System.out.println("2) Médica");
        System.out.print("Seleccione categoría (1-2): ");
        
        int categoriaIndex = leerOpcion();
        if (categoriaIndex < 1 || categoriaIndex > 2) {
            System.out.println(" Categoría inválida.");
            return;
        }
        
        String categoria = categoriaIndex == 1 ? "Servicio" : "Médica";
        String[] tipos = TIPOS_LICENCIA.get(categoria);
        
        // Seleccionar tipo
        System.out.println("\nTipos de licencia disponibles:");
        for (int i = 0; i < tipos.length; i++) {
            System.out.printf("%d) %s%n", i + 1, tipos[i]);
        }
        System.out.print("Seleccione tipo: ");
        
        int tipoIndex = leerOpcion() - 1;
        if (tipoIndex < 0 || tipoIndex >= tipos.length) {
            System.out.println(" Tipo inválido.");
            return;
        }
        
        String tipo = tipos[tipoIndex];
        
        // Solicitar fechas
        System.out.print("Fecha de inicio (yyyy-MM-dd): ");
        String fechaInicio = scanner.nextLine().trim();
        System.out.print("Fecha de fin (yyyy-MM-dd): ");
        String fechaFin = scanner.nextLine().trim();
        
        // Calcular días
        int dias = calcularDias(fechaInicio, fechaFin);
        if (dias <= 0) {
            System.out.println(" Las fechas son inválidas. La fecha de fin debe ser posterior a la de inicio.");
            return;
        }
        
        // Crear licencia
        Licencia licencia = storage.createLicense(usuario.getLegajo(), categoria, tipo, dias,
                                                usuario.getUsername(), fechaInicio, fechaFin);
        
        System.out.println("\n Licencia solicitada exitosamente!");
        System.out.printf(" Días calculados: %d%n", dias);
        System.out.printf(" ID de licencia: %d%n", licencia.getId());
    }

    private static int calcularDias(String fechaInicio, String fechaFin) {
        try {
            LocalDate inicio = LocalDate.parse(fechaInicio);
            LocalDate fin = LocalDate.parse(fechaFin);
            return (int) ChronoUnit.DAYS.between(inicio, fin) + 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private static void verMisLicencias(Usuario usuario) {
        System.out.println("\n MIS LICENCIAS");
        System.out.println("─".repeat(50));
        
        List<Licencia> licencias = storage.getLicensesByEmployee(usuario.getLegajo());
        if (licencias.isEmpty()) {
            System.out.println(" No tienes licencias registradas.");
            return;
        }
        
        for (Licencia licencia : licencias) {
            System.out.println(licencia.getDetalle());
        }
    }

    private static void verTodasLasLicencias() {
        System.out.println("\n TODAS LAS LICENCIAS");
        System.out.println("─".repeat(50));
        
        List<Licencia> licencias = storage.getAllLicenses();
        if (licencias.isEmpty()) {
            System.out.println(" No hay licencias registradas en el sistema.");
            return;
        }
        
        // Mostrar resumen
        System.out.println("RESUMEN:");
        for (Licencia licencia : licencias) {
            System.out.println(licencia.getResumen());
        }
        
        // Opción de ver detalle
        System.out.print("\n¿Ver detalle de alguna licencia? (ID o 0 para continuar): ");
        int id = leerOpcion();
        if (id > 0) {
            Licencia licencia = storage.getLicenseById(id);
            if (licencia != null) {
                System.out.println(licencia.getDetalle());
            } else {
                System.out.println(" Licencia no encontrada.");
            }
        }
    }

    private static void modificarLicencia(String role) {
        System.out.println("\n MODIFICAR LICENCIA");
        System.out.println("─".repeat(50));
        
        verTodasLasLicencias();
        System.out.print("\nIngrese ID de licencia a modificar (0 para cancelar): ");
        int id = leerOpcion();
        
        if (id == 0) return;
        
        Licencia licencia = storage.getLicenseById(id);
        if (licencia == null) {
            System.out.println(" Licencia no encontrada.");
            return;
        }
        
        System.out.println("\nLicencia actual:");
        System.out.println(licencia.getDetalle());
        
        // Modificar tipo
        System.out.printf("Tipo actual: %s%n", licencia.getTipo());
        System.out.print("Nuevo tipo (Enter para mantener): ");
        String nuevoTipo = scanner.nextLine().trim();
        if (nuevoTipo.isEmpty()) nuevoTipo = licencia.getTipo();
        
        // Modificar días
        System.out.printf("Días actuales: %d%n", licencia.getDias());
        System.out.print("Nuevos días (Enter para mantener): ");
        String diasStr = scanner.nextLine().trim();
        int nuevosDias = diasStr.isEmpty() ? licencia.getDias() : Integer.parseInt(diasStr);
        
        // Cambiar estado si es administrativo y está en "Solicitada"
        String nuevoEstado = licencia.getEstado();
        if (role.equals("administrativo") && licencia.getEstado().equals("Solicitada")) {
            System.out.print("¿Cambiar estado a 'Pendiente'? (s/n): ");
            if (scanner.nextLine().trim().toLowerCase().startsWith("s")) {
                nuevoEstado = "Pendiente";
            }
        }
        
        // Aplicar cambios
        if (storage.updateLicense(id, nuevoTipo, nuevosDias, nuevoEstado)) {
            System.out.println(" Licencia modificada exitosamente.");
        } else {
            System.out.println(" Error al modificar la licencia.");
        }
    }

    private static void validarLicencia() {
        System.out.println("\n VALIDAR LICENCIA");
        System.out.println("─".repeat(50));
        
        verTodasLasLicencias();
        System.out.print("\nIngrese ID de licencia a validar (0 para cancelar): ");
        int id = leerOpcion();
        
        if (id == 0) return;
        
        Licencia licencia = storage.getLicenseById(id);
        if (licencia == null) {
            System.out.println(" Licencia no encontrada.");
            return;
        }
        
        if (!licencia.getEstado().equals("Pendiente")) {
            System.out.println(" Solo se pueden validar licencias en estado 'Pendiente'.");
            return;
        }
        
        System.out.println("\nLicencia a validar:");
        System.out.println(licencia.getDetalle());
        
        System.out.println("Estados disponibles:");
        System.out.println("1) Otorgada");
        System.out.println("2) Rechazada");
        System.out.println("3) Anulada");
        System.out.println("4) Suspendida");
        System.out.print("Seleccione nuevo estado: ");
        
        int estadoIndex = leerOpcion();
        String nuevoEstado = switch (estadoIndex) {
            case 1 -> "Otorgada";
            case 2 -> "Rechazada";
            case 3 -> "Anulada";
            case 4 -> "Suspendida";
            default -> null;
        };
        
        if (nuevoEstado == null) {
            System.out.println(" Estado inválido.");
            return;
        }
        
        if (storage.updateLicense(id, null, 0, nuevoEstado)) {
            System.out.printf(" Licencia %s exitosamente.%n", nuevoEstado.toLowerCase());
        } else {
            System.out.println(" Error al validar la licencia.");
        }
    }

    private static void adjuntarDocumento() {
        System.out.println("\n ADJUNTAR DOCUMENTO");
        System.out.println("─".repeat(50));
        
        verTodasLasLicencias();
        System.out.print("\nIngrese ID de licencia (0 para cancelar): ");
        int id = leerOpcion();
        
        if (id == 0) return;
        
        Licencia licencia = storage.getLicenseById(id);
        if (licencia == null) {
            System.out.println(" Licencia no encontrada.");
            return;
        }
        
        System.out.print("Nombre del documento: ");
        String documento = scanner.nextLine().trim();
        
        if (documento.isEmpty()) {
            System.out.println(" Debe ingresar un nombre de documento.");
            return;
        }
        
        if (storage.attachDocument(id, documento)) {
            System.out.println(" Documento adjuntado exitosamente.");
        } else {
            System.out.println(" Error al adjuntar el documento.");
        }
    }

    private static void mostrarEstadisticas() {
        System.out.println("\n ESTADÍSTICAS DEL SISTEMA");
        System.out.println("─".repeat(50));
        
        List<Licencia> todasLasLicencias = storage.getAllLicenses();
        
        if (todasLasLicencias.isEmpty()) {
            System.out.println(" No hay datos para mostrar estadísticas.");
            return;
        }
        
        // Contar por estado
        Map<String, Long> porEstado = todasLasLicencias.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Licencia::getEstado,
                    java.util.stream.Collectors.counting()
                ));
        
        // Contar por categoría
        Map<String, Long> porCategoria = todasLasLicencias.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Licencia::getCategoria,
                    java.util.stream.Collectors.counting()
                ));
        
        System.out.printf(" Total de licencias: %d%n%n", todasLasLicencias.size());
        
        System.out.println("Por Estado:");
        porEstado.forEach((estado, count) -> 
            System.out.printf("  %s: %d%n", estado, count));
        
        System.out.println("\nPor Categoría:");
        porCategoria.forEach((categoria, count) -> 
            System.out.printf("  %s: %d%n", categoria, count));
        
        // Promedio de días
        double promedioDias = todasLasLicencias.stream()
                .mapToInt(Licencia::getDias)
                .average()
                .orElse(0.0);
        
        System.out.printf("%nPromedio de días por licencia: %.1f%n", promedioDias);
    }
}