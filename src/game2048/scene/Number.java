package game2048.scene;

import game2048.Direction;
import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Třída reprezentující a vykreslující jedno políčko na herní desce ve scéně.
 *
 * @author Jan Šmucr
 */
public final class Number implements Animable, Drawable
{
    
    public static final int WIDTH = 50;
    public static final int HEIGHT = 50;
    private static final int HALF_WIDTH = WIDTH / 2;
    private static final int HALF_HEIGHT = HEIGHT / 2;
    private static final float APPEARING_DEFAULT_HORIZONTAL_MARGIN = HALF_WIDTH;
    private static final float APPEARING_DEFAULT_VERTICAL_MARGIN = HALF_HEIGHT;
    private static final float APPEARING_DURATION = 75f;
    private static final float MOVEMENT_DURATION = 100f;
    private static final Hashtable IMAGES = new Hashtable(16);

    private final String number;
    private final int foreground, background;
    private final int roundness;
    private int currentX, currentY;
    private final Image numberImage;
    private final int imageOffsetX;
    private final int imageOffsetY;

    private int moveDestinationX, moveDestinationY;
    private float moveDistance;
    private int moveDirection;
    private Runnable runOnMovementStop = null;
    private float movingSpeed;
    private boolean moving = false;

    private float horizontalClipMargin;
    private float verticalClipMargin;
    private Runnable runOnAppear = null;
    private float verticalAppearingSpeed;
    private float horizontalAppearingSpeed;
    private boolean appeared = false;
    private boolean appearing = false;

    public Number(final int number, final int x, final int y, final int roundness)
    {
        this.number = Integer.toString(number);
        this.roundness = roundness;
        currentX = x;
        currentY = y;

        switch (number)
        {
            case 2:
                background = 0xEEE4DA;
                break;
            case 4:
                background = 0xEDE0C8;
                break;
            case 8:
                background = 0xF2B179;
                break;
            case 16:
                background = 0xF59563;
                break;
            case 32:
                background = 0xF67C5F;
                break;
            case 64:
                background = 0xF65E3B;
                break;
            case 128:
                background = 0xEDCF72;
                break;
            case 256:
                background = 0xEDCC61;
                break;
            case 512:
                background = 0xEDC850;
                break;
            case 1024:
                background = 0xEDC53F;
                break;
            case 2048:
                background = 0xEDC22E;
                break;
            default:
                background = 0x3C3A32;
                break;
        }

        switch (number)
        {
            case 2:
            case 4:
                foreground = 0x000000;
                break;
            default:
                foreground = 0xF9F6F2;
                break;
        }

        numberImage = getImage(number);
        if (numberImage == null)
        {
            imageOffsetX = 0;
            imageOffsetY = 0;
        }
        else
        {
            imageOffsetX = (WIDTH - numberImage.getWidth()) / 2;
            imageOffsetY = (HEIGHT - numberImage.getHeight()) / 2;
        }
    }

