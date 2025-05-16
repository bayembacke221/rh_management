package sn.bmbacke.rh.entity.enums;

public enum NotificationType {
    LEAVE_REQUEST("Demande de congé"),
    LEAVE_APPROVED("Congé approuvé"),
    LEAVE_REJECTED("Congé rejeté"),
    NEW_EMPLOYEE("Nouvel employé"),
    CONTRACT_EXPIRING("Contrat expirant"),
    DOCUMENT_UPLOADED("Document téléchargé"),
    EVALUATION_DUE("Évaluation à effectuer"),
    BIRTHDAY("Anniversaire"),
    ANNOUNCEMENT("Annonce"),
    TASK_ASSIGNED("Tâche assignée"),
    SYSTEM("Notification système");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}