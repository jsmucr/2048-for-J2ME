package game2048.scene;

/**
 * Rozhraní implementované animovatelnými prvky scény.
 * 
 * @author Jan Šmucr
 */
public interface Animable
{
    /**
     * Požádá animovatelný prvek o provedení části animace.
     * @param msec Čas uplynulý od poslední aktualizace v milisekundách.
     * @return <code>true</code>, pokud došlo k animaci.
     */
    boolean animate(long msec);
}