    private static synchronized Image getImage(final int number)
    {
        try
        {
            final Object numObject = new Integer(number);
            Image image = (Image) IMAGES.get(numObject);
            if (image == null)    
            {
                image = Image.createImage(Number.class.getResourceAsStream("images/numbers/" + Integer.toString(number) + ".png"));
                IMAGES.put(numObject, image);
            }
            return image;
        } catch (IOException e)
        {
            System.err.println("Failed to load image for number " + number + ".");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @see Animable#animate(long) 
     */
    public final boolean animate(final long timePassedMsec)
    {
        if (!(moving || appearing))
        {
            return false;
        }

        if (appearing)
        {
            animateAppearing(timePassedMsec);
        }

        if (moving)
        {
            animateMovement(timePassedMsec);
        }

        return true;
    }

    /**
     * @see Drawable#draw(javax.microedition.lcdui.Graphics) 
     */
    public final void draw(final Graphics graphics)
    {
        int oldClipX = 0, oldClipY = 0, oldClipWidth = 0, oldClipHeight = 0;
        
        if (!appeared)
        {
            if (!appearing)
            {
                return;
            }

            final int vMargin = (int) this.verticalClipMargin;
            final int hMargin = (int) this.horizontalClipMargin;
            oldClipX = graphics.getClipX();
            oldClipY = graphics.getClipY();
            oldClipWidth = graphics.getClipWidth();
            oldClipHeight = graphics.getClipHeight();
            graphics.setClip(currentX + hMargin, currentY + vMargin, WIDTH - hMargin * 2, HEIGHT - vMargin * 2);
        }

        drawAt(currentX, currentY, graphics);
        
        if (!appeared && appearing)
        {
            graphics.setClip(oldClipX, oldClipY, oldClipWidth, oldClipHeight);
        }              
    }
    
    private void drawAt(final int x, final int y, final Graphics graphics)
    {
        graphics.setColor(background);
        graphics.fillRoundRect(x, y, WIDTH, HEIGHT, roundness, roundness);

        if (numberImage == null)
        {
            graphics.setColor(foreground);
            graphics.drawString(number, x + HALF_WIDTH, y + HALF_HEIGHT, Graphics.BASELINE | Graphics.HCENTER);
        } else
        {
            graphics.drawImage(numberImage, x + imageOffsetX, y + imageOffsetY, Graphics.LEFT | Graphics.TOP);
        }
    }

    /**
     * Spustí animaci zobrazování políčka.
     * @param runOnDone Pokud není <code>null</code>, bude provedeno po
     * dokončení animace.
     */
    public final void appear(final Runnable runOnDone)
    {
        if (appeared)
        {
            return;
        }
        appearing = true;
        verticalClipMargin = APPEARING_DEFAULT_VERTICAL_MARGIN;
        horizontalClipMargin = APPEARING_DEFAULT_HORIZONTAL_MARGIN;
        runOnAppear = runOnDone;
        verticalAppearingSpeed = verticalClipMargin / APPEARING_DURATION;
        horizontalAppearingSpeed = horizontalClipMargin / APPEARING_DURATION;
    }

    /**
     * Provede posun políčka.
     * @param distance Vzdálenost v pixelech.
     * @param direction Směr (konstanta z třídy {@linkplain Direction}).
     * @param runOnMovementStop Pokud není <code>null</code>, bude provedeno po
     * dokončení animace.
     */
    public final void move(final int distance, final int direction, final Runnable runOnMovementStop)
    {
        moving = true;
        moveDistance = distance;
        moveDirection = direction;

        switch (direction)
        {
            case Direction.LEFT:
                moveDestinationX = currentX - distance;
                moveDestinationY = currentY;
                break;
            case Direction.UP:
                moveDestinationX = currentX;
                moveDestinationY = currentY - distance;
                break;
            case Direction.RIGHT:
                moveDestinationX = currentX + distance;
                moveDestinationY = currentY;
                break;
            case Direction.DOWN:
                moveDestinationX = currentX;
                moveDestinationY = currentY + distance;
                break;
            default:
                throw new IllegalArgumentException("direction");
        }

        this.movingSpeed = distance / MOVEMENT_DURATION;
        this.runOnMovementStop = runOnMovementStop;
    }

    private void animateMovement(final long timePassedMsec)
    {
        final float delta = timePassedMsec * movingSpeed;
        moveDistance -= delta;
        if (moveDistance <= 0.0f)
        {
            currentX = moveDestinationX;
            currentY = moveDestinationY;
            moving = false;
            if (runOnMovementStop != null)
            {
                //new Thread(runOnMovementStop).start();
                runOnMovementStop.run();
            }
            return;
        }

        switch (moveDirection)
        {
            case Direction.LEFT:
                currentX = (int) ((float) moveDestinationX + moveDistance);
                break;
            case Direction.UP:
                currentY = (int) ((float) moveDestinationY + moveDistance);
                break;
            case Direction.RIGHT:
                currentX = (int) ((float) moveDestinationX - moveDistance);
                break;
            case Direction.DOWN:
                currentY = (int) ((float) moveDestinationY - moveDistance);
                break;
        }
    }

    private void animateAppearing(final long timePassedMsec)
    {
        final float verticalMarginChange = timePassedMsec * verticalAppearingSpeed;
        final float horizontalMarginChange = timePassedMsec * horizontalAppearingSpeed;

        boolean verticalOk = false;
        boolean horizontalOk = false;

        if (verticalMarginChange > verticalClipMargin)
        {
            verticalClipMargin = 0f;
            verticalOk = true;
        } else
        {
            verticalClipMargin -= verticalMarginChange;
        }

        if (horizontalMarginChange > horizontalClipMargin)
        {
            horizontalClipMargin = 0f;
            horizontalOk = true;
        } else
        {
            horizontalClipMargin -= horizontalMarginChange;
        }

        if (verticalOk && horizontalOk)
        {
            appearing = false;
            appeared = true;
            if (runOnAppear != null)
            {
                //new Thread(runOnAppear).start();
                runOnAppear.run();
            }
        }
    }
}
