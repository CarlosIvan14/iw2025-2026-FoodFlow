package pos.domain;

public enum TableState {
    FREE,       // LIBRE
    OCCUPIED,   // OCUPADA
    RESERVED    // RESERVADA
    ;

    public TableState getState() {
        return this;
    }
}
