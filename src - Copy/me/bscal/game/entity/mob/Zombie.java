package me.bscal.game.entity.mob;

import java.util.List;

import me.bscal.game.Game;
import me.bscal.game.entity.Player;
import me.bscal.game.graphics.Rectangle;
import me.bscal.game.graphics.Render;
import me.bscal.game.mapping.Node;
import me.bscal.game.sprites.AnimatedSprite;
import me.bscal.game.sprites.Sprite;
import me.bscal.game.util.Vector2i;

public class Zombie extends NPC{

	private List<Node> path = null;
	private int time = 0;
	
	public Zombie(Sprite sprite, int animationLength) {
		this.animatedSprite = (AnimatedSprite) sprite;
		this.animationLength = animationLength;
		rect = new Rectangle(15, 15, 20, 26);
		collisionRect = new Rectangle(15, 15, 18, 24);
		speed = 5;
		updateDirection();
	}
	
	public void render(Render renderer, int xZoom, int yZoom) {
		renderer.renderSprite(animatedSprite, 0xFF7200, 0xFF732611, rect.x, rect.y, xZoom, yZoom, false);
	}
	
	public void update(Game game) {
		boolean moved = false;
		int newDirection = direction;
		time++;
		move(game);
		
		if(xa < 0) {
			newDirection = 1;
			moved = true;
		}
		if(xa > 0) {
			newDirection = 0;
			moved = true;
		}
		if(ya < 0) {
			newDirection = 2;
			moved = true;
		}
		if(ya > 0) {
			newDirection = 3;
			moved = true;
		}
		
		if(newDirection != direction) {
			direction = newDirection;
			updateDirection();
		}
		
		if(moved) {
			collisionRect.x += xa;
			collisionRect.y += ya;
			checkCollision(game);
			animatedSprite.update(game);
		}
		else {
			animatedSprite.reset();
		}
	}
	
	private void move(Game game) {
		xa = 0;
		ya = 0;
		int px = game.getPlayer(-1).getRectangle().x;
		int py = game.getPlayer(-1).getRectangle().y;
		Vector2i start = new Vector2i(rect.x / (16 * 2), rect.y / (16 * 2));
		Vector2i destination = new Vector2i(px / (16 * 2), py / (16 * 2));
		if(time % 30 == 0) {
			path = game.getMap().findPath(start, destination);
			System.out.println("Path: " + start.getX() + " | " + start.getY() +" | "+ destination.getX() + " | " + destination.getY());
			System.out.println(path);
		}
		if(path != null) {
			//If a there is a Node to travel too the NPC will travel to the Node.
			if(path.size() > 0) {
				Vector2i vector = path.get(path.size() - 1).tile;
				if(rect.x < vector.getX() * (16 * 2)) {
					xa++;
				}
				if(rect.x > vector.getX() * (16 * 2)) {
					xa--;
				}
				if(rect.y < vector.getY() * (16 * 2)) {
					ya++;
				}
				if(rect.y > vector.getY() * (16 * 2)) {
					ya--;
				}
			}
			//Else travel to the nearest player.
			else {
				List<Player> entities = game.getMap().getNearbyPlayers(this, 50);
				if(entities.size() > 0) {
					Rectangle pRect = entities.get(0).getRectangle();
					if(rect.x < pRect.x) {
						xa++;
					}
					if(rect.x > pRect.x) {
						xa--;
					}
					if(rect.y < pRect.y) {
						ya++;
					}
					if(rect.y > pRect.y) {
						ya--;
					}
				}
			}
		}
	}

}