package barberiapp.model;

public enum ApprovalStatus {
    PENDING,   // recién creado, esperando aprobación del super admin
    ACTIVE,    // aprobado y visible públicamente
    REJECTED   // rechazado, no visible al público
}
