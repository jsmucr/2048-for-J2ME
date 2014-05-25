package game2048.scene;

/**
 * Rozhraní implementované posluchači uživatelského vstupu ze scény.
 * 
 * @author Jan Šmucr
 */
public interface UserActivityListener
{
    /**
     * Vyvolána, pokud uživatel stiskl klávesu.
     * @param key Kód klávesy.
     */
    void onKeyPressed(int key);
}
