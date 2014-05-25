package game2048.scene;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Základní třída pro implementaci dialogů. Poskytuje možnosti animace zobrazení
 * a skrytí dialogu a potomkovi přesně definuje oblasti, kam může vykreslit text
 * hlášky a text voleb pro akční tlačítka.
 * 
 * @author Jan Šmucr
 */
public abstract class Message implements Drawable, Animable
{
    private static final int BACKGROUND = 0xFFFFFF;
    private static final int FOREGROUND = 0x000000;
    private static final int PADDING = 10;
    private static final float ANIMATION_DURATION = 100f;
    private static final int MIN_CONTENT_HEIGHT = 0;
    
    private final Canvas canvas;
    private int currentY;
    private long animationTime;
    private int height;
    private int contentWidth;
    private int contentHeight;
    private int optionPaneWidth;
    private int optionPaneHeight;
    private boolean visible = false;
    private boolean appearing = false;
    private boolean disappearing = false;
    private Runnable runOnAppear;
    private Runnable runOnDisappear;
    
    public Message(final Canvas canvas)
    {
        this.canvas = canvas;
    }

    /**
     * @see Drawable#draw(javax.microedition.lcdui.Graphics) 
     */
    public final void draw(final Graphics graphics)
    {
        if (!visible)
        {
            return;
        }

        final int canvasWidth = canvas.getWidth();
        final int canvasHeight = canvas.getHeight();
        
        graphics.setColor(BACKGROUND);
        graphics.fillRect(0, currentY, canvasWidth, canvasHeight - currentY);

        graphics.setColor(FOREGROUND);
        graphics.drawLine(0, currentY, canvasWidth, currentY);

        final int translateX = PADDING;
        int translateY = currentY + PADDING + 1;
        final int clipX = graphics.getClipX();
        final int clipY = graphics.getClipY();
        final int clipWidth = graphics.getClipWidth();
        final int clipHeight = graphics.getClipHeight();
        
        graphics.setClip(translateX, translateY, contentWidth, contentHeight); 
        
        graphics.translate(translateX, translateY);
        drawContent(graphics);       
        //graphics.drawRect(0, 0, getContentWidth(), getContentHeight());
        graphics.translate(-translateX, -translateY);
        
        translateY += contentHeight;
        translateY += PADDING;
        translateY += PADDING;
        
        graphics.setClip(translateX, translateY, optionPaneWidth, optionPaneHeight); 
        
        graphics.translate(translateX, translateY);
        drawOptionPane(graphics);
        //graphics.drawRect(0, 0, getOptionPaneWidth(), getOptionPaneHeight()); 
        graphics.translate(-translateX, -translateY);
        
        graphics.clipRect(clipX, clipY, clipWidth, clipHeight);
    }

    /**
     * @see Animable#animate(long) 
     */
    public final boolean animate(final long msec)
    {
        if (!(appearing || disappearing))
        {
            return false;
        }
        
        animationTime += msec;
        final float visiblePartHeight;
        
        if (appearing)
        {
            visiblePartHeight = height * animationTime / ANIMATION_DURATION;            
            if (visiblePartHeight >= height)
            {
                currentY = canvas.getHeight() - height;
                appearing = false;
                if (runOnAppear != null)
                {
                    new Thread(runOnAppear).start();
                }
                return true;
            }           
        }
        else // if (disappearing)
        {
            visiblePartHeight = height * (ANIMATION_DURATION - animationTime) / ANIMATION_DURATION;            
            if (visiblePartHeight <= 0f)
            {
                currentY = canvas.getHeight();
                disappearing = false;
                visible = false;
                if (runOnDisappear != null)
                {
                    new Thread(runOnDisappear).start();
                }
                return true;
            }           
        }

        currentY = (int) (canvas.getHeight() - visiblePartHeight);           
        return true;
    }

    /**
     * Animuje zobrazení hlášky.
     * @param runOnAppear Pokud není nastaveno na <code>null</code>, provede se
     * po dokončení animace.
     */
    public final void appear(final Runnable runOnAppear)
    {
        if (appearing || disappearing)
        {
            return;
        }
        
        this.visible = true;
        this.appearing = true;
        this.runOnAppear = runOnAppear;
        this.currentY = canvas.getHeight();
        this.animationTime = 0;
        this.contentHeight = Math.max(getContentHeight() + 1, MIN_CONTENT_HEIGHT);
        this.contentWidth = canvas.getWidth() - 2 * PADDING;
        this.optionPaneHeight = getOptionPaneHeight() + 1;
        this.optionPaneWidth = contentWidth;
        this.height = 4 * PADDING + contentHeight + optionPaneHeight + 1;
    }
    
    /**
     * Animuje skrytí hlášky.
     * @param runOnDisappear Pokud není nastaveno na <code>null</code>, provede se
     * po dokončení animace.
     */
    public final void disappear(final Runnable runOnDisappear)
    {
        if (appearing || disappearing)
        {
            return;
        }
        
        this.disappearing = true;
        this.runOnDisappear = runOnDisappear;
        this.animationTime = 0;
    }

    /**
     * Vrací maximální šířku obsahu, který může potomek vykreslit.
     * @return Šířka v pixelech.
     */
    protected final int getContentWidth()
    {
        return contentWidth - 1;
    }

    /**
     * Vrací maximální šířku textu voleb akčních tlačítek.
     * @return Šířka v pixelech.
     */
    protected final int getOptionPaneWidth()
    {
        return optionPaneWidth - 1;
    }

    /**
     * @return Plátno.
     */
    protected final Canvas getCanvas()
    {
        return canvas;
    }
    
    /**
     * @return <code>true</code> pokud je alespoň část okénka hlášky vidět.
     */
    public final boolean isVisible()
    {
        return visible;
    }
    
    /**
     * @return <code>true</code> pokud běží animace zobrazování okna.
     */
    public final boolean isAppearing()
    {
        return appearing;
    }
    
    /**
     * @return <code>true</code> pokud běží animace skrývání okna.
     */
    public final boolean isDisappearing()
    {
        return disappearing;
    }
    
    /**
     * Vykreslí obsah hlášky.
     * @param graphics Cíl.
     */
    protected abstract void drawContent(Graphics graphics);
    
    /**
     * Vykreslí text akčních tlačítek.
     * @param graphics Cíl.
     */
    protected abstract void drawOptionPane(Graphics graphics);
    
    /**
     * @return Výška textu akčních tlačítek.
     */
    protected abstract int getOptionPaneHeight();
    
    /**
     * @return Výška obsahu hlášky.
     */
    protected abstract int getContentHeight();
}
