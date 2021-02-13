package frc.robot.Toolkit;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.util.Color;

public class CT_LEDStrip extends AddressableLED{

    private AddressableLEDBuffer m_LEDBuffer;
    private int m_rainbowFirstPixelHue = 0;
    
    private int m_snakeLoopIndex = 0;
    private int m_snakeCounter = 0;

    private int m_movingColorsCounter = 0;
    private int m_movingColorIndex = 0;

    public enum Speed {
        Slow, // 50 loop iterations
        Fast, // 25 loop iterations
        VeryFast, // 10 loop iterations
        Ludicrous // 1 loop iteration
    }

    /**
     * Creates a default CT_LED instance where the LED length is the max.
     * 
     * @param PWMPort the PWM port the LED Strip is connected.
     */
    public CT_LEDStrip(int PWMPort) {
        this(PWMPort, 150); // 150 is the max amount of LEDS on a standard LED strip
    }

    /**
     * Creates a CT_LED instance where the LED length can be passed in.
     * 
     * @param PWMPort the PWM port the LED Strip is connected.
     * @param length the amount of individual LEDS that will be turned on and affected by color chanes.
     */
    public CT_LEDStrip(int PWMPort, int length) {
        super(PWMPort);
        m_LEDBuffer = new AddressableLEDBuffer(length);
        setLength(m_LEDBuffer.getLength());
        setData(m_LEDBuffer);
    }

    /**
     * Sets the colors of the LED strip. Colors passed in will retain their order on the LED strip.
     * For example, if (Color.kRed, Color.kBlue) is passed in, the pattern "red, blue, red, blue, red, blue" 
     * will repeat on the LED strip. 
     * If the LED Strip draws too much current, using the color white too often or having the LED strip length too long 
     * might cause colors to be dimmed or show up on the LED strip incorrectly. (e.g. 2 amps will not support a 150 white LED strip)
     * 
     * @param color the colors to be shown on the LED Strip.
     */
    public void setColor(Color... color) {

        if(color.length > 0) {

            int colorIndex = 0;

            for (int ledIndex = 0; ledIndex < m_LEDBuffer.getLength(); ledIndex++) {

                if(colorIndex == color.length - 1) { // Cycles the colorIndex variable to rotate through the color array.
                    colorIndex = 0;
                } else {
                    colorIndex++;
                }

                m_LEDBuffer.setLED(ledIndex, color[colorIndex]);

            }

            setData(m_LEDBuffer);
        }
    }

    /**
     * Sets colors that will move along the LED strip. This method should be called
     * in the periodic of a subsystem to gain full effect. 
     * 
     * @param speed the speed at which the snake will move. Current speeds:
     *              Slow, Fast, Ludicrous.
     * @param color the colors that will be moving on the LED strip. More than 1 color should be passed in or the method
     * will not work.
     */
    public void setMovingColors(Speed speed, Color... color) {

        if (color.length <= 1) {
            System.out.println("Too little amount of colors passed in, pass in more colors or use the setColor method.");
            return;
        }

        int colorLoopIndex = m_movingColorIndex;

        // Waits for the correct amount of loop iterations to complete.
        if(hasWaited(speed, m_movingColorsCounter)) {

            // Loops through the whole LED strip.
            for (int ledIndex = 0; ledIndex < m_LEDBuffer.getLength(); ledIndex++) {
    
                // Increment the color loop index
                colorLoopIndex++;

                // Reset the color loop index back to 0 if it excedes the amount of values in the array.
                if (colorLoopIndex > (color.length - 1)) {
                    colorLoopIndex = 0;
                } 

                m_LEDBuffer.setLED(ledIndex, color[colorLoopIndex]);
            }

            // Increment the color index so the color pattern is off by one on the next time this method is run. 
            // This gives the effect of the colors moving down the LED strip.
            m_movingColorIndex++;

            // Put the color index back to 0 if it excedes the amount of values in the array.
            if (m_movingColorIndex > (color.length - 1)) {
                m_movingColorIndex = 0;
            }
            m_movingColorsCounter = 0;

        } else {
            m_movingColorsCounter++;
        }

        setData(m_LEDBuffer);
    }

