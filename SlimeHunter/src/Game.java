import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;


public class Game extends Canvas implements Runnable {

    public static final long serialVersionUID = 1L;
    public static int WIDTH = 640, HEIGHT = WIDTH / 12*9;
    public String title = "Slime Hunter";

    public Thread thread;
    private boolean isRunning = false;

    private Handler handler;
    private KeyInput keyInput;
    private MouseInput mouseInput;
    private Camera camera;
    private Level level;
    private SpriteSheet sheet;
    private HUD hud;
    private Menu menu;
    private Fonts customFonts;
    private Sound music;
    public GAMESTATE gameState;

    public static enum GAMESTATE {
        MENU,
        GAME, 
        PAUSE,
    };

    public static void main(String args[]) {
        new Game();
    }

    public Game() {
        new Window(WIDTH, HEIGHT, title, this);
        start();
    }

    private synchronized void start() {
        if (isRunning)
            return;
        thread = new Thread(this);
        thread.start();
        isRunning = true;
    }

    private synchronized void stop() {
        if (!isRunning)
            return;
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        this.requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        int ticks = 0, frames = 0;
        long timer = System.currentTimeMillis();

        init();
        music.playInLoop();

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                ticks++;
                tick();
                delta--;
            }
            render();
            frames++;
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("Frames: " + frames + ", Ticks: " + ticks);
                frames = 0;
                ticks = 0;
            }
        }
        stop();
    }

    private void init() {
        handler = new Handler();
        keyInput = new KeyInput();
        camera = new Camera(0, 0, WIDTH, HEIGHT, handler);
        sheet = new SpriteSheet("res/level1tileset.png");
        level = new Level("res/level1.txt", sheet, camera, handler);
        music = new Sound("res/story time.wav", 0.5f);
        customFonts = new Fonts();
        hud = new HUD(handler, customFonts.font_12);
        menu = new Menu(handler, WIDTH, HEIGHT, customFonts);
        mouseInput = new MouseInput(handler, camera, level, this, menu);

        gameState = GAMESTATE.MENU;
        handler.newPlayer(new Player(32, 32, keyInput, camera, level, handler, true, "res/player2.png"));

        this.addKeyListener(keyInput);
        this.addMouseListener(mouseInput);
    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();

        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        if(gameState == GAMESTATE.GAME) {
            level.render(g);
            handler.render(g);
            hud.render(g);
        } else if (gameState == GAMESTATE.MENU) {
            menu.render(g);
        } else {
            g.setFont(customFonts.font_48);
            g.setColor(Color.white);
            g.drawString("Game Paused", 100, 100);
        }
        
        bs.show();
        g.dispose();
    }

    private void tick() {
        if(handler.isEndGame())
        {
            gameState = GAMESTATE.MENU;
            music.play();
            menu.menuType = 3;
        }
        if (gameState == GAMESTATE.GAME) {
            handler.tick();
            camera.tick();
            level.tick();
            hud.tick();
        } else if(gameState == GAMESTATE.MENU) {
            menu.tick();
        }
    }

	public int getGameState() {
        if(gameState == GAMESTATE.MENU) {
            return 1;
        } else if(gameState == GAMESTATE.GAME) {
            return 2;
        } else if(gameState == GAMESTATE.PAUSE) {
            return 3;
        } else {
            return 0;
        }
    }
    public void setGameState(GAMESTATE state) {
        gameState = state;
    }

	public void newGame() {
        music.stop();
        handler.newGame();
        level.newGame();
	}
}
