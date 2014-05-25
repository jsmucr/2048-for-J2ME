package game2048;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Prázdné plátno určené k zobrazení namísto {@linkplain Scene}, když je potřeba
 * uvolnit paměť.
 * 
 * @author Jan Šmucr
 */
public final class EmptyCanvas extends Canvas
{

    protected void paint(final Graphics g)
    {
        
    }
    
}
