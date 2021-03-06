package me.bscal.game.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import me.bscal.game.Game;
import me.bscal.game.attributes.Attribute;
import me.bscal.game.entity.projectile.Projectile;
import me.bscal.game.graphics.Rectangle;
import me.bscal.game.graphics.Render;
import me.bscal.game.sprites.AnimatedSprite;
import me.bscal.game.sprites.Sprite;
import me.bscal.game.util.Cooldown;
import me.bscal.serialization.QVField;
import me.bscal.serialization.QVObject;
import me.bscal.serialization.QVString;

public abstract class Entity implements GameObject{
	
	protected List<Projectile> projectiles 	= new ArrayList<Projectile>();
	protected List<Cooldown> cooldowns 		= new ArrayList<Cooldown>();
	protected Set<Attribute> attributes 	= new HashSet<Attribute>();
	
	protected String name;
	protected Rectangle rect;
	protected Sprite sprite;
	protected AnimatedSprite animatedSprite = null;
	protected boolean isRemoved 			= false;
	protected boolean isMoving 				= false;
	public boolean isInvulnerable 			= false;
	public boolean isCollidable 			= true;	
	public boolean isVisible 				= true;
	protected int layer 					= 0;
	protected int direction 				= 0;		//0 = Right, 1 = Left, 2 = Up, 3 = Down
	protected int time 						= 0;
	protected int animationLength;
	protected float speed;
	
	public int id;
	
	public Entity() {}
	
	public void init() {
		Game.getEntities().add(this);
	}
	
	protected void updateDirection() {
		if(animatedSprite != null) {
			int range = direction * animationLength;
			animatedSprite.setAnimationRange(range, range + (animationLength - 1));
		}
	}
	
	public void render(Render renderer, int xZoom, int yZoom) {
		if(isVisible) {
			if(animatedSprite != null) {
				renderer.renderSprite(animatedSprite, rect.x, rect.y, xZoom, yZoom, false);
			}
			else if(sprite != null) {
				renderer.renderSprite(sprite, rect.x, rect.y, xZoom, yZoom, false);
			}
			else {
				renderer.renderRectangle(rect, xZoom, yZoom, false);
			}
		}
	}
	
	public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom) { return false; }
	
	public Rectangle getRectangle() {
		return rect;
	}
	
	public int getLayer() {
		return layer;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isRemoved() {
		return isRemoved;
	}
	
	public int getID() {
		return id;
	}
	
	public List<Projectile> getProjectiles() {
		return projectiles;
	}
	
	public void remove() {
		Game.getRemovedEntities().add(this);
		isRemoved = true;
	}
	
	/**
	 * Returns int 0 - 4 holding the directions Right, Left, Up, Down respectively.
	 */
	public int getDirection() {
		return direction;
	}
	
	/**
	 * Simple collision detections. Entities will not slide on/off walls or areas that collision is detected.
	 * @return false if no collision is detected.
	 */
	public boolean simpleCollisionCheck(Game game, Rectangle rect) {
		if(!game.getMap().checkCollision(rect, layer, game.getXZoom(), game.getYZoom()) 
				&& !game.getMap().checkCollision(rect, layer + 1, game.getXZoom(), game.getYZoom())
				&& !game.getMap().checkCollision(rect, layer + 2, game.getXZoom(), game.getYZoom())) {
			return false;
		}
		return true;
	}
	
	public static double getProjectileDirection(double targetX, double targetY, double srcX, double srcY) {
		double dx = targetX - srcX;
		double dy = targetY - srcY;
		double dir = Math.atan2(dy, dx);
		return dir;
	}
	
	public int getDirectionFromMouse(double angle) {
		return -1;
	}
	
	public Set<Attribute> getAttributes() {
		return attributes;
	}
	
	public Attribute getAttribute(String name) {
		Iterator<Attribute> it = attributes.iterator();
		while(it.hasNext()) {
			Attribute a = it.next();
			if(a.equals(name)) {
				return a;
			}
		}
		return null;
	}
	
	public void updateCooldowns() {
		if(!cooldowns.isEmpty()) {
			Iterator<Cooldown> i = cooldowns.iterator();
			while (i.hasNext()) {
				Cooldown cd = i.next();
				cd.update();
				if(!cd.onCooldown()) {
				   i.remove();
				}
			}
		}
	}
	
	public boolean isOnCooldown(String s) {
		for(int i = 0; i < cooldowns.size(); i++) {
			if(s == cooldowns.get(i).getId()) {
				return true;
			}
		}
		return false;
	}
	
	public Cooldown getCooldown(String s) {
		for(int i = 0; i < cooldowns.size(); i++) {
			if(s == cooldowns.get(i).getId()) {
				return cooldowns.get(i);
			}
		}
		return null;
	}
	
	public List<Cooldown> getCooldowns() {
		return cooldowns;
	}
	
	public void serialize(QVObject o) {
		o.addField(QVField.createInt("x", rect.x));
		o.addField(QVField.createInt("y", rect.y));
		o.addField(QVField.createInt("dir", direction));
		o.addField(QVField.createBoolean("mov", isMoving));
		o.addField(QVField.createInt("id", id));
		o.addString(QVString.create("Name", name.toCharArray()));
	}
	
	public void deserialize(QVObject o) {
		this.rect.x = o.findField("x").getInt();
		this.rect.y = o.findField("y").getInt();
		this.direction = o.findField("dir").getInt();
		this.isMoving = o.findField("mov").getBoolean();
		this.id = o.findField("id").getInt();
		this.name = o.findString("Name").getString();
	}
	
}
