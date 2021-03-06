package net.dragberry.thegame.game;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input.Keys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Rectangle;
import net.dragberry.thegame.game.objects.BunnyHead;
import net.dragberry.thegame.game.objects.Feather;
import net.dragberry.thegame.game.objects.GoldCoin;
import net.dragberry.thegame.game.objects.Rock;
import net.dragberry.thegame.screen.MenuScreen;
import net.dragberry.thegame.game.objects.BunnyHead.JUMP_STATE;
import net.dragberry.thegame.util.CameraHelper;
import net.dragberry.thegame.util.Constants;

/**
 * Created by maksim on 11.01.17.
 */
public class WorldController extends InputAdapter {

    private static final String TAG = WorldController.class.getName();
    
    private Game game;
    
    public CameraHelper cameraHelper;
    
    public Level level;
    public int lives;
    public int score;
    
    private Rectangle r1 = new Rectangle();
    private Rectangle r2 = new Rectangle();
    
    private float timeLeftGameOverDelay;

    public WorldController(Game game) {
    	this.game = game;
        init();
    }

    private void init() {
    	Gdx.input.setInputProcessor(this);
    	cameraHelper = new CameraHelper();
    	lives = Constants.LIVES_START;
    	timeLeftGameOverDelay = 0;
    	initLevel();
    }
    
    private void initLevel() {
    	score = 0;
    	level = new Level(Constants.LEVEL_01);
    	cameraHelper.setTarget(level.bunnyHead);
    }
    
    @Override
    public boolean keyUp(int keycode) {
    	switch (keycode) {
		case  Keys.R:
			init();
    		Gdx.app.debug(TAG, "Game world has been resetted!");
			break;
		case Keys.ENTER:
			cameraHelper.setTarget(cameraHelper.hasTarget() ? null : level.bunnyHead);
			break;
		case Keys.ESCAPE:
		case Keys.BACK:
			backToMenu();
			break;
		default:
			break;
		}
    	return false;
    }


    public void update(float deltaTime) {
    	handleDebugInput(deltaTime);
    	if (isGameOver()) {
    		timeLeftGameOverDelay -= deltaTime;
    		if (timeLeftGameOverDelay < 0) {
    			backToMenu();
    		}
    	} else {
    		handleInputGame(deltaTime);
    	}
    	level.update(deltaTime);
    	testCollisions();
        cameraHelper.update(deltaTime);
        if (!isGameOver() && isPlayerInWater()) {
        	lives--;
        	if (isGameOver()){
        		timeLeftGameOverDelay = Constants.TIME_DELAY_GAME_OVER;
        	} else {
        		initLevel();
        	}
        }
    }
    
    private void handleInputGame(float deltaTime) {
    	if (cameraHelper.hasTarget(level.bunnyHead)) {
    		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
    			level.bunnyHead.velocity.x = -level.bunnyHead.terminalVelocity.x;
    		} else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
    			level.bunnyHead.velocity.x = level.bunnyHead.terminalVelocity.x;
    		} else {
    			if (Gdx.app.getType() != ApplicationType.Desktop) {
    				level.bunnyHead.velocity.x = level.bunnyHead.terminalVelocity.x;
    			}
    		}
    		
