package game2048;

/**
 * Rozhraní, prostřednictvím kterého lze naslouchat událostem na herní desce.
 * 
 * @author Jan Šmucr
 */
public interface BoardChangeListener
{
    /**
     * Signalizuje, že na desce došlo k vytvoření čísla.
     * @param board Deska.
     * @param row Řádek, kde se nachází nové číslo.
     * @param col Sloupec, kde se nachází nové číslo.
     * @param last <code>true</code>, pokud šlo o poslední číslo a deska je nyní
     * plná.
     */
    void onNumberCreated(final Board board, int row, int col, boolean last);
    
    /**
     * Signalizuje, že na desce došlo ke spojení dvou stejných čísel.
     * @param board Deska.
     * @param movement Informace o tahu.
     */
    void onNumbersJoined(final Board board, final Movement movement);
    
    /**
     * Signalizuje, že na desce došlo k pohybu čísla.
     * @param board Deska.
     * @param movement Informace o tahu.
     */
    void onNumberMoved(final Board board, final Movement movement);
    
    /**
     * Signalizuje odebrání čísla z desky. To se může stát hlavně při
     * inicializaci nové hry nebo načítání předem uloženého stavu desky.
     * @param board Deska.
     * @param row Řádek, kde bylo odstraněno číslo.
     * @param col Sloupec, kde bylo odstraněno číslo.
     */
    void onNumberRemoved(final Board board, int row, int col);
}
