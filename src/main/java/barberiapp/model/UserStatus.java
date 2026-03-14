package barberiapp.model;

public enum UserStatus {
    PENDING,   // recién registrado, esperando aprobación
    ACTIVE,    // aprobado por super admin, puede operar normalmente
    REJECTED   // rechazado, no puede realizar acciones
}
