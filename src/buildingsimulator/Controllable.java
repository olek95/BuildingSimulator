package buildingsimulator;

/**
 * Interfejs <code>Controllable</code> reprezentuje wszystkie obiekty, które 
 * mogą być sterowane przez gracza. 
 * @author AleksanderSklorz
 */
public interface Controllable {
    /**
     * Zwraca wszystkie dostępne akcje dla danego obiektu. 
     * @return dostępne akcje 
     */
    public Control.Actions[] getAvailableActions();
}
