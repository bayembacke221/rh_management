package sn.bmbacke.rh.entity.enums;

public enum LeaveType {
    ANNUAL_LEAVE("Congé annuel"),
    SICK_LEAVE("Congé maladie"),
    MATERNITY_LEAVE("Congé maternité"),
    PATERNITY_LEAVE("Congé paternité"),
    UNPAID_LEAVE("Congé sans solde"),
    BEREAVEMENT_LEAVE("Congé pour décès"),
    MARRIAGE_LEAVE("Congé mariage"),
    TRAINING_LEAVE("Congé formation"),
    SABBATICAL_LEAVE("Congé sabbatique"),
    OTHER("Autre");

    private final String displayName;

    LeaveType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}