    		// Bunny jump
			level.bunnyHead.setJumping(Gdx.input.isTouched() || Gdx.input.isKeyPressed(Keys.SPACE));
    	}
    }

    private void handleDebugInput(float deltaTime) {
		if (Gdx.app.getType() != ApplicationType.Desktop) {
			return;
		}
		
		if (!cameraHelper.hasTarget(level.bunnyHead)) {
			float camMoveSpeed = 5 * deltaTime;
			float camMoveSpeedAccelerationFactor = 5;
			
			if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
				camMoveSpeed *= camMoveSpeedAccelerationFactor;
			}
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				moveCamera(-camMoveSpeed, 0);
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				moveCamera(camMoveSpeed, 0);
			}
			if (Gdx.input.isKeyPressed(Keys.UP)) {
				moveCamera(0, camMoveSpeed);
			}
			if (Gdx.input.isKeyPressed(Keys.DOWN)) {
				moveCamera(0, -camMoveSpeed);
			}
			if (Gdx.input.isKeyPressed(Keys.BACKSPACE)) {
				cameraHelper.setPosition(0, 0);
			}
		}
		
		float camZoomSpeed = 1 * deltaTime;
		float camZoomSpeedAccelerationfactor = 5;
		
		if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
			camZoomSpeed *= camZoomSpeedAccelerationfactor;
		}
		if (Gdx.input.isKeyPressed(Keys.COMMA)) {
			cameraHelper.addZoom(camZoomSpeed);
		}
		if (Gdx.input.isKeyPressed(Keys.PERIOD)) {
			cameraHelper.addZoom(-camZoomSpeed);
		}
		if (Gdx.input.isKeyPressed(Keys.SLASH)) {
			cameraHelper.setZoom(1);
		}
		
	}
    
    private void moveCamera(float x, float y) {
    	x += cameraHelper.getPosition().x;
    	y += cameraHelper.getPosition().y;
    	cameraHelper.setPosition(x, y);
    }

    private void onCollisionBunnyWithRock(Rock rock) {
    	BunnyHead bunnyHead = level.bunnyHead;
    	float heightDifference = Math.abs(bunnyHead.position.y - (rock.position.y + rock.bounds.height));
    	if (heightDifference > 0.25f) {
    		boolean hitRightEdge = bunnyHead.position.x > (rock.position.x + rock.bounds.width / 2.0f);
    		if (hitRightEdge) {
    			bunnyHead.position.x = rock.position.x + rock.bounds.width;
    		} else {
    			bunnyHead.position.x = rock.position.x - bunnyHead.bounds.width;
    		}
    		return;
    	}
    	
    	switch (bunnyHead.jumpState) {
    	case GROUNDED:
    		break;
    	case FALLING:
    	case JUMP_FALLING:
    		bunnyHead.position.y = rock.position.y + bunnyHead.bounds.height + bunnyHead.origin.y;
    		bunnyHead.jumpState = JUMP_STATE.GROUNDED;
    		break;
    	case JUMP_RISING:
    		bunnyHead.position.y = rock.position.y + bunnyHead.bounds.height + bunnyHead.origin.y;
    		break;
    	}
    }
    
    private void onCollisionBunnyWithGolCoin(GoldCoin coin) {
    	coin.collected = true;
    	score += coin.getScore();
    	Gdx.app.log(TAG,  "Gold coin collected");
    }
    
    private void onCollisionBunnyWithFeather(Feather feather) {
    	feather.collected = true;
    	score += feather.getScore();
    	level.bunnyHead.setFeatherPowerup(true);
    	Gdx.app.log(TAG,  "Feather collected");
    	
    }
    
    private void testCollisions() {
    	r1.set(level.bunnyHead.position.x, level.bunnyHead.position.y,
    			level.bunnyHead.bounds.width, level.bunnyHead.bounds.height);
    	for (Rock rock : level.rocks) {
    		r2.set(rock.position.x, rock.position.y, rock.bounds.width, rock.bounds.height);
    		if (!r1.overlaps(r2)) {
    			continue;
    		}
    		onCollisionBunnyWithRock(rock);
    		break;
    	}
    	
    	for (GoldCoin coin : level.goldCoins) {
    		r2.set(coin.position.x, coin.position.y, coin.bounds.width, coin.bounds.height);
    		if (!r1.overlaps(r2)) {
    			continue;
    		}
    		onCollisionBunnyWithGolCoin(coin);
    		break;
    	}
    	
    	for (Feather feather : level.feathers) {
    		r2.set(feather.position.x, feather.position.y, feather.bounds.width, feather.bounds.height);
    		if (!r1.overlaps(r2)) {
    			continue;
    		}
    		onCollisionBunnyWithFeather(feather);
    		break;
    	}
    }
    
    public boolean isGameOver() {
    	return lives < 0;
    }
    
    public boolean isPlayerInWater() {
    	return level.bunnyHead.position.y < -5;
    }
    
    private void backToMenu() {
    	game.setScreen(new MenuScreen(game));
    }

}
