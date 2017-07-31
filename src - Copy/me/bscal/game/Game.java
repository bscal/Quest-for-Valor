package me.bscal.game;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import me.bscal.game.GUI.GUI;
import me.bscal.game.GUI.GUIButton;
import me.bscal.game.GUI.SDKButton;
import me.bscal.game.entity.GameObject;
import me.bscal.game.entity.Player;
import me.bscal.game.entity.mob.Archer;
import me.bscal.game.entity.mob.Dummy;
import me.bscal.game.graphics.Rectangle;
import me.bscal.game.graphics.Render;
import me.bscal.game.listeners.KeyboardListener;
import me.bscal.game.listeners.MouseClickListener;
import me.bscal.game.mapping.Map;
import me.bscal.game.mapping.Tiles;
import me.bscal.game.sprites.AnimatedSprite;
import me.bscal.game.sprites.Sprite;
import me.bscal.game.sprites.SpriteHandler;
import me.bscal.game.util.Font;

public class Game extends JFrame implements Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7240204747443743168L;
	public static int width = 900;
	public static int height = width / 12 * 9;
	public static final int XZOOM = 2;
	public static final int YZOOM = 2;
	public static final int ALPHA = 0xFFCCFF00; //0xFF defines Hex color codes
	public static final String PATH = "resources/img/";
	
	private Thread thread;
	private boolean isRunning = false;
	private Canvas canvas = new Canvas();
	private Render renderer;
	private Tiles tiles;
	private Map map;
	private Font font;
	private KeyboardListener listener;
	private MouseClickListener mouseListener;
	private Player player;
	private int selectedTileID = 3;
	private int selectedLayer = 0;
	
	private static List<GameObject> entities = new ArrayList<GameObject>();
	private static List<GameObject> entitiesToRemove = new ArrayList<GameObject>();
	private static List<GameObject> entitiesToAdd = new ArrayList<GameObject>();
	private ArrayList<Integer> fpsList = new ArrayList<Integer>();
	
	public Game() {
		this.setTitle("Game");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(0, 0, width, height);
		this.setLocationRelativeTo(null);
		canvas.setBounds(0, 0, width, height);
		this.add(canvas);
		this.pack();
		this.setVisible(true);
		canvas.createBufferStrategy(2);
		renderer = new Render(canvas.getWidth(), canvas.getHeight());
		init();
	}
	
	private void init() {
		float start = System.nanoTime();
		String workingDir = System.getProperty("user.dir");
		System.out.println("Current working directory : " + workingDir);
		//Loads SpriteSheet
		new SpriteHandler();
		//Tiles/Map
		File tilesFile = new File(Game.class.getResource("resources/Tiles.txt").getPath());
		File mapFile = new File(Game.class.getResource("resources/Map.txt").getPath());
		tiles = new Tiles(tilesFile, SpriteHandler.tileSheet);
		map = new Map(mapFile, tiles);
		font = new Font();
		//Load GUI and SDKButtonGUI
		GUIButton[] buttons = new GUIButton[tiles.size()];
		Sprite[] tileList = tiles.getSprites();
		for(int i = 0; i < buttons.length; i++) {
			Rectangle tileRect = new Rectangle(0, i*(16 * XZOOM + 2), 16*XZOOM, 16*YZOOM);
			buttons[i] = new SDKButton(this, i, tileList[i], tileRect);
		}
		GUI gui = new GUI(buttons, 5, 5, true);

		//Initialize entities
		AnimatedSprite animatedPlayer = new AnimatedSprite(SpriteHandler.playerSheet, 3);
		player = new Player(animatedPlayer, 8);
		for(int i = 0; i < 1; i++) {
			//entitiesToAdd.add(new Zombie(new AnimatedSprite(SpriteHandler.playerSheet, 3), 8));
			entitiesToAdd.add(new Archer());
			entitiesToAdd.add(new Dummy(new AnimatedSprite(SpriteHandler.playerSheet, 3), 8));
		}
		entities.add(player);
		entities.add(gui);
		//Initialize listeners
		mouseListener = new MouseClickListener(this);
		listener = new KeyboardListener(this);
		canvas.addKeyListener(listener);
		canvas.addFocusListener(listener);
		canvas.addMouseListener(mouseListener);
		canvas.addMouseMotionListener(mouseListener);
		addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) {}

			@Override
			public void componentMoved(ComponentEvent e) {}

			@Override
			public void componentResized(ComponentEvent e) {
				getRenderer().getCamera().width = canvas.getWidth();
				getRenderer().getCamera().height = canvas.getHeight();
				width = canvas.getWidth();
				height = canvas.getHeight();
			}

			@Override
			public void componentShown(ComponentEvent e) {}
		});
		canvas.requestFocus();
		
		float finish = System.nanoTime();
		System.out.println("Startup took: " + ((finish - start) / 1000000) + " ms | " + ((finish - start) / 1000000000) + " seconds");
	}
	
	public void update() {
		for(int i = 0; i < entities.size(); i++) {
			entities.get(i).update(this);
		}
		updateEntities();
	}
	
	public void render() {
		BufferStrategy bufferStrategy = canvas.getBufferStrategy();
		Graphics g = bufferStrategy.getDrawGraphics();
		
		map.render(renderer, getEntities(), XZOOM, YZOOM);
		font.render("Hello! How is your day? \nGood.", 50, 50, 0, 3252, renderer, XZOOM, YZOOM);
		renderer.render(g);
	
		g.dispose();
		bufferStrategy.show();
		renderer.clear();
	}

	public void run() {
		long lastTime = System.nanoTime();
		double delta = 0;
		int fps = 0;
		int updates = 0;
		long timer = System.currentTimeMillis();
		final long OPTIMAL_TIME = 1000000000 / 30;
	
		while(isRunning) {
			long now = System.nanoTime();
			delta += (now - lastTime) / (double)OPTIMAL_TIME;
			lastTime = now;
			if(delta >= 1) {
				update();
				updates++;
				delta--;
			}
			render();
			fps++;
			if(System.currentTimeMillis() - timer > 1000L) {
				timer += 1000L;
				Runtime runtime = Runtime.getRuntime();
				long memory = runtime.totalMemory() - runtime.freeMemory();
			    System.out.println("Used memory in megabytes: " + memory/1048576);
				System.out.println("Fps: " + fps + " | Ticks: " + updates + " | Entites: " + entities.size());
				fpsList.add(fps);
				fps = 0;
				updates = 0;
			}
		}
		stop();
	}
	
	private void calculatePreformance() {
		Collections.sort(fpsList);
		Collections.reverse(fpsList);
		int mean = 0;
		for(int i = 0; i < fpsList.size(); i++) {
			mean += i;
		}
		System.out.println("Max FPS: " + fpsList.get(0));
		System.out.println("Min FPS: " + fpsList.get(fpsList.size() - 1));
		System.out.println("Mean FPS: " + mean/fpsList.size());
	}
	
	public synchronized void start() {
		isRunning = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public synchronized void stop() {
		isRunning = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
	{
		Game game = new Game();
		game.start();
	}
	
	public KeyboardListener getKeyListener() {
		return listener;
	}
	
	public MouseClickListener getMouseListener() {
		return mouseListener;
	}
	
	public Render getRenderer() {
		return renderer;
	}
	
	public int getXZoom() {
		return XZOOM;
	}
	
	public int getYZoom() {
		return XZOOM;
	}
	
	public Map getMap() {
		return map;
	}
	
	public void saveGame() {
		map.saveMap();
	}
	
	public static List<GameObject> getEntities() {
		return entities;
	}
	
	private void updateEntities() {
		if(!entitiesToRemove.isEmpty()) {
			Iterator<GameObject> i = entities.iterator();
			while (i.hasNext()) {
				GameObject o = i.next();
				if(entitiesToRemove.contains(o)) {
				   i.remove();
				}
			}
			entitiesToRemove.clear();
		}
		if(!entitiesToAdd.isEmpty()) {
			entities.addAll(entitiesToAdd);
			entitiesToAdd.clear();
		}
	}
	
	public static List<GameObject> getRemovedEntities() {
		return entitiesToRemove;
	}
	
	public static List<GameObject> getAddedEntities() {
		return entitiesToAdd;
	}


	public void setSelectedTile(int id) {
		selectedTileID = id;
	}
	
	public int getSelectedTileID() {
		return selectedTileID;
	}
	
	public int getSelectedLayer() {
		return selectedLayer;
	}
	
	public Player getPlayer(int id) {
		List<Player> players = new ArrayList<Player>();
		for(int i = 0; i < entities.size(); i++) {
			if(entities.get(i) instanceof Player) {
				players.add((Player) entities.get(i));
			}
		}
		if(id == -1) {
			return players.get(0);
		}
		return players.get(id);
	}
	
}