    /**
     * Creates a snake pattern on the LED strip where the pattern contained in the
     * snakeColorPattern array moves over the background color. This method should
     * be called in the periodic of a subsystem to gain full effect.
     * 
     * @param speed             the speed at which the snake will move. Current 
     *                          speeds: Slow, Fast, Ludicrous.
     * @param backgroundColor   the color that will be the background that the snake will travel over.
     * @param snakeColorPattern the snake pattern that will traverse the LED strip. 
     * Length needs to be greater than 0 or the method will not work.
     */
    public void doSnake(Speed speed, Color backgroundColor, Color[] snakeColorPattern) {
        
        if (snakeColorPattern.length == 0) {
            System.out.println("Snake length is zero, create a longer snake by making the snakeColorPattern array longer.");
            return;
        }

        // Waits for the correct amount of loop iterations to complete.
        if(hasWaited(speed, m_snakeCounter)) {

            // Loops through the whole LED strip.
            for (int ledIndex = 0; ledIndex < m_LEDBuffer.getLength(); ledIndex++) {

                 // Checks if the current led is the one that will start the snake.
                if(ledIndex == m_snakeLoopIndex) {

                    int endOfSnakeIndex = ledIndex + snakeColorPattern.length;
                    int currentColorIndex = 0;

                    // Loops through the entire snake.
                    for(int snakeIndex = ledIndex; snakeIndex < m_LEDBuffer.getLength() + snakeColorPattern.length; snakeIndex++) {
                        

                        // If the current color index is outside the color array, that means the snake is complete.
                        if(currentColorIndex == snakeColorPattern.length) { 
                            break; 
                        }

                        int newSnakeIndex = snakeIndex;
                         
                        // Moves the new snake index back to the beginning when the snake goes over 
                        // the end so it flows smoothly to the beginning again.
                        if(snakeIndex > m_LEDBuffer.getLength() - 1) {
                            newSnakeIndex -= m_LEDBuffer.getLength(); 
                        }

                        m_LEDBuffer.setLED(newSnakeIndex, snakeColorPattern[currentColorIndex]);
                        currentColorIndex++;

                    }

                    // Advances the ledIndex to the end of the snake.
                    ledIndex = endOfSnakeIndex - 1;

                } else {
                    m_LEDBuffer.setLED(ledIndex, backgroundColor);
                }

            }
            m_snakeCounter = 0;

            m_snakeLoopIndex++;

            // Reset the snake loop index back to the beginning when it reaches the end.
            if (m_snakeLoopIndex > m_LEDBuffer.getLength() - 1) {
                m_snakeLoopIndex = 0;
            }

        } else {
            m_snakeCounter++;
        }

        setData(m_LEDBuffer);
    }

    private boolean hasWaited(Speed speed, int counter) {
        if(counter == 50 && speed == Speed.Slow ||
           counter == 25 && speed == Speed.Fast ||
           counter == 10 && speed == Speed.VeryFast ||
           counter == 1 && speed == Speed.Ludicrous) {

            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Creates a rainbow effect on the LED strip.
     * This method should be called in the periodic of a subsystem to gain full effect.
     * 
     * Code is from https://docs.wpilib.org/en/stable/docs/software/actuators/addressable-leds.html 
     * with a few modifcations to make it more readable and standard.
     */
    public void doRainbow() {
        // For every pixel
        for (int i = 0; i < m_LEDBuffer.getLength(); i++) {
            // Calculate the hue - hue is easier for rainbows because the color
            // shape is a circle so only one value needs to precess
            int hue = (m_rainbowFirstPixelHue + (i * 180 / m_LEDBuffer.getLength())) % 180;
            // Set the value
            m_LEDBuffer.setHSV(i, hue, 255, 128);
        }

        // Increase by to make the rainbow "move"
        m_rainbowFirstPixelHue += 3;
        // Check bounds
        m_rainbowFirstPixelHue %= 180;

        setData(m_LEDBuffer);
    }
}