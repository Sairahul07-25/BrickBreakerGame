import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * BrickBreaker - A Java game showcasing OOP principles, graphics, and event handling
 * 
 * This game demonstrates:
 * - Java Swing for GUI
 * - Object-oriented design with inheritance and encapsulation
 * - Game loop implementation
 * - Collision detection algorithms
 * - Event handling (keyboard and timer)
 * - Graphics rendering
 */
public class BrickBreaker extends JFrame {
    
    public static void main(String[] args) {
        // Use EventQueue to ensure thread safety
        EventQueue.invokeLater(() -> {
            BrickBreaker game = new BrickBreaker();
            game.setVisible(true);
        });
    }
    
    public BrickBreaker() {
        initializeUI();
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        gamePanel.startGame();
    }
    
    private void initializeUI() {
        setTitle("Brick Breaker - Java Skills Demo");
        setSize(800, 600);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    // Inner class for the game panel
    class GamePanel extends JPanel implements ActionListener, KeyListener {
        // Game constants
        private static final int DELAY = 10;
        private static final int PADDLE_WIDTH = 100;
        private static final int PADDLE_HEIGHT = 20;
        private static final int BALL_SIZE = 16;
        private static final int BRICK_WIDTH = 80;
        private static final int BRICK_HEIGHT = 30;
        private static final int BRICK_ROWS = 5;
        private static final int BRICK_COLUMNS = 9;
        
        // Game objects
        private Timer timer;
        private Paddle paddle;
        private Ball ball;
        private ArrayList<Brick> bricks;
        
        // Game state
        private boolean inGame = true;
        private boolean gameWon = false;
        private int score = 0;
        private int lives = 3;
        
        public GamePanel() {
            initializePanel();
            initializeGame();
        }
        
        private void initializePanel() {
            setBackground(Color.BLACK);
            setFocusable(true);
            addKeyListener(this);
        }
        
        private void initializeGame() {
            // Create paddle centered at bottom
            paddle = new Paddle(getWidth() / 2 - PADDLE_WIDTH / 2, 
                                getHeight() - 50, 
                                PADDLE_WIDTH, PADDLE_HEIGHT);
            
            // Create ball
            resetBall();
            
            // Create bricks array
            bricks = new ArrayList<>();
            
            // Initialize timer but don't start yet
            timer = new Timer(DELAY, this);
        }
        
        public void startGame() {
            // Wait for component to be sized properly
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    createBricks();
                    resetBall();
                    timer.start();
                    removeComponentListener(this);
                }
            });
        }
        
        private void resetBall() {
            ball = new Ball(getWidth() / 2 - BALL_SIZE / 2, 
                          getHeight() / 2 - BALL_SIZE / 2,
                          BALL_SIZE);
        }
        
        private void createBricks() {
            bricks.clear();
            int startX = (getWidth() - (BRICK_COLUMNS * BRICK_WIDTH)) / 2;
            int y = 50;
            
            for (int row = 0; row < BRICK_ROWS; row++) {
                Color brickColor = getRowColor(row);
                int pointValue = (BRICK_ROWS - row) * 10; // Higher rows worth more
                
                for (int col = 0; col < BRICK_COLUMNS; col++) {
                    int x = startX + col * BRICK_WIDTH;
                    Brick brick = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, brickColor, pointValue);
                    bricks.add(brick);
                }
                
                y += BRICK_HEIGHT + 5; // 5px gap between rows
            }
        }
        
        private Color getRowColor(int row) {
            switch (row % 5) {
                case 0: return new Color(200, 80, 80);  // Red
                case 1: return new Color(230, 150, 40); // Orange
                case 2: return new Color(200, 200, 60); // Yellow
                case 3: return new Color(80, 200, 80);  // Green
                case 4: return new Color(80, 80, 200);  // Blue
                default: return Color.WHITE;
            }
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (inGame) {
                drawGameObjects(g2d);
            } else {
                drawGameOver(g2d);
            }
            
            // Always draw score and lives
            drawStats(g2d);
            
            // Sync graphics for smoother animation
            Toolkit.getDefaultToolkit().sync();
        }
        
        private void drawGameObjects(Graphics2D g2d) {
            // Draw paddle
            paddle.draw(g2d);
            
            // Draw ball
            ball.draw(g2d);
            
            // Draw bricks
            for (Brick brick : bricks) {
                if (brick.isVisible()) {
                    brick.draw(g2d);
                }
            }
        }
        
        private void drawGameOver(Graphics2D g2d) {
            String message;
            
            if (gameWon) {
                message = "Congratulations! You won!";
            } else {
                message = "Game Over";
            }
            
            // Draw game over message
            Font font = new Font("Helvetica", Font.BOLD, 36);
            FontMetrics metrics = getFontMetrics(font);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(font);
            g2d.drawString(message, 
                         (getWidth() - metrics.stringWidth(message)) / 2, 
                         getHeight() / 2);
            
            // Draw restart message
            font = new Font("Helvetica", Font.PLAIN, 20);
            g2d.setFont(font);
            metrics = getFontMetrics(font);
            message = "Press SPACE to restart";
            g2d.drawString(message, 
                         (getWidth() - metrics.stringWidth(message)) / 2, 
                         getHeight() / 2 + 50);
        }
        
        private void drawStats(Graphics2D g2d) {
            // Draw score
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Helvetica", Font.BOLD, 16));
            g2d.drawString("Score: " + score, 20, 20);
            
            // Draw lives
            g2d.drawString("Lives: " + lives, getWidth() - 100, 20);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (inGame) {
                moveObjects();
                checkCollisions();
                checkGameState();
            }
            
            repaint();
        }
        
        private void moveObjects() {
            // Move paddle
            paddle.move(getWidth());
            
            // Move ball
            ball.move(getWidth(), getHeight());
        }
        
        private void checkCollisions() {
            // Check if ball hits bottom
            if (ball.getY() + ball.getSize() >= getHeight()) {
                loseLife();
                return;
            }
            
            // Check collision with paddle
            if (ball.getBounds().intersects(paddle.getBounds())) {
                // Calculate where on the paddle the ball hit
                double relativeIntersectX = (paddle.getX() + (paddle.getWidth() / 2)) - 
                                           (ball.getX() + (ball.getSize() / 2));
                
                // Normalize to [-1, 1]
                double normalizedRelativeIntersectionX = relativeIntersectX / (paddle.getWidth() / 2);
                
                // Calculate bounce angle (-60 to 60 degrees)
                double bounceAngle = normalizedRelativeIntersectionX * (Math.PI / 3);
                
                // Set new velocity
                double ballSpeed = Math.sqrt(ball.getDx() * ball.getDx() + ball.getDy() * ball.getDy());
                ball.setDx(ballSpeed * Math.sin(bounceAngle));
                ball.setDy(-ballSpeed * Math.cos(bounceAngle));
                
                // Ensure ball is above paddle (prevent multiple collisions)
                ball.setY(paddle.getY() - ball.getSize());
            }
            
            // Check collision with bricks
            for (Brick brick : bricks) {
                if (brick.isVisible() && ball.getBounds().intersects(brick.getBounds())) {
                    // Determine which side of the brick was hit
                    Rectangle ballBounds = ball.getBounds();
                    Rectangle brickBounds = brick.getBounds();
                    
                    // Calculate penetration depths
                    double overlapLeft = ballBounds.getMaxX() - brickBounds.getMinX();
                    double overlapRight = brickBounds.getMaxX() - ballBounds.getMinX();
                    double overlapTop = ballBounds.getMaxY() - brickBounds.getMinY();
                    double overlapBottom = brickBounds.getMaxY() - ballBounds.getMinY();
                    
                    // Find smallest overlap (which is the side we hit)
                    boolean hitHorizontalSide = Math.min(overlapLeft, overlapRight) <= 
                                              Math.min(overlapTop, overlapBottom);
                    
                    if (hitHorizontalSide) {
                        ball.setDx(-ball.getDx());
                    } else {
                        ball.setDy(-ball.getDy());
                    }
                    
                    // Destroy brick and add score
                    brick.setVisible(false);
                    score += brick.getPoints();
                    
                    // Only process one brick collision per frame
                    break;
                }
            }
        }
        
        private void loseLife() {
            lives--;
            if (lives <= 0) {
                gameOver();
            } else {
                resetBall();
            }
        }
        
        private void checkGameState() {
            // Check if all bricks are destroyed
            boolean allBricksDestroyed = true;
            for (Brick brick : bricks) {
                if (brick.isVisible()) {
                    allBricksDestroyed = false;
                    break;
                }
            }
            
            if (allBricksDestroyed) {
                gameWon = true;
                gameOver();
            }
        }
        
        private void gameOver() {
            inGame = false;
            timer.stop();
        }
        
        private void restartGame() {
            score = 0;
            lives = 3;
            gameWon = false;
            inGame = true;
            createBricks();
            resetBall();
            timer.start();
        }
        
        // KeyListener methods
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            
            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    paddle.setDx(-8);
                }
                
                if (key == KeyEvent.VK_RIGHT) {
                    paddle.setDx(8);
                }
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    restartGame();
                }
            }
        }
        
        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
                paddle.setDx(0);
            }
        }
        
        @Override
        public void keyTyped(KeyEvent e) {
            // Not used
        }
    }
    
    // Game object classes
    
    // Base GameObject class
    abstract class GameObject {
        protected double x;
        protected double y;
        protected int width;
        protected int height;
        protected Color color;
        
        public GameObject(double x, double y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public double getX() {
            return x;
        }
        
        public void setX(double x) {
            this.x = x;
        }
        
        public double getY() {
            return y;
        }
        
        public void setY(double y) {
            this.y = y;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public Rectangle getBounds() {
            return new Rectangle((int)x, (int)y, width, height);
        }
        
        public abstract void draw(Graphics2D g2d);
    }
    
    // Paddle class
    class Paddle extends GameObject {
        private double dx;
        
        public Paddle(double x, double y, int width, int height) {
            super(x, y, width, height);
            this.color = new Color(150, 150, 255);
            this.dx = 0;
        }
        
        public void setDx(double dx) {
            this.dx = dx;
        }
        
        public void move(int canvasWidth) {
            x += dx;
            
            // Keep paddle in bounds
            if (x < 0) {
                x = 0;
            }
            
            if (x + width > canvasWidth) {
                x = canvasWidth - width;
            }
        }
        
        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillRoundRect((int)x, (int)y, width, height, 15, 15);
            
            // Add 3D effect
            g2d.setColor(color.brighter());
            g2d.drawLine((int)x, (int)y, (int)x + width, (int)y);
            g2d.drawLine((int)x, (int)y, (int)x, (int)y + height);
            
            g2d.setColor(color.darker());
            g2d.drawLine((int)x, (int)y + height, (int)x + width, (int)y + height);
            g2d.drawLine((int)x + width, (int)y, (int)x + width, (int)y + height);
        }
    }
    
    // Ball class
    class Ball extends GameObject {
        private double dx;
        private double dy;
        private final int size;
        
        public Ball(double x, double y, int size) {
            super(x, y, size, size);
            this.size = size;
            this.color = Color.WHITE;
            
            // Initialize with random direction
            Random random = new Random();
            double angle = Math.PI / 4 + (random.nextDouble() * Math.PI / 2);
            double speed = 5;
            this.dx = speed * Math.cos(angle);
            this.dy = -speed * Math.sin(angle);
        }
        
        public int getSize() {
            return size;
        }
        
        public double getDx() {
            return dx;
        }
        
        public void setDx(double dx) {
            this.dx = dx;
        }
        
        public double getDy() {
            return dy;
        }
        
        public void setDy(double dy) {
            this.dy = dy;
        }
        
        public void move(int canvasWidth, int canvasHeight) {
            x += dx;
            y += dy;
            
            // Bounce off walls
            if (x < 0) {
                x = 0;
                dx = -dx;
            }
            
            if (x + size > canvasWidth) {
                x = canvasWidth - size;
                dx = -dx;
            }
            
            if (y < 0) {
                y = 0;
                dy = -dy;
            }
        }
        
        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillOval((int)x, (int)y, size, size);
            
            // Add 3D effect
            g2d.setColor(color.darker());
            g2d.drawOval((int)x, (int)y, size, size);
        }
    }
    
    // Brick class
    class Brick extends GameObject {
        private boolean visible;
        private int points;
        
        public Brick(double x, double y, int width, int height, Color color, int points) {
            super(x, y, width, height);
            this.color = color;
            this.visible = true;
            this.points = points;
        }
        
        public boolean isVisible() {
            return visible;
        }
        
        public void setVisible(boolean visible) {
            this.visible = visible;
        }
        
        public int getPoints() {
            return points;
        }
        
        @Override
        public void draw(Graphics2D g2d) {
            if (visible) {
                g2d.setColor(color);
                g2d.fillRect((int)x, (int)y, width, height);
                
                // Add 3D effect
                g2d.setColor(color.brighter());
                g2d.drawLine((int)x, (int)y, (int)x + width, (int)y);
                g2d.drawLine((int)x, (int)y, (int)x, (int)y + height);
                
                g2d.setColor(color.darker());
                g2d.drawLine((int)x, (int)y + height, (int)x + width, (int)y + height);
                g2d.drawLine((int)x + width, (int)y, (int)x + width, (int)y + height);
                
                // Draw a subtle pattern
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.drawLine((int)x + 10, (int)y, (int)x + 10, (int)y + height);
                g2d.drawLine((int)x + width - 10, (int)y, (int)x + width - 10, (int)y + height);
            }
        }
    }